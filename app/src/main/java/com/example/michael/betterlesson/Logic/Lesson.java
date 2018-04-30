package com.example.michael.betterlesson.Logic;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Lesson implements Serializable,Comparable<Lesson> {
    private String id;
    private String courseName;
    private String address;
    private String teacherEmail;
    private String studentEmail;
    private String teacherName;
    private String studentName;
    private String date;
    private String startingTime;
    private String endingTime;
    private boolean approved; //If lesson approved by the teacher
    private boolean teacherApprove;
    private boolean studentApprove;
    private boolean passed;

    public Lesson(){

    }

    public Lesson(String courseName,String address,String teacherEmail,String studentEmail,String teacherName,String studentName,String date,
                  String startingTime,String endingTime,boolean approved,boolean teacherApprove,boolean studentApprove,boolean passed){
        this.courseName = courseName;
        this.address = address;
        this.teacherEmail = teacherEmail;
        this.studentEmail = studentEmail;
        this.teacherName = teacherName;
        this.studentName = studentName;
        this.date = date;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.approved = approved;
        this.teacherApprove = teacherApprove;
        this.studentApprove = studentApprove;
        this.passed = passed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(String startingTime) {
        this.startingTime = startingTime;
    }

    public String getEndingTime() {
        return endingTime;
    }

    public void setEndingTime(String endingTime) {
        this.endingTime = endingTime;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
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

    public boolean isTeacherApprove() {
        return teacherApprove;
    }

    public void setTeacherApprove(boolean teacherApprove) {
        this.teacherApprove = teacherApprove;
    }

    public boolean isStudentApprove() {
        return studentApprove;
    }

    public void setStudentApprove(boolean studentApprove) {
        this.studentApprove = studentApprove;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public int compareTo(@NonNull Lesson other) {
        SimpleDateFormat totalTimeFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm") ;
        int result = 0;
        Date currentLessonDate = null;
        Date otherLessonDate = null;
        try{
            currentLessonDate = totalTimeFormat.parse(this.getDate() +" "+ this.getStartingTime());
            otherLessonDate= totalTimeFormat.parse(other.getDate() +" "+ other.getStartingTime());
        }catch(java.text.ParseException e){
            e.printStackTrace();
        }
        if(currentLessonDate.after(otherLessonDate))
            result = -1;
        else if(currentLessonDate.before(otherLessonDate))
            result = 1;
        else
            result = 0;
        return result;
    }
}
