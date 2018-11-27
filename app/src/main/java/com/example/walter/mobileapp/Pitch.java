package com.example.walter.mobileapp;

import android.net.Uri;

public class Pitch {

    String address;
    double price;
    Uri uri;
    boolean covered;

    public Pitch(String address,double price,boolean covered){
        this.address=address;
        this.price=price;
        this.covered=covered;
        uri = null;
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
