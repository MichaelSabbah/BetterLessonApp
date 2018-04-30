package com.example.michael.betterlesson.Logic;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HW implements Serializable,Comparable<HW>{
    private String hwId;
    private String date;
    private String teacherName;
    private String studentName;
    private String teacherEmail;
    private String studentEmail;
    private String courseName;
    private String uri;
    private String url;
    private boolean expired;

    public HW(){

    }

    public HW(String hwId, String date, String teacherName, String studentName,
              String teacherEmail, String studentEmail,String courseName,String uri,String url,boolean expired) {
        this.hwId = hwId;
        this.date = date;
        this.teacherName = teacherName;
        this.studentName = studentName;
        this.teacherEmail = teacherEmail;
        this.studentEmail = studentEmail;
        this.courseName = courseName;
        this.uri = uri;
        this.url = url;
        this.expired = expired;
    }

    public String getHwId() {
        return hwId;
    }

    public void setHwId(String hwId) {
        this.hwId = hwId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getTeacherEmail() {
        return teacherEmail;
    }

    public void setTeacherEmail(String teacherEmail) {
        this.teacherEmail = teacherEmail;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int compareTo(@NonNull HW other) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy") ;
        int result = 0;
        Date hwDate = null;
        Date otherHwDate = null;
        try{
            hwDate = dateFormat.parse(this.getDate() +" "+ this.getDate());
            otherHwDate = dateFormat.parse(other.getDate() +" "+ other.getDate());
        }catch(java.text.ParseException e){
            e.printStackTrace();
        }
        if(hwDate.after(otherHwDate))
            result = -1;
        else if(hwDate.before(otherHwDate))
            result = 1;
        else
            result = 0;
        return result;
    }
}
