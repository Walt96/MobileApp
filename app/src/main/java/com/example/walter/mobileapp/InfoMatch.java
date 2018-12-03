package com.example.walter.mobileapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class InfoMatch extends Fragment {

    View fragmentView;
    Match handledMatch;

    public InfoMatch(){}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView =  inflater.inflate(R.layout.info_match, container, false);
        Match handledMatch = (Match) getArguments().get("match");
        ((TextView)(fragmentView.findViewById(R.id.address))).setText(handledMatch.getAddress());
        ((TextView)(fragmentView.findViewById(R.id.date))).setText(handledMatch.getDate());
        ((TextView)(fragmentView.findViewById(R.id.time))).setText(handledMatch.getTime());
        ((TextView)(fragmentView.findViewById(R.id.covered))).setText(String.valueOf(handledMatch.isCovered()));
        ((TextView)(fragmentView.findViewById(R.id.bookingby))).setText(handledMatch.getManager());
        ((TextView)(fragmentView.findViewById(R.id.registered))).setText(handledMatch.getHowManyRegistered()+ "/10");
        return fragmentView;
    }

}
