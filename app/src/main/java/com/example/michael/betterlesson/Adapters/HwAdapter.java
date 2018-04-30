package com.example.michael.betterlesson.Adapters;

import android.content.Context;
import android.media.Image;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.michael.betterlesson.Firebase.DatabaseHandler;
import com.example.michael.betterlesson.Logic.HW;
import com.example.michael.betterlesson.Logic.User;
import com.example.michael.betterlesson.Logic.UserType;
import com.example.michael.betterlesson.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Michael on 13/03/2018.
 */

public class HwAdapter extends BaseAdapter{

    final long ONE_MEGABYTE = 4096 * 4096;

    //Logic references
    private Context context;
    private ArrayList<HW> hwList;
    private UserType currentUserType;
    private SparseBooleanArray selectedItemsIds;

    //Firebase references
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private DatabaseReference mDatabase;
    private StorageReference pdfRef;

    //Files counter
    private static int fileNumber = 0;

    public HwAdapter(Context context, ArrayList<HW> hwList, UserType currentUserType){
        this.context = context;
        this.hwList = hwList;
        this.currentUserType= currentUserType;
        this.selectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public int getCount() {
        return hwList.size();
    }

    @Override
    public Object getItem(int position) {
        return hwList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        HW hw = (HW)getItem(position);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //View view = layoutInflater.inflate(R.layout.custome_view,null);
        String contactName = currentUserType.equals(UserType.TEACHER) ? hw.getStudentName() : hw.getTeacherName();

        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.hw_view,null);
        }

        //Get views
        TextView contact = (TextView)convertView.findViewById(R.id.uploaded_by_text);
        TextView course = (TextView)convertView.findViewById(R.id.course_text);
        TextView date = (TextView)convertView.findViewById(R.id.expiration_date);
        ImageView expirationImage = (ImageView)convertView.findViewById(R.id.expiration_image);
        //Set views
        contact.setText(contactName);
        course.setText(hw.getCourseName());
        date.setText(hw.getDate());
        if(hw.isExpired())
            expirationImage.setVisibility(View.VISIBLE);
        else
            expirationImage.setVisibility(View.GONE);


        return convertView;
    }

    public void remove(HW hw) {

        String teacherEmailKey = DatabaseHandler.emailToValidKey(hw.getTeacherEmail());
        String studentEmailKey = DatabaseHandler.emailToValidKey(hw.getStudentEmail());

        if(currentUserType.equals(UserType.TEACHER)) {//remove hw from teacher lessons list only if the teacher deleted the file
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("teachers").child(teacherEmailKey).child(hw.getHwId()).setValue(null);

            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child(hw.getHwId()).setValue(null);
        }

        //remove hw from student lessons list
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("students").child(studentEmailKey).child(hw.getHwId()).setValue(null);

        hwList.remove(hw);
        notifyDataSetChanged();

    }

    public void toggleSelection(int position) {
        selectView(position, !selectedItemsIds.get(position));
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

    /*public int getSelectedCount() {
        return selectedItemsIds.size();
    }*/

    public SparseBooleanArray getSelectedIds() {
        return selectedItemsIds;
    }
}
