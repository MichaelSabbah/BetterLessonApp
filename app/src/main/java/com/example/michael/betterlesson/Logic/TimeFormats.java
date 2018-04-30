package com.example.michael.betterlesson.Logic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeFormats {

    public static String getDateFormat(int year,int month,int day){
        return day+"/"+month+"/"+year;
    }

    public static String getTimeFormat(int hour,int minute){
        String strHour = String.valueOf(hour);
        String strMinute = String.valueOf(minute);
        if(strHour.length() == 1)
            strHour = "0"+strHour;
        if(strMinute.length() == 1)
            strMinute = strMinute+"0";
        return strHour+":"+strMinute;
    }

    public static boolean isLessonPassed(Lesson lesson){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm") ;
        Date lessonTime = null;
        Date currentTime = null;

        //Current date
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        //Current time
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        String strDate = getDateFormat(year,month,day);
        String strTime = getTimeFormat(hour,minute);

        try {
            lessonTime = timeFormat.parse(lesson.getDate() +" "+ lesson.getStartingTime());
            currentTime = timeFormat.parse(strDate +" "+ strTime);
        }catch (ParseException e){
            e.printStackTrace();
        }

        if(currentTime.after(lessonTime))
            return true;
        return false;
    }

}
