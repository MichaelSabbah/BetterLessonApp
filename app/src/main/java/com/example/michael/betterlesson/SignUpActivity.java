package com.example.michael.betterlesson;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.michael.betterlesson.Firebase.DatabaseHandler;
import com.example.michael.betterlesson.Logic.UserType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    //Logic references
    private String name;

    //Firebase references
    private FirebaseAuth mAuth;

    //UI references
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText addressEditText;
    private EditText priceEditText;
    private RadioGroup typeRadioGroup;
    private View signUpFormView;
    private View progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        nameEditText = (EditText)findViewById(R.id.name);
        emailEditText = (EditText)findViewById(R.id.email);
        passwordEditText = (EditText)findViewById(R.id.password);
        addressEditText = (EditText)findViewById(R.id.address);
        priceEditText = (EditText)findViewById(R.id.price);
        typeRadioGroup = (RadioGroup)findViewById(R.id.radio_profile_type);

        Button signUpButton = (Button)findViewById(R.id.sign_up_button);
        signUpButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                attemptSignUp();
            }
        });

        typeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.radio_student)
                    priceEditText.setVisibility(View.GONE);
                else
                    priceEditText.setVisibility(View.VISIBLE);
            }
        });

        signUpFormView = findViewById(R.id.sign_up_form);
        progressView = findViewById(R.id.sign_up_progress);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
            startActivity(new Intent(SignUpActivity.this,MainActivity.class));
    }

    private void attemptSignUp(){

        // Reset errors.
        emailEditText.setError(null);
        passwordEditText.setError(null);

        // Store values at the time of the sign up attempt.
        String name = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String address = addressEditText.getText().toString();
        UserType type = UserType.valueOf(getType(typeRadioGroup).toUpperCase());
        String stringPrice = priceEditText.getText().toString();
        double price = -1; //Student dont need to enter price so if it's student his price will be -1.
        if(type.equals(UserType.TEACHER))
            if(!priceEditText.getText().toString().isEmpty())
                price = Double.parseDouble(priceEditText.getText().toString());

        boolean cancel = false;
        View focusView = null;

        //Check if name is not empty
        if(TextUtils.isEmpty(name)){
            nameEditText.setError(getString(R.string.error_field_required));
            focusView = nameEditText;
            cancel = true;
        }

        //Check if email is valid and not empty
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.error_field_required));
            focusView = emailEditText;
            cancel = true;
        } else if (!isEmailValid(email)) {
            emailEditText.setError(getString(R.string.error_invalid_email));
            focusView = emailEditText;
            cancel = true;
        }

        //Check if password is valid and not empty
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            passwordEditText.setError(getString(R.string.error_invalid_password));
            focusView = passwordEditText;
            cancel = true;
        }

        //Check if address is empty
        if(TextUtils.isEmpty(address)){
            addressEditText.setError(getString(R.string.error_field_required));
            focusView = addressEditText;
            cancel = true;
        }

        //Check if price is empty
        if(type.equals(UserType.TEACHER) && TextUtils.isEmpty(stringPrice)){
            priceEditText.setError(getString(R.string.error_field_required));
            focusView = priceEditText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            startSignUp(name,email,password,address,price,type);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        Button signUpButton = (Button)findViewById(R.id.sign_up_button);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            signUpButton.setVisibility(show ? View.GONE : View.VISIBLE);
            signUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            signUpFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    signUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });


            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            signUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            signUpButton.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void startSignUp(final String name,final String email,String password,final String address,final double price,final UserType type){

        //Firebase sign up
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            //Set user name
                            setFirebaseUserName(user,name);

                            //Insert into database
                            DatabaseHandler.addUserToDatabase(name,email,address,price,type);

                            Intent mainActivityIntent = new Intent(SignUpActivity.this,MainActivity.class);
                            mainActivityIntent.putExtra("email",email);
                            mainActivityIntent.putExtra("fromNewContact",false);
                            startActivity(mainActivityIntent);
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            showProgress(false);
                        }
                    }
                });
    }
    private String getType(RadioGroup radioGroup){
        int radioId = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(radioId);
        return radioButton.getText().toString();
    }

    //Firebase methods
    private void setFirebaseUserName(FirebaseUser user,String name){
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name).build();
        user.updateProfile(profileUpdates);
    }

}
