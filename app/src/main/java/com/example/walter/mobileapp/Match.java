package com.example.walter.mobileapp;

import java.io.Serializable;

public class Match implements Serializable {

    private String date;
    private String time;
    private String address;
    private boolean covered;
    private String manager;
    private String howManyRegistered;

    public Match(){}

    public Match(String date, String time, String address, boolean isCovered, String manager, String howManyRegistered){
        this.date = date;
        this.time = time;
        this.address = address;
        covered = isCovered;
        this.manager = manager;
    }

    public Match(Match newMatch){
        date = newMatch.date;
        time = newMatch.time;
        address = newMatch.address;
        covered = newMatch.covered;
        manager = newMatch.manager;
        howManyRegistered = newMatch.howManyRegistered;
    }


    public String getAddress() {
        return address;
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

    public void setCovered(boolean covered) {
        this.covered = covered;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setHowManyRegistered(String howManyRegistered) {
        this.howManyRegistered = howManyRegistered;
    }

    public String getHowManyRegistered() {
        return howManyRegistered;
    }

    public boolean isCovered() {
        return covered;
    }

}
