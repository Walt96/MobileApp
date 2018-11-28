package com.example.walter.mobileapp;

import android.net.Uri;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class Pitch {

    String address;
    double price;
    Uri uri;
    boolean covered;
    ArrayAdapter availableTime;
    String[] time;
    public Pitch(String address,double price,boolean covered){
        this.address=address;
        this.price=price;
        this.covered=covered;
        uri = null;
        time = new String[15];
        for(int i = 8;i<23;i++)
            time[i-8]=String.valueOf(i);
        availableTime  = new ArrayAdapter(StaticInstance.currentActivity,R.layout.spinneritem,time);
    }

    public ArrayAdapter getAvailableTime() {
        return availableTime;
    }

    public void removeTime(int remove_time){
        time[remove_time] = "OCCUPATO";
        availableTime.notifyDataSetChanged();
    }

    public void addTime(int add_time){
        time[add_time] = String.valueOf(add_time+8);
        availableTime.notifyDataSetChanged();
    }

    public void initWithoutThese(ArrayList<String> notAvailable){
        for(int i = 8;i<23;i++)
            if(notAvailable.contains(String.valueOf(i)))
                time[i-8] = "OCCUPATO";
            else{
                time[i-8] = String.valueOf(i);
            }
        availableTime.notifyDataSetChanged();
    }

    public Uri getUri() {
        return uri;
    }

    public String getAddress() {
        return address;
    }

    public double getPrice() {
        return price;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCovered(boolean covered) {
        this.covered = covered;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isCovered() {
        return covered;
    }
}
