package com.example.walter.mobileapp.aiobject;

import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;
@Id("pitch")
public class Pitch {

    @Param(0)
    private String code;

    @Param(1)
    private int distance;

    @Param(2)
    private int rate;

    @Param(3)
    private int numMatchInPitch;

    public Pitch(){

    }

    public Pitch(String code, int distance, int rate, int numMatchInPitch) {
        this.code = code;
        this.distance = distance;
        this.rate = rate;
        this.numMatchInPitch = numMatchInPitch;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getNumMatchInPitch() {
        return numMatchInPitch;
    }

    public void setNumMatchInPitch(int numMatchInPitch) {
        this.numMatchInPitch = numMatchInPitch;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }



    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    @Override
    public boolean equals( Object obj) {
        return code.equals(((Pitch)obj).code);
    }
}
