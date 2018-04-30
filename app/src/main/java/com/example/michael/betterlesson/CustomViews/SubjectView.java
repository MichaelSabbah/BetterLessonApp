package com.example.michael.betterlesson.CustomViews;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.michael.betterlesson.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SubjectView extends LinearLayout {

    //Logic reference
    private String lessonKey;
    private String subjectName;

    //Firebase database reference
    private DatabaseReference mDatabase;

    //UI references
    private TextView subject;
    private ImageView deleteButton;

    public SubjectView(Context context, String lessonId, final String subjectName){
        super(context);

        this.lessonKey = lessonId;
        this.subjectName = subjectName;

        subject = new TextView(context);
        deleteButton = new ImageView(context);

        this.setOrientation(HORIZONTAL);
        int margin = (int)getResources().getDimension(R.dimen.list_row_margin);
        this.setPadding(margin,margin,margin,margin);
        this.setWeightSum(1);

        //Subject name
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(0,LayoutParams.MATCH_PARENT,0.85f);
        param.setMargins(0,0,100,0);
        subject.setTextAppearance(R.style.TextStyle);

        subject.setLayoutParams(param);
        addView(subject);

        //Delete button
        param = new LinearLayout.LayoutParams(0,LayoutParams.MATCH_PARENT,0.15f);
        deleteButton.setImageResource(R.mipmap.delete_icon);
        deleteButton.setLayoutParams(param);
        deleteButton.setForegroundGravity(Gravity.LEFT);
        deleteButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase.child("lessons").child(lessonKey).child("subjects").child(subjectName).setValue(null);
            }
        });

        addView(deleteButton);
    }

    public TextView getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject.setText(subject);
    }

    public ImageView getDeleteButton() {
        return deleteButton;
    }

    public void setDeleteButton(ImageView deleteButton) {
        this.deleteButton = deleteButton;
    }
}
