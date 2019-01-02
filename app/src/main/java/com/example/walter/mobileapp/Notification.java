package com.example.walter.mobileapp;

public class Notification {
    String id;
    String from;
    String to;
    String state;
    String info_match;

    public Notification(String id, String from, String to, String state, String info_match) {
        this.from = from;
        this.to = to;
        this.state = state;
        this.info_match = info_match;
        this.id = id;
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
        return notification.from.equals(from) && notification.to.equals(to) && notification.info_match.equals(info_match);
    }
}
