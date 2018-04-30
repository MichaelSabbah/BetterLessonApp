package com.example.michael.betterlesson;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.michael.betterlesson.Adapters.SubjectAdapter;
import com.example.michael.betterlesson.Logic.Lesson;
import com.example.michael.betterlesson.Logic.UserType;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LessonPageActivity extends AppCompatActivity{

    //Logic references
    private Lesson lesson;
    private ArrayList<String> subjects;
    private UserType currentUserType;

    //UI references
    private TextView statusColor;
    private TextView contactType;
    private TextView contactName;
    private TextView course;
    private TextView date;
    private TextView startingTime;
    private TextView endingTime;
    private TextView address;
    private ListView subjectsList;
    private ImageView addToSubjectButton;
    private EditText newSubjectEditText;

    //Firebase database reference
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_page);

        Bundle extras = getIntent().getExtras();
        this.lesson = (Lesson) extras.getSerializable("lesson");
        this.currentUserType = (UserType)extras.getSerializable("currentUserType");

        //Get views
        statusColor = (TextView)findViewById(R.id.lesson_status_circle);
        //contactType = (TextView)findViewById(R.id.contact_type);
        contactName = (TextView)findViewById(R.id.contact_name);
        course = (TextView)findViewById(R.id.lesson_course_name);
        date = (TextView)findViewById(R.id.lesson_date);
        startingTime = (TextView)findViewById(R.id.lesson_starting_time);
        endingTime = (TextView)findViewById(R.id.lesson_ending_time);
        subjectsList = (ListView)findViewById(R.id.subjects_list_view);
        addToSubjectButton = (ImageView)findViewById(R.id.add_subject_button);
        address = (TextView)findViewById(R.id.lesson_addres);

        //Set views
        setStatusCircleColor();
        String name;
        if(currentUserType.equals(UserType.TEACHER))
            name = lesson.getStudentName();
        else
            name = lesson.getTeacherName();
        contactName.setText(name);
        course.setText(lesson.getCourseName());
        date.setText(lesson.getDate());
        startingTime.setText(lesson.getStartingTime() + "-");
        endingTime.setText(lesson.getEndingTime());
        address.setText(lesson.getAddress());
        newSubjectEditText = (EditText)findViewById(R.id.new_subject);

        loadSubjectsFromDatabase();

        addToSubjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject = newSubjectEditText.getText().toString();
                newSubjectEditText.setText(null);
                addNewSubjectToDatabase(subject);
            }
        });
    }

    public void setStatusCircleColor() {
        if (lesson.isPassed())
            statusColor.setBackground(getDrawable(R.drawable.grey_circle_view));
        else if (!lesson.isTeacherApprove() || !lesson.isStudentApprove()) {
            statusColor.setBackground(getDrawable(R.drawable.orange_circle_view));
        } else {
            statusColor.setBackground(getDrawable(R.drawable.green_circle_view));
        }
    }

    public void loadSubjectsFromDatabase(){
        mDatabase = FirebaseDatabase.getInstance().getReference().child("lessons").child(lesson.getId()).child("subjects");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                subjects = new ArrayList<String>();
                for(DataSnapshot subjectRecord : dataSnapshot.getChildren()){
                    subjects.add(subjectRecord.getKey());
                }

                //Set subjects list view adpater
                SubjectAdapter subjectAdapter = new SubjectAdapter(getApplicationContext(),lesson.getId(),subjects);
                subjectAdapter.notifyDataSetChanged();
                subjectsList.setAdapter(subjectAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void addNewSubjectToDatabase(String subject){
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("lessons").child(lesson.getId()).child("subjects").child(subject).setValue(true);
    }
}
