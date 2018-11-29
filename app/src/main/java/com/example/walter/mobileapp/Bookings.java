package com.example.walter.mobileapp;

import java.util.ArrayList;
import java.util.HashMap;

public class Bookings {

    ArrayList<Booking> bookings;

    public Bookings(){
        bookings=new ArrayList<>();
    }

    public Bookings(ArrayList<Booking> bookings){
        this.bookings=bookings;
    }

    public void setBookings(ArrayList<Booking> bookings) {
        this.bookings = bookings;
    }

    public ArrayList<Booking> getBookings() {
        return bookings;
    }
}
