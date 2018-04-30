package com.example.michael.betterlesson.Adapters;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.michael.betterlesson.Firebase.DatabaseHandler;
import com.example.michael.betterlesson.Logic.User;
import com.example.michael.betterlesson.CustomViews.UserView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ContactsAdapter extends BaseAdapter {
    private DatabaseReference mDatabase;
    private ArrayList<User> students;
    private Context context;
    private SparseBooleanArray selectedItemsIds;

    public ContactsAdapter(Context context, ArrayList<User> students){
        super();
        this.students = students;
        this.context = context;
        this.mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        this.selectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public int getCount() {
        return students.size();
    }

    @Override
    public Object getItem(int position) {
        return students.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final User contact = (User)getItem(position);
        final String contactKey = DatabaseHandler.emailToValidKey(contact.getEmail());
        final UserView userView;

        if(students != null){
            if (convertView == null) {
                userView = new UserView(context);
                userView.setName(contact.getName());

            } else {
                userView = (UserView) convertView;
                userView.setName(contact.getName());
            }
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(contactKey))
                    userView.setAppLogoVisibility(true);
                else
                    userView.setAppLogoVisibility(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
            return userView;
        }
        return null;
    }

    public void toggleSelection(int position) {
        selectView(position, !selectedItemsIds.get(position));
    }

    public void remove(User contact) {
        //Delete from firebase instead
        String currentUserEmailKey = DatabaseHandler.emailToValidKey(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        String currentUserGroup= contact.getType().getContactsGroupName();
        String contactsGroup = contact.getType().getTypeGroupName();
        String contactEmailKey = DatabaseHandler.emailToValidKey(contact.getEmail());

        //remove contact from current user
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(currentUserGroup).child(currentUserEmailKey).child(contactsGroup).child(contactEmailKey).setValue(null);

        //remove all lesson with this contact
        DatabaseHandler.removeAllLessonsWithContact(contact);
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
