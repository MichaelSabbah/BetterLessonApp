package com.example.michael.betterlesson.CustomViews;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.michael.betterlesson.Firebase.DatabaseHandler;
import com.example.michael.betterlesson.Logic.UserType;
import com.example.michael.betterlesson.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LessonView extends LinearLayout {
    //Lesson identifier
    private String id;
    private UserType currentUserType;

    //Firebase
    private DatabaseReference mDatabase;

    //Lesson views
    private ImageView approvalOkImage;
    private TextView contactName;
    private TextView date;
    private TextView hour;
    private TextView statusColor;
    private TextView courseName;

    public LessonView(Context context, String id, UserType currentUserType){
        super(context);
        this.id = id;
        this.currentUserType = currentUserType;

        approvalOkImage = new ImageView(context);
        contactName = new TextView(context);
        date = new TextView(context);
        hour = new TextView(context);
        statusColor = new TextView(context);
        courseName = new TextView(context);

        this.setOrientation(HORIZONTAL);
        this.setWeightSum(1);

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.activatedBackgroundIndicator, typedValue, true);
        if (typedValue.resourceId != 0) {
            this.setBackgroundResource(typedValue.resourceId);
        } else {
            this.setBackgroundColor(typedValue.data);
        }

        //Set the status color
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(0,LayoutParams.MATCH_PARENT,0.03f);
        statusColor.setLayoutParams(param);
        addView(statusColor);

        //Set name text view
         param = new LinearLayout.LayoutParams(0,250,0.3f);
        param.setMargins(30,0,0,0);
        contactName.setTextAppearance(R.style.TextStyle);
        contactName.setLayoutParams(param);
        //name.setHeight(LayoutParams.MATCH_PARENT);
        contactName.setGravity(Gravity.CENTER_VERTICAL);
        addView(contactName);

        //Set date text view
        param = new LinearLayout.LayoutParams(0,LayoutParams.MATCH_PARENT,0.3f);
        param.setMargins(30,0,0,0);
        date.setTextAppearance(R.style.TextStyle);
        date.setLayoutParams(param);
        //name.setHeight(LayoutParams.MATCH_PARENT);
        date.setGravity(Gravity.CENTER_VERTICAL);
        addView(date);

        //Set hour text view
        param = new LinearLayout.LayoutParams(0,LayoutParams.MATCH_PARENT,0.3f);
        param.setMargins(30,0,0,0);
        hour.setTextAppearance(R.style.TextStyle);
        hour.setLayoutParams(param);
        //name.setHeight(LayoutParams.MATCH_PARENT);
        hour.setGravity(Gravity.CENTER);
        addView(hour);

        param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT,0);
        //param.setMargins(0,60,60,60);
        approvalOkImage.setLayoutParams(param);
        approvalOkImage.setImageResource(R.mipmap.ok_green);
        approvalOkImage.setForegroundGravity(Gravity.CENTER_VERTICAL);

        //Set listener for image click
        approvalOkImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setLessonInDatabase();
            }
        });

        addView(approvalOkImage);
        setApprovalOkImageVisibility(false);
    }

    public ImageView getApprovedImage() {
        return approvalOkImage;
    }

    public void setApprovedImage(ImageView approvedImage) {
        this.approvalOkImage = approvedImage;
    }

    public TextView getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName.setText(contactName);
    }

    public TextView getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date.setText(date);
    }

    public TextView getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour.setText(hour);
    }

    public TextView getStatusColor() {
        return statusColor;
    }

    public void setStatusColor(int statusColor) {
        this.statusColor.setBackgroundColor(statusColor);
    }

    public String getLessonId() {
        return id;
    }

    public void setLessonId(String id) {
        this.id = id;
    }

    public TextView getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName.setText(courseName);
    }

    public void setLessonInDatabase(){
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        //Set lesson approved to true at all lessons list
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("lessons").child(this.getLessonId()).child("approved").setValue(true);
        if(currentUserType.equals(UserType.TEACHER))
            mDatabase.child("lessons").child(this.getLessonId()).child("teacherApprove").setValue(true);
        else if(currentUserType.equals(UserType.STUDENT))
            mDatabase.child("lessons").child(this.getLessonId()).child("studentApprove").setValue(true);
    }

    public void setApprovalOkImageVisibility(boolean visible){
        approvalOkImage.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
