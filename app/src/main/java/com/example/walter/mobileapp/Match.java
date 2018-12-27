package com.example.walter.mobileapp;

import java.io.Serializable;
import java.util.ArrayList;

public class Match implements Serializable {

    private boolean isFinished;
    private  String pitchCode;
    private String pitchOwner;
    private String id;
    private String date;
    private String time;
    private String manager;
    private boolean bookedByMe;
    private ArrayList partecipants;
    private ArrayList registered;
    private String address;
    private boolean isCovered;

    public Match(){}

    public Match(String id, String date, String time, String manager, String address, ArrayList registered, boolean covered) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.manager = manager;
        this.address = address;
        this.registered = registered;
        this.isCovered = covered;
    }

    public Match(String id,String date, String time,String manager, String pitchCode, boolean bookedByMe,ArrayList partecipants, ArrayList registered, boolean isCovered, String address, String pitchOwner, boolean isFinished){
        this.id = id;
        this.isFinished = isFinished;
        this.date = date;
        this.time = time;
        this.manager = manager;
        this.pitchCode = pitchCode;
        this.bookedByMe = bookedByMe;
        this.partecipants = partecipants;
        this.registered = registered;
        this.isCovered = isCovered;
        this.address = address;
        this.pitchOwner = pitchOwner;
    }

    public String getPitchOwner() {
        return pitchOwner;
    }

    public void setPitchOwner(String pitchOwner) {
        this.pitchOwner = pitchOwner;
    }

    public boolean isCovered() {
        return isCovered;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public String getAddress() {
        return address;
    }

    public ArrayList getPartecipants() {
        return partecipants;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCovered(boolean covered) {
        isCovered = covered;
    }

    public void setPartecipants(ArrayList partecipants) {
        this.partecipants = partecipants;
    }


    public void setRegistered(ArrayList registered) {
        this.registered = registered;
    }

    public ArrayList getRegistered() {
        return registered;
    }

    public void setBookedByMe(boolean bookedByMe) {
        this.bookedByMe = bookedByMe;
    }

    public boolean isBookedByMe() {
        return bookedByMe;
    }

    public String getId() {
        return id;
    }

    public String getPitchCode() {
        return pitchCode;
    }

    public String getDate() {
        return date;
    }

    public String getManager() {
        return manager;
    }

    public String getTime() {
        return time;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPitchCode(String pitchCode) {
        this.pitchCode = pitchCode;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public boolean equals( Object obj) {
        return ((Match)obj).getId().equals(id);
    }
}
