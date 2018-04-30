package com.example.michael.betterlesson;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.michael.betterlesson.Adapters.ContactsAdapter;
import com.example.michael.betterlesson.Logic.Lesson;
import com.example.michael.betterlesson.Logic.User;
import com.example.michael.betterlesson.Logic.UserType;

import java.io.Serializable;
import java.util.Calendar;

public class NewLessonActivity extends AppCompatActivity {

    //Logic references
    private static User currentUser;
    private static User selectedContact;
    private static Intent saveEventIntent;

    //Views
    private TextView selectContactText;
    private ListView contactsList;

    //Date & Time variables
    private static int lessonYear, lessonMonth, lessonDay;
    private static int lessonsStartingHour, lessonStartingMinute, lessonEndingHour, lessonEndingMinute;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_lesson);

        Bundle extras = getIntent().getExtras();
        currentUser = (User) extras.getSerializable("currentUser");

        //Get references
        selectContactText = (TextView) findViewById(R.id.select_contact_text);
        contactsList = (ListView) findViewById(R.id.students_list_view_for_lesson);

        //Set correct header (Teacher/Student)
        if (currentUser.getType().equals(UserType.STUDENT))
            selectContactText.setText(R.string.select_teacher);

        //Set students list adapter
        ContactsAdapter contactsAdapter = new ContactsAdapter(this, currentUser.getContacts());
        contactsAdapter.notifyDataSetChanged();
        contactsList.setAdapter(contactsAdapter);
        contactsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedContact = (User) contactsList.getItemAtPosition(position);

                //Show the date picker
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });
    }

    //Pickers fragments class
    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker

            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH) + 1;
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);

        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

            lessonYear = year;
            lessonMonth = month;
            lessonDay = dayOfMonth;

            //Show time picker fragment for starting hour and minute
            DialogFragment startingTimePicker = new StartingTimePicker();
            startingTimePicker.show(getFragmentManager(), "timePickerStarting");
        }
    }

    public static class StartingTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfDay) {
            lessonsStartingHour = hourOfDay;
            lessonStartingMinute = minuteOfDay;

            //Show ending time picker fragment
            DialogFragment endingTimePicker = new EndingTimePicker();
            endingTimePicker.show(getFragmentManager(), "endingTimePicker");
        }
    }

    public static class EndingTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfDay) {
            lessonEndingHour = hourOfDay;
            lessonEndingMinute = minuteOfDay;


            //Create new lesson
            //Get teacher and student details
            String teacherEmail = currentUser.getType().equals(UserType.TEACHER) ? currentUser.getEmail() : selectedContact.getEmail();
            String studentEmail = currentUser.getType().equals(UserType.TEACHER) ? selectedContact.getEmail() : currentUser.getEmail();
            String teacherName = currentUser.getType().equals(UserType.TEACHER) ? currentUser.getName() : selectedContact.getName();
            String studentName = currentUser.getType().equals(UserType.TEACHER) ? selectedContact.getName() : currentUser.getName();
            ;
            String contactEmail = (currentUser.getType().equals(UserType.TEACHER)) ? studentEmail : teacherEmail;
            boolean teacherApprove = currentUser.getType().equals(UserType.TEACHER) ? true : false;


            //Set date and time format
            String date = getDateFormat(lessonYear, lessonMonth, lessonDay);
            String startingTime = getTimeFormat(lessonsStartingHour, lessonStartingMinute);
            String endingTime = getTimeFormat(lessonEndingHour, lessonEndingMinute);
            Lesson newLesson = new Lesson(currentUser.getContactByEmail(contactEmail).getCourse(), currentUser.getAddress(),
                    teacherEmail, studentEmail, teacherName, studentName, date,
                    startingTime, endingTime, false, teacherApprove, !teacherApprove, false);


            //Create event on the device calendar
            long startMillis = 0;
            long endMillis = 0;
            Calendar beginTime = Calendar.getInstance();
            beginTime.set(lessonYear, lessonMonth - 1, lessonDay, lessonsStartingHour, lessonStartingMinute);
            startMillis = beginTime.getTimeInMillis();
            Calendar endTime = Calendar.getInstance();
            endTime.set(lessonYear, lessonMonth - 1, lessonDay, lessonEndingHour, lessonEndingMinute);
            endMillis = endTime.getTimeInMillis();

            //Create main activity intent
            Intent returnIntent = new Intent();
            returnIntent.putExtra("newLesson", (Serializable) newLesson);
            returnIntent.putExtra("startMillis",startMillis);
            returnIntent.putExtra("endMillis",endMillis);
            getActivity().setResult(Activity.RESULT_OK, returnIntent);
            getActivity().finish();
        }
    }

    public static String getDateFormat(int year, int month, int day) {
        return day + "/" + month + "/" + year;
    }

    public static String getTimeFormat(int hour, int minute) {
        String strHour = String.valueOf(hour);
        String strMinute = String.valueOf(minute);
        if (strHour.length() == 1)
            strHour = "0" + strHour;
        if (strMinute.length() == 1)
            strMinute = strMinute + "0";
        return strHour + ":" + strMinute;
    }

}


