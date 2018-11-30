package com.example.walter.mobileapp;

import android.content.Context;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class StaticInstance {

    static FirebaseFirestore db = FirebaseFirestore.getInstance();
    static Context currentActivity;
    static FirebaseDatabase database = FirebaseDatabase.getInstance();



    public static FirebaseFirestore getInstance(){
        return db;
    }
    public static FirebaseDatabase getDatabase(){return  database;}
}
