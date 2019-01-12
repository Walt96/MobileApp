package com.example.walter.mobileapp.object;

import android.net.Uri;

public class Player {

    private String role;
    private String team;
    private String username;
    private Uri uri;
    private int rate;

    public Player(String role, String team, String username, Uri uri) {
        this.role = role;
        this.team = team;
        this.username = username;
        this.uri = uri;
        this.rate = 0;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public int getRate() {
        return rate;
    }

    public String getRole() {
        return role;
    }

    public String getTeam() {
        return team;
    }

    public String getUsername() {
        return username;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
