package com.example.michael.betterlesson.Logic;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable{
    private String name;
    private String email;
    private String address;
    private String course;
    private double price;
    private UserType type;
    private String userType;
    private ArrayList<User> contacts;

    public User(){

    }

    public User(String name,String email,String address,String course,double price,UserType type){
        this.name = name;
        this.email = email;
        this.address = address;
        this.price = price;
        this.type = type;
        this.course = course;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public UserType getType() {
        return type;
    }

    public void setType(UserType type) {
        this.type = type;
    }

    public ArrayList<User> getContacts(){
        return contacts;
    }

    public void setContacts(ArrayList<User> contacts) {
        this.contacts = contacts;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public User getContactByEmail(String contactEmail){
        for(User contact : getContacts()){
            if(contact.getEmail().equals(contactEmail))
                return contact;
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {// Two users equals if they have the same email and type
        User otherUser = (User)obj;
        return this.getEmail().equals(otherUser.getEmail()) && this.getType().equals(otherUser.getType());
    }
}

