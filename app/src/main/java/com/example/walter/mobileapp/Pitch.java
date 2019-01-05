package com.example.walter.mobileapp;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;

public class Pitch {

    String ownermail;
    String id;
    String address;
    double price;
    Uri uri;
    boolean covered;
    ArrayAdapter availableTime;
    String[] time;
    String city;
    String owner;

    public Pitch(String address, double price, boolean covered, String city) {
        this.address = address;
        this.price = price;
        this.covered = covered;
        uri = null;
        time = new String[15];
        for (int i = 8; i < 23; i++)
            time[i - 8] = String.valueOf(i) + ":00";
        availableTime = new ArrayAdapter(StaticInstance.currentActivity, R.layout.spinneritem, time);
        this.city = city;

    }

    public Pitch(String id, String address, double price, boolean covered, String city, String owner, String ownermail) {
        this.city = city;
        this.id = id;
        this.address = address;
        this.price = price;
        this.owner = owner;
        this.covered = covered;
        this.ownermail = ownermail;
        uri = null;
        time = new String[15];
        for (int i = 8; i < 23; i++)
            time[i - 8] = String.valueOf(i) + ":00";
        availableTime = new ArrayAdapter(StaticInstance.currentActivity, R.layout.spinneritem, time);
    }

    public Pitch(String id, String address, double price, boolean covered, String city, String owner) {
        this.city = city;
        this.id = id;
        this.address = address;
        this.price = price;
        this.owner = owner;
        this.covered = covered;
        uri = null;
        time = new String[15];
        for (int i = 8; i < 23; i++)
            time[i - 8] = String.valueOf(i) + ":00";
        availableTime = new ArrayAdapter(StaticInstance.currentActivity, R.layout.spinneritem, time);
    }

    public String getOwner() {
        return owner;
    }


    public ArrayAdapter getAvailableTime() {
        return availableTime;
    }

    public void removeTime(int remove_time) {
        time[remove_time - 8] = "OCCUPATO";
        availableTime.notifyDataSetChanged();
    }

    public void addTime(int add_time) {
        time[add_time - 8] = String.valueOf(add_time + 8) + ":00";
        availableTime.notifyDataSetChanged();
    }

    public void initWithoutThese(ArrayList<String> notAvailable) {
        for (int i = 8; i < 23; i++)
            if (notAvailable.contains(String.valueOf(i)))
                time[i - 8] = "OCCUPATO";
            else {
                time[i - 8] = String.valueOf(i) + ":00";
            }
        availableTime.notifyDataSetChanged();
    }

    public String getCity() {
        return city;
    }

    public String getId() {
        return id;
    }

    public String getOwnermail() {
        return ownermail;
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

    @Override
    public boolean equals(Object obj) {
        return id.equals(((Pitch) (obj)).id);
    }


}
