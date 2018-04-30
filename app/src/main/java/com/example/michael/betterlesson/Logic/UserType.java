package com.example.michael.betterlesson.Logic;

public enum UserType {
    TEACHER,STUDENT;

    public String getTypeGroupName(){
        switch (this){
            case TEACHER:
                return "teachers";
            case STUDENT:
                return "students";
        }
        return null;
    }

    public String getContactsGroupName(){
        switch (this){
            case TEACHER:
                return "students";
            case STUDENT:
                return "teachers";
        }
        return null;
    }

    public String getProfileUserType(){
        switch (this){
            case TEACHER:
                return "Teacher";
            case STUDENT:
                return "Student";
        }
        return null;
    }
}
