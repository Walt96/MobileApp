package com.example.walter.mobileapp.aiobject;

import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;

@Id("choosedResult")
public class ChoosedResult {

    @Param(0)
    private String pitchcode;

    @Param(1)
    private String matchtime;

    public ChoosedResult(){}

    public ChoosedResult(String pitchcode, String matchtime) {
        this.pitchcode = pitchcode;
        this.matchtime = matchtime;
    }

    public String getPitchcode() {
        return pitchcode;
    }

    public void setPitchcode(String pitchcode) {
        this.pitchcode = pitchcode;
    }

    public String getMatchtime() {
        return matchtime;
    }

    public void setMatchtime(String matchtime) {
        this.matchtime = matchtime;
    }
}
