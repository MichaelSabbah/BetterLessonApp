package com.example.michael.betterlesson.Firebase;

import android.content.Context;

import com.example.michael.betterlesson.Logic.User;
import com.example.michael.betterlesson.Logic.UserType;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DatabaseHandler {

    private static DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();;
    private final Context context;

    public DatabaseHandler(Context context){
        this.context = context;
    }

    public static void addUserToDatabase(String name,String email,String address,double price,UserType type){
        String emailKey = emailToValidKey(email);
        User user = new User(name,email,address,"",price,type);

         mDatabase.child("users").child(emailKey).child("type").setValue(type);
         mDatabase.child(type.getTypeGroupName()).child(emailKey).setValue(user);
    }

    public static String emailToValidKey(String email){
        String emailKey = email.replace(".","_dot_");
        return emailKey ;
    }

    public static void removeAllLessonsWithContact(User contact){
        String currentUserEmailKey = emailToValidKey(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        String currentUserGroup = contact.getType().getContactsGroupName(); //The current user is in the opposite group
        final String contactUserEmail = contact.getEmail();
        mDatabase = FirebaseDatabase.getInstance().getReference().child(currentUserGroup).child(currentUserEmailKey).child("lessons");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot lesson : dataSnapshot.getChildren()){
                    if(lesson.getValue().equals(contactUserEmail)){
                        lesson.getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
