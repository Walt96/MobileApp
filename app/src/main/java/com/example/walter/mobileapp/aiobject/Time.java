package com.example.walter.mobileapp.aiobject;

import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;

@Id("time")
public class Time {

    @Param(0)
    private String time;

    @Param(1)
    private int rate;

    @Param(2)
    private int numTimesAtThisTime;


    public Time(){}

    public Time(String time, int rate, int numTimesAtThisTime) {
        this.time = time;
        this.rate = rate;
        this.numTimesAtThisTime = numTimesAtThisTime;
    }

    public int getNumTimesAtThisTime() {
        return numTimesAtThisTime;
    }

    public void setNumTimesAtThisTime(int numTimesAtThisTime) {
        this.numTimesAtThisTime = numTimesAtThisTime;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }
}
