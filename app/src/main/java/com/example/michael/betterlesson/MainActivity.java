package com.example.michael.betterlesson;

import android.content.Intent;
import android.provider.CalendarContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.example.michael.betterlesson.Firebase.DatabaseHandler;
import com.example.michael.betterlesson.Logic.Lesson;
import com.example.michael.betterlesson.Logic.User;
import com.example.michael.betterlesson.Logic.UserType;
import com.example.michael.betterlesson.Tabs.HomeWorkFragment;
import com.example.michael.betterlesson.Tabs.LessonsFragment;
import com.example.michael.betterlesson.Tabs.ContactsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{

    //Activity fore result codes
    private final int ADD_NEW_LESSON_REQUEST = 2;
    private final int WRITE_TO_CALENDAR_REQUEST = 4;

    private static User currentUser;
    private User newContact;
    private String userEmail;
    private static ArrayList<User> contacts;

    //Tabs references
    private TabsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private static FloatingActionButton fab;

    //Firebase references
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private Intent addNewContact;
    private Intent addNewLesson;
    private Intent addNewHw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Bundle extras = getIntent().getExtras();
        userEmail = extras.getString("email");
        boolean fromAddNewContact = extras.getBoolean("fromNewContact");
        if(fromAddNewContact)
            newContact = (User)extras.getSerializable("newContact");

        int iconId = getResources().getIdentifier("ic_people_white_36dp","mipmap","android");
        View view= (View)findViewById(iconId);

        contacts = new ArrayList<User>();

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        tabLayout = (TabLayout)findViewById(R.id.tabs);
        setTabsIconsColors(0);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final String userEmailKey = DatabaseHandler.emailToValidKey(userEmail);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserType type = dataSnapshot.child("users").child(userEmailKey).child("type").getValue(UserType.class);
                String currentUserGroup = type.getTypeGroupName();
                currentUser = dataSnapshot.child(currentUserGroup).child(userEmailKey).getValue(User.class);
                setTabsAccordingToCurrentUser();
                loadContactsFromDatabase();

                mSectionsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
                mViewPager = (ViewPager) findViewById(R.id.container);
                mViewPager.setAdapter(mSectionsPagerAdapter);
                mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
                tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

                //Set action bar title for the first time
                if(currentUser.getType().equals(UserType.TEACHER)){ //The user is teacher
                    toolbar.setTitle(R.string.students_header);

                }else{ //The user is student
                    toolbar.setTitle(R.string.teachers_header);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Set up the ViewPager with the sections adapter.
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                fab.setVisibility(View.VISIBLE);
                switch(tab.getPosition()){
                    case 0:
                        fab.setImageResource(R.mipmap.add_contact_icon);
                        if(currentUser.getType().equals(UserType.TEACHER)){ //The user is teacher
                            toolbar.setTitle(R.string.students_header);

                        }else{ //The user is student
                            toolbar.setTitle(R.string.teachers_header);
                        }
                        break;
                    case 1:
                        fab.setImageResource(R.mipmap.new_add_lesson_icon);
                        toolbar.setTitle(R.string.lessons_header);
                        break;
                    case 2:
                        if(currentUser.getType().equals(UserType.TEACHER))
                            fab.setImageResource(R.mipmap.add_hw_button_icon);
                        else
                            fab.setVisibility(View.GONE);
                        toolbar.setTitle(R.string.hw_header);
                }
                setTabsIconsColors(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        fab = (FloatingActionButton)findViewById(R.id.floating_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(tabLayout.getSelectedTabPosition()){
                    case 0://Contacts tab
                        addNewContact = new Intent(MainActivity.this,NewContactActivity.class);
                        addNewContact.putExtra("currentUser",currentUser);
                        startActivity(addNewContact);
                        break;
                    case 1://Lesson tab
                        addNewLesson = new Intent(MainActivity.this,NewLessonActivity.class);
                        addNewLesson.putExtra("currentUser",currentUser);
                        startActivityForResult(addNewLesson,ADD_NEW_LESSON_REQUEST);
                        break;
                    case 2://H.W tab
                        if(currentUser.getType().equals(UserType.TEACHER)){
                        addNewHw = new Intent(MainActivity.this, NewHwActivity.class);
                        addNewHw.putExtra("currentUser", currentUser);
                        startActivity(addNewHw);
                    }
                        break;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.menu_log_out){
            mAuth.signOut();
            finish();
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            return true;
        }

        if(id == R.id.user_profile){
            Intent userProfileIntent = new Intent(MainActivity.this,UserProfileActivity.class);
            userProfileIntent.putExtra("user",currentUser);
            startActivity(userProfileIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    public class TabsPagerAdapter extends FragmentPagerAdapter {

        public TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:
                    return ContactsFragment.newInstance(currentUser,userEmail,newContact);
                case 1:
                    return LessonsFragment.newInstance(currentUser);
                case 2:
                    return HomeWorkFragment.newInstance(currentUser);
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    public void getCurrentUserFromDatabase(String userEmail) {
        final String userEmailKey = DatabaseHandler.emailToValidKey(userEmail);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserType type = UserType.valueOf(dataSnapshot.child("users").child(userEmailKey).child("type").getValue(String.class));
                String currentUserGroup = type.getTypeGroupName();
                User user = dataSnapshot.child(currentUserGroup).child(userEmailKey).getValue(User.class);
                currentUser = user;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void setTabsAccordingToCurrentUser(){
    }

    public void loadContactsFromDatabase(){
        final String currentUserEmailKey = DatabaseHandler.emailToValidKey(currentUser.getEmail());
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Get current user details
                String currentUserGroupName = currentUser.getType().getTypeGroupName();
                String contactsGroupName = currentUser.getType().getContactsGroupName();

                for(DataSnapshot contact : dataSnapshot.child(currentUserGroupName).child(currentUserEmailKey).child(contactsGroupName).getChildren()){
                    User newContact = contact.getValue(User.class);
                    if(newContact != null)
                        contacts.add(newContact);
                }
                currentUser.setContacts(contacts);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Check which request we're responding to
        if(requestCode == ADD_NEW_LESSON_REQUEST){
            if(resultCode == RESULT_OK){
                Lesson lesson = (Lesson) data.getExtras().getSerializable("newLesson");
                long startMillis = data.getExtras().getLong("startMillis");
                long endMillis= data.getExtras().getLong("endMillis");

                //Add lesson to database
                addNewLessonToDatabase(lesson);
                createCalendarEvent(lesson,startMillis,endMillis);
            }
        }

        if(requestCode == WRITE_TO_CALENDAR_REQUEST){
            if(resultCode == RESULT_OK){

            }
        }
    }

    public void addNewLessonToDatabase(Lesson lesson){
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //Add lesson to lesson path
        String id = mDatabase.push().getKey();
        lesson.setId(id);
        mDatabase.child("lessons").child(id).setValue(lesson);

        //Get teacher and student keys
        final String teacherEmailKey = DatabaseHandler.emailToValidKey(lesson.getTeacherEmail());
        final String studentEmailKey = DatabaseHandler.emailToValidKey(lesson.getStudentEmail());

        //Add lesson to teacher
        if(mDatabase.child("teachers").child(teacherEmailKey) != null &&
                mDatabase.child("teachers").child(teacherEmailKey).child("students").child(studentEmailKey) != null)
            mDatabase.child("teachers").child(teacherEmailKey).child("lessons").child(id).setValue(lesson.getStudentEmail());

        //Add lesson to student
        if(mDatabase.child("students").child(studentEmailKey) != null &&
                mDatabase.child("students").child(studentEmailKey).child("teachers").child(teacherEmailKey) != null)
            mDatabase.child("students").child(studentEmailKey).child("lessons").child(id).setValue(lesson.getTeacherEmail());
    }

    public void createCalendarEvent(Lesson lesson,long startMillis,long endMillis){
        String contactName = (currentUser.getType().equals(UserType.TEACHER)) ? lesson.getStudentName() : lesson.getTeacherName();
        String contactEmail = (currentUser.getType().equals(UserType.TEACHER)) ? lesson.getStudentEmail() : lesson.getTeacherEmail();
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                .putExtra(CalendarContract.Events.TITLE, "Lesson with " + contactName)
                .putExtra(CalendarContract.Events.DESCRIPTION, "Private lesson")
                .putExtra(CalendarContract.Events.EVENT_LOCATION, lesson.getAddress())
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                .putExtra(Intent.EXTRA_EMAIL, contactEmail);
        startActivity(intent);
    }

    public void setTabsIconsColors(int selectedTabPosition){
        switch(selectedTabPosition){
            case 0:
                tabLayout.getTabAt(0).setIcon(R.mipmap.ic_people_white_36dp);
                tabLayout.getTabAt(1).setIcon(R.mipmap.ic_education_dark_36dp);
                tabLayout.getTabAt(2).setIcon(R.mipmap.ic_hw_dark_36dp);
                break;
            case 1:
                tabLayout.getTabAt(0).setIcon(R.mipmap.ic_peopl_dark_36dp);
                tabLayout.getTabAt(1).setIcon(R.mipmap.ic_education_white_36dp);
                tabLayout.getTabAt(2).setIcon(R.mipmap.ic_hw_dark_36dp);
                break;
            case 2:
                tabLayout.getTabAt(0).setIcon(R.mipmap.ic_peopl_dark_36dp);
                tabLayout.getTabAt(1).setIcon(R.mipmap.ic_education_dark_36dp);
                tabLayout.getTabAt(2).setIcon(R.mipmap.ic_hw_white_36dp);
                break;
            default:
                break;
        }
    }

    public static FloatingActionButton getFab(){
        return fab;
    }
}
