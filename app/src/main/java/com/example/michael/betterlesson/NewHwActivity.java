package com.example.michael.betterlesson;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.michael.betterlesson.Adapters.ContactsAdapter;
import com.example.michael.betterlesson.Logic.User;

public class NewHwActivity extends AppCompatActivity {

    //Logic references
    private User currentUser;
    private User selectedStudent;
    private String date;

    //Views
    private ListView studentsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_hw);

        Bundle extras = getIntent().getExtras();
        currentUser = (User)extras.getSerializable("currentUser");

        studentsList = findViewById(R.id.students_list_view_for_hw);

        ContactsAdapter studentsAdapter = new ContactsAdapter(this,currentUser.getContacts());
        studentsAdapter.notifyDataSetChanged();
        studentsList.setAdapter(studentsAdapter);

        studentsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedStudent = (User)studentsList.getItemAtPosition(position);

                Intent addPdfIntent = new Intent(NewHwActivity.this,AddPdfActivity.class);
                addPdfIntent.putExtra("currentUser",currentUser);
                addPdfIntent.putExtra("selectedStudent",selectedStudent);
                startActivity(addPdfIntent);
                finish();
            }
        });
    }
}
