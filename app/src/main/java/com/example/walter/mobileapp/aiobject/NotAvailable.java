package com.example.walter.mobileapp.aiobject;

import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;

@Id("notAvailable")
public class NotAvailable {

    @Param(0)
    private String codepitch;

    @Param(1)
    private String time;

    public NotAvailable(){}

    public NotAvailable(String codepitch, String time) {
        this.codepitch = codepitch;
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public String getCodepitch() {
        return codepitch;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setCodepitch(String codepitch) {
        this.codepitch = codepitch;
    }
}
