package com.example.michael.betterlesson;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.michael.betterlesson.Logic.User;

public class UserProfileActivity extends AppCompatActivity {

    //Logic reference
    private User user;

    //UI references
    private TextView profileType;
    private TextView userName;
    private TextView userEmail;
    private TextView userAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Bundle extras = getIntent().getExtras();
        user = (User)extras.getSerializable("user");

        //Get views
        profileType = (TextView)findViewById(R.id.profile_type);
        userName = (TextView)findViewById(R.id.user_profile_name);
        userEmail = (TextView)findViewById(R.id.profile_email);
        userAddress = (TextView)findViewById(R.id.profile_address);

        //Set views
        profileType.setText(user.getType().getProfileUserType());
        userName.setText(user.getName());
        userEmail.setText(user.getEmail());
        userAddress.setText(user.getAddress());

    }
}
