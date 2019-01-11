package com.example.walter.mobileapp;

import android.content.Context;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

//usata per accedere staticamente alle connessioni e ai dati usati in tutta l'applicazione
public class StaticInstance {

    static FirebaseFirestore db = FirebaseFirestore.getInstance();
    static Context currentActivity;
    static FirebaseDatabase database = FirebaseDatabase.getInstance();
    static String username;
    static String role;
    static String email;
    static boolean fblogged = false;
    static StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    static boolean notificationEnabled = true;
    static boolean sound = true;
    static boolean calendarEvent = true;
    static boolean wantToRateMatch = true;
    static boolean wantToSendMail = true;
    




    public static FirebaseFirestore getInstance(){
        return db;
    }
    public static FirebaseDatabase getDatabase(){return  database;}
}
