package com.example.walter.mobileapp;

public class Notification {
    String id;
    String from;
    String to;
    String state;
    String info_match;
    String match;
    String team;
    String role;
    String date;
    String time;
    boolean covered;
    String address;

    public Notification(String id, String from, String to, String state, String info_match, String match, String role, String team, String date, String time, boolean covered, String address) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.state = state;
        this.info_match = info_match;
        this.match = match;
        this.team = team;
        this.role = role;
        this.date = date;
        this.time = time;
        this.covered = covered;
        this.address = address;
    }


    public String getTeam() {
        return team;
    }

    public String getRole() {
        return role;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public String getInfo_match() {
        return info_match;
    }

    public String getState() {
        return state;
    }

    public String getTo() {
        return to;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setInfo_match(String info_match) {
        this.info_match = info_match;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public boolean equals( Object obj) {
        Notification notification = (Notification)obj;
        return notification.id.equals(id);
    }

    public String getMatch() {
        return match;
    }

    public String getAddress() {
        return address;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

    public void setCovered(boolean covered) {
        this.covered = covered;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isCovered() {
        return covered;
    }
}
