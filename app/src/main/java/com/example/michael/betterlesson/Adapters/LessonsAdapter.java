package com.example.michael.betterlesson.Adapters;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.michael.betterlesson.CustomViews.LessonView;
import com.example.michael.betterlesson.Firebase.DatabaseHandler;
import com.example.michael.betterlesson.Logic.Lesson;
import com.example.michael.betterlesson.Logic.UserType;
import com.example.michael.betterlesson.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class LessonsAdapter extends BaseAdapter{

    private ArrayList<Lesson> lessons;
    private Context context;

    //
    private UserType currentUserType;
    private SparseBooleanArray selectedItemsIds;

    //Firebase database reference
    private DatabaseReference mDatabase;

    public LessonsAdapter(Context context, ArrayList<Lesson> lessons, UserType currentUserType){
        this.context = context;
        this.lessons = lessons;
        this.currentUserType = currentUserType;
        this.selectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public int getCount() {
        return lessons.size();
    }

    @Override
    public Object getItem(int position) {
        return lessons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Lesson lesson = (Lesson)getItem(position);
        LessonView lessonView;
        String contactName = (currentUserType.equals(UserType.TEACHER)) ? lesson.getStudentName() : lesson.getTeacherName();
        boolean showApproveImageView = (currentUserType.equals(UserType.TEACHER)) ? !lesson.isTeacherApprove() : !lesson.isStudentApprove();

        if(convertView == null){
            lessonView = new LessonView(context,lesson.getId(),currentUserType);
            lessonView.setContactName(contactName);
            lessonView.setDate(lesson.getDate());
            lessonView.setHour(lesson.getStartingTime());
            lessonView.setCourseName(lesson.getCourseName());
        }else{
            lessonView = (LessonView)convertView;
            lessonView.setContactName(contactName);
            lessonView.setDate(lesson.getDate());
            lessonView.setHour(lesson.getStartingTime());
            lessonView.setCourseName(lesson.getCourseName());
        }

        lessonView.setApprovalOkImageVisibility(false);
        if(lesson.isPassed())
            lessonView.setStatusColor(context.getResources().getColor(R.color.primaryColor));
        else if(!lesson.isTeacherApprove() || !lesson.isStudentApprove()){
            lessonView.setApprovalOkImageVisibility(showApproveImageView);
            lessonView.setStatusColor(context.getResources().getColor(R.color.waitingToApproval));
        }
        else{
            lessonView.setStatusColor(context.getResources().getColor(R.color.secondaryColor));
        }

        return lessonView;
    }

    public void toggleSelection(int position) {
        selectView(position, !selectedItemsIds.get(position));
    }

    public void remove(Lesson lesson) {

        String teacherEmailKey = DatabaseHandler.emailToValidKey(lesson.getTeacherEmail());
        String studentEmailKey = DatabaseHandler.emailToValidKey(lesson.getStudentEmail());

        //remove lesson at teacher lessons list
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("teachers").child(teacherEmailKey).child("lessons").child(lesson.getId()).setValue(null);

        //remove lesson at student lessons list
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("students").child(studentEmailKey).child("lessons").child(lesson.getId()).setValue(null);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("lessons").child(lesson.getId()).setValue(null);

        lessons.remove(lesson);
        notifyDataSetChanged();

    }

    public void removeSelection() {
        selectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value) {
        if (value)
            selectedItemsIds.put(position, value);
        else
            selectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return selectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return selectedItemsIds;
    }
}
