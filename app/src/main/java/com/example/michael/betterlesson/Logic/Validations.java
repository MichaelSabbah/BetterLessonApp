package com.example.michael.betterlesson.Logic;

public class Validations {

    public static boolean isEmailValid(String email) {
        return email.contains("@");
    }

    public static boolean isAddressValid(String address){
        return address.isEmpty();
    }

    public static boolean isNameValid(String name){
        return name.isEmpty();
    }

    public static boolean isCourseValid(String course){
        return course.isEmpty();
    }

    public static boolean isPriceValid(String name){
        try{
            double price = Double.parseDouble(name);
            if(price >= 0)
                return true;
            else
                return false;
        }catch (NumberFormatException e){
            return false;
        }
    }
}
