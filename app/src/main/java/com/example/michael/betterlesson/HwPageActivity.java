package com.example.michael.betterlesson;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.michael.betterlesson.Logic.HW;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class HwPageActivity extends AppCompatActivity {

    //Logic references
    private HW hw;

    //Ui references
    private PDFView pdfViewer;
    private Button downloadButton;
    private LinearLayout downloadForm;
    private LinearLayout downloadProgress;

    //Firebase storage references
    private FirebaseStorage storage;
    private StorageReference pdfRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hw_page);

        Bundle extras = getIntent().getExtras();
        hw = (HW)extras.getSerializable("hw");

        storage = FirebaseStorage.getInstance();
        pdfRef = storage.getReference().child(hw.getHwId());

        pdfViewer = (PDFView)findViewById(R.id.pdf_hw_viewer);
        downloadButton = (Button)findViewById(R.id.download_button);
        downloadProgress= (LinearLayout)findViewById(R.id.download_progress);
        downloadForm = (LinearLayout)findViewById(R.id.viewer_form);

        loadFileFromStorage();

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadFile();
            }
        });
    }

    public void loadFileFromStorage(){
        final long ONE_MEGABYTE = 1024 * 1024;
        pdfRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                downloadProgress.setVisibility(View.GONE);
                pdfViewer.fromBytes(bytes).load();
                downloadForm.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });
    }

    public void downloadFile(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(hw.getUrl()));
        startActivity(intent);
    }
}
