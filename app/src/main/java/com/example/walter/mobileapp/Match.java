package com.example.walter.mobileapp;

import java.io.Serializable;

public class Match implements Serializable {

    private  String pitchCode;
    private String id;
    private String date;
    private String time;
    private String manager;
    private boolean bookedByMe;

    public Match(){}

    public Match(String id,String date, String time,String manager, String pitchCode, boolean bookedByMe){
        this.id = id;
        this.date = date;
        this.time = time;
        this.manager = manager;
        this.pitchCode = pitchCode;
        this.bookedByMe = bookedByMe;
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
        return ((Match)obj).getId()==id;
    }
}
