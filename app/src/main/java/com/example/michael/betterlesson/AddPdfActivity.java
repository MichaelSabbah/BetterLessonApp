package com.example.michael.betterlesson;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.michael.betterlesson.Firebase.DatabaseHandler;
import com.example.michael.betterlesson.Logic.HW;
import com.example.michael.betterlesson.Logic.TimeFormats;
import com.example.michael.betterlesson.Logic.User;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.UUID;

public class AddPdfActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PHOTO_REQUEST = 9002;
    private static final int REQUEST_READ_PERMISSION = 9003;

    //Logic reference
    private User currentUser;
    private User selectedStudent;
    private static String date;

    //Firebase references
    private FirebaseStorage storage;
    private DatabaseReference mDatabase;

    //Views
    private ImageView file;
    private Button addButton;
    private TextView tapToUploadText;
    private PDFView pdfView;
    private LinearLayout uploadFileProgress;
    private LinearLayout uploadForm;

    //PDF path
    private String pdfPath;
    private Uri pdfUri;
    private String pdfUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pdf);

        Bundle extras = getIntent().getExtras();
        currentUser = (User)extras.getSerializable("currentUser");
        selectedStudent = (User)extras.getSerializable("selectedStudent");

        //Initial dtabase reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Show the date picker
        DialogFragment newFragment = new AddPdfActivity.DatePickerFragment();
        newFragment.show(getFragmentManager(),"datePicker");

        pdfView = (PDFView)findViewById(R.id.pdf_viewer);
        addButton = (Button)findViewById(R.id.upload_file_button);
        tapToUploadText = (TextView)findViewById(R.id.tap_to_upload_text);
        uploadFileProgress = (LinearLayout) findViewById(R.id.upload_file_progress);
        uploadForm = (LinearLayout)findViewById(R.id.upload_form);

        pdfView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });
        addButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showProgress(true);

                pdfPath = "hw/" + UUID.randomUUID() + "_pdf";
                storage = FirebaseStorage.getInstance();
                StorageReference pdfRef = storage.getReference(pdfPath);

                pdfRef.putFile(pdfUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                pdfUrl = taskSnapshot.getDownloadUrl().toString();
                                addHwReferenceToDatabase();
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Toast.makeText(AddPdfActivity.this,"upload failed",Toast.LENGTH_LONG).show();

                            }
                        });
            }
        });
    }

    public void requestPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(AddPdfActivity.this,android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(AddPdfActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)){

                }else{
                    ActivityCompat.requestPermissions(AddPdfActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_READ_PERMISSION);
                }
            }else{
                openFilePicker();
            }
        }else{
            openFilePicker();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       switch (requestCode){
           case REQUEST_READ_PERMISSION:{
               if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                   openFilePicker();
               }else{
                   Toast.makeText(AddPdfActivity.this,"Cannot pick file from storage",Toast.LENGTH_LONG).show();
               }
           }
       }
    }

    private void openFilePicker(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PHOTO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PHOTO_REQUEST && resultCode == RESULT_OK && data!= null){
            pdfUri = data.getData();
            tapToUploadText.setVisibility(View.GONE);
            pdfView.fromUri(pdfUri).load();
        }
    }

    //Pickers fragments class
    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker

            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH) + 1;
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);

        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

            date = TimeFormats.getDateFormat(year,month,dayOfMonth);
        }
    }

    public void addHwReferenceToDatabase(){
        String currentUserEmailKey = DatabaseHandler.emailToValidKey(currentUser.getEmail());
        String selectedStudentEmailKey = DatabaseHandler.emailToValidKey(selectedStudent.getEmail());
        HW hw = new HW(pdfPath,date,currentUser.getName(),selectedStudent.getName(),
                currentUser.getEmail(),selectedStudent.getEmail(),selectedStudent.getCourse(),pdfUri.toString(),pdfUrl,false);

        //Add to hw total list
        mDatabase.child(hw.getHwId()).setValue(hw);

        //Add to teacher hw list
        mDatabase.child("teachers").child(currentUserEmailKey).child(hw.getHwId()).setValue(true);

        //Add to student hw lis
        mDatabase.child("students").child(selectedStudentEmailKey).child(hw.getHwId()).setValue(true);

    }

    public void showProgress(boolean show){
        uploadFileProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        uploadForm.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
