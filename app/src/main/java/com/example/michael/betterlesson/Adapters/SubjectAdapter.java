package com.example.michael.betterlesson.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import com.example.michael.betterlesson.CustomViews.SubjectView;

import java.util.ArrayList;

/**
 * Created by Michael on 15/03/2018.
 */

public class SubjectAdapter extends BaseAdapter {

    private ArrayList<String> subjects;
    private Context context;
    private String lessonId;

    public SubjectAdapter(Context context,String lessonId,ArrayList<String> subjects){
        this.context = context;
        this.lessonId = lessonId;
        this.subjects = subjects;
    }

    @Override
    public int getCount() {
        return subjects.size();
    }

    @Override
    public Object getItem(int position) {
        return subjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String subject = (String)getItem(position);
        SubjectView subjectView;

        if(convertView == null){
            subjectView = new SubjectView(context,lessonId,subject);
        }else{
            subjectView = (SubjectView)convertView;
        }

        subjectView.setSubject(subject);
        return subjectView;
    }
}
