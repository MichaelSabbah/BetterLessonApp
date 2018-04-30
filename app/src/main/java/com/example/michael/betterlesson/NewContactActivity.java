package com.example.michael.betterlesson;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.michael.betterlesson.Firebase.DatabaseHandler;
import com.example.michael.betterlesson.Logic.User;
import com.example.michael.betterlesson.Logic.UserType;
import com.example.michael.betterlesson.Logic.Validations;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NewContactActivity extends AppCompatActivity implements TextWatcher {
    private final double STUDENT_NULL_PRICE = -1;

    private User currentUser;
    private User newContact;

    private boolean contactExsist = false;

    //Courses expandable listview
    private ArrayList<String> courses;
    private ArrayList<String> listDataHeader;
    private HashMap<String,List<String>> listDataChild;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    //Views
    private AutoCompleteTextView emailEditText;
    private EditText nameEditText;
    private EditText addressEditText;
    private AutoCompleteTextView coursesEditText;
    private EditText priceEditText;

    private String course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_contact);

        Bundle extras = getIntent().getExtras();
        currentUser = (User)extras.getSerializable("currentUser");

        mAuth = FirebaseAuth.getInstance();

        //Set views correct text (Teacher/Student)
        TextView fillContactText = (TextView)findViewById(R.id.fill_contact_details);

        //Initial references
        emailEditText = (AutoCompleteTextView)findViewById(R.id.email_add_user);
        nameEditText = (EditText)findViewById(R.id.name_add_user);
        addressEditText = (EditText)findViewById(R.id.address_add_user);
        coursesEditText = (AutoCompleteTextView)findViewById(R.id.courses_new_contact);
        priceEditText = (EditText)findViewById(R.id.price_new_contact2);

        //Set auto complete list
        final String  contactsGroupName= currentUser.getType().getContactsGroupName();
        mDatabase = FirebaseDatabase.getInstance().getReference().getRoot().child(contactsGroupName);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                final ArrayList<String> users = new ArrayList<String>();
                for(DataSnapshot userKey : dataSnapshot.getChildren()){
                    if(userKey.hasChild("email")) {
                        String contactEmail = userKey.child("email").getValue(String.class);
                        users.add(contactEmail);
                    }
                }
                //Set adapter (with list) for auto complete text view.
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(NewContactActivity.this,android.R.layout.simple_list_item_1,users);
                emailEditText.setAdapter(adapter);

                //Fill user details, if user was selected from the auto complete list
                emailEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //Get selected user object from database
                        String selectedUserEmail = (String)parent.getItemAtPosition(position);//users.get(position);
                        String selectedUserKey = DatabaseHandler.emailToValidKey(selectedUserEmail);
                        User selectedUser = dataSnapshot.child(selectedUserKey).getValue(User.class);
                        //Fill EditText fields and change their background color
                        emailEditText.setBackgroundResource(R.drawable.filled_round_edit_text);
                        nameEditText.setText(selectedUser.getName());
                        nameEditText.setBackgroundResource(R.drawable.filled_round_edit_text);
                        addressEditText.setText(selectedUser.getAddress());
                        addressEditText.setBackgroundResource(R.drawable.filled_round_edit_text);
                        priceEditText.setText(String.valueOf(selectedUser.getPrice()));
                        priceEditText.setBackgroundResource(R.drawable.filled_round_edit_text);
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        prapareCoursesList();
        ArrayAdapter<String> coursesAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,courses);
        coursesEditText.setAdapter(coursesAdapter);

        if(currentUser.getType().equals(UserType.STUDENT)){
            fillContactText.setText(R.string.fill_teacher_details);
            priceEditText.setVisibility(View.VISIBLE);
        }

        //Set listener for all the EditTexts
        emailEditText.addTextChangedListener(this);
        nameEditText.addTextChangedListener(this);
        addressEditText.addTextChangedListener(this);
        coursesEditText.addTextChangedListener(this);
        priceEditText.addTextChangedListener(this);

        Button addButton = (Button)findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                attemptAddContact();

            }
        });
    }
    public void prapareCoursesList(){
        courses = new ArrayList<String>();
        courses.add("Mathematics");
        courses.add("English");
        courses.add("Programming");
        courses.add("Language");
        courses.add("Computers");
        courses.add("Physics");
        courses.add("Biology");
        courses.add("Chemistry");
    }

    public boolean contactExsist(final String newContactEmail){

        mDatabase.child("users");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String newContactEmailKey = DatabaseHandler.emailToValidKey(newContactEmail);
                if(dataSnapshot.hasChild(newContactEmailKey) && diffrentTypes(newContactEmail))
                    contactExsist = true;
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return contactExsist;
    }

    public boolean diffrentTypes(String newContactEmail){
        return true;
    }

    private void attemptAddContact() {

        // Reset errors.
        emailEditText.setError(null);
        nameEditText.setError(null);
        addressEditText.setError(null);
        priceEditText.setError(null);
        coursesEditText.setError(null);

        // Store values at the time of the login attempt.
        String email = emailEditText.getText().toString();
        String name = nameEditText.getText().toString();
        String address = addressEditText.getText().toString();
        String stringPrice = priceEditText.getText().toString();
        String course = coursesEditText.getText().toString();
        double price = STUDENT_NULL_PRICE;

        boolean cancel = false;
        View focusView = null;


        //Check if price is not empty
        if(currentUser.getType().equals(UserType.STUDENT)) {
            if (TextUtils.isEmpty(stringPrice) || !Validations.isPriceValid(stringPrice)) {
                priceEditText.setError(getString(R.string.error_field_required));
                focusView = priceEditText;
                cancel = true;
            }
        }

        //Check if course is not empty
        if(TextUtils.isEmpty(course) || Validations.isCourseValid(course)){
            coursesEditText.setError(getString(R.string.error_field_required));
            focusView = coursesEditText;
            cancel = true;
        }

        //Check if address is not empty
        if(TextUtils.isEmpty(address) || Validations.isAddressValid(address)){
            addressEditText.setError(getString(R.string.error_field_required));
            focusView = addressEditText;
            cancel = true;
        }

        //Check if name is not empty
        if(TextUtils.isEmpty(name) || Validations.isNameValid(name)){
            nameEditText.setError(getString(R.string.error_field_required));
            focusView = nameEditText;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.error_field_required));
            focusView = emailEditText;
            cancel = true;
        } else if (!Validations.isEmailValid(email)) {
            emailEditText.setError(getString(R.string.error_invalid_email));
            focusView = emailEditText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {

            if(currentUser.getType().equals(UserType.STUDENT))
                price = Double.parseDouble(stringPrice);
            if (contactExsist(email)) {//The new contact have the app so his details already exsists.

                //Get new contact from database
                final String newContactKey = DatabaseHandler.emailToValidKey(email);
                final String newContactGroup = currentUser.getType().getContactsGroupName();
                mDatabase = FirebaseDatabase.getInstance().getReference().child(newContactGroup);

                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        newContact = dataSnapshot.getValue(User.class);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } else {//The new contact don't have the app, create new one. (The new contact must be student)
                UserType contactType = (currentUser.getType().equals(UserType.TEACHER)) ? UserType.STUDENT : UserType.TEACHER;
                newContact = new User(name, email, address,course, price, contactType);
            }
            String currentUserEmailKey = DatabaseHandler.emailToValidKey(currentUser.getEmail());
            String newContactEmailKey = DatabaseHandler.emailToValidKey(email);
            String currentUserGroup = currentUser.getType().getTypeGroupName();
            String contactsGroupName = currentUser.getType().getContactsGroupName();
            mDatabase.getRoot().child(currentUserGroup).child(currentUserEmailKey).child(contactsGroupName).child(newContactEmailKey).setValue(newContact);
            mDatabase.getRoot().child(currentUserGroup).child(currentUserEmailKey).child(contactsGroupName).child(newContactEmailKey).child("course").setValue(course);
            Intent mainActivityIntent = new Intent(NewContactActivity.this, MainActivity.class);
            mainActivityIntent.putExtra("fromNewContact",true);
            mainActivityIntent.putExtra("email",currentUser.getEmail());
            mainActivityIntent.putExtra("newContact", newContact);
            startActivity(mainActivityIntent);
            finish();
        }
    }

    //Listener to change background of empty EditTexts
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(s.toString().isEmpty()){
            getCurrentFocus().setBackgroundResource(R.drawable.round_edit_text);
        }else{
            getCurrentFocus().setBackgroundResource(R.drawable.filled_round_edit_text);
        }
    }
}
