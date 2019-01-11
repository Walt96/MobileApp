package com.example.walter.mobileapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ShowTeams extends Fragment {
    View fragmentView;
    View selectedPlayer = null;
    MyClickListener listener = new MyClickListener();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.showteams, container, false);
        Match handledMatch = (Match) getArguments().get("match");
        ArrayList<HashMap> partecipants = handledMatch.getRegistered();
        String[] players = new String[10];
        orderPlayer(partecipants,players);

        for (int i = 1; i < 11; i++) {

            Button currentPlayer = fragmentView.findViewById(getResources().getIdentifier("a" + String.valueOf(i), "id", "com.example.walter.mobileapp"));
            currentPlayer.setOnClickListener(listener);
            currentPlayer.setText(players[i-1]);
        }

        //gestione del movimento dei marker relativi ai player
        fragmentView.findViewById(R.id.pitch).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (selectedPlayer != null) {
                    selectedPlayer.setX(event.getX() - selectedPlayer.getWidth() / 2);
                    selectedPlayer.setY(event.getY() - selectedPlayer.getHeight() / 2);
                    selectedPlayer = null;
                }

                return false;
            }
        });
        return fragmentView;

    }


    //ordinamento dei player in base ai loro ruoli
    //grazie a questa funzione gestiamo anche casi in cui una squadra è formaata da 5 portieri e altri casi limite
    private void orderPlayer(ArrayList<HashMap> players_query, String[] players) {
        //portieriA = 0, portieriB = 0, difensoriA = 0, difensoriB = 0, centrocampistiA=0,centrocampistiB=0,attaccantiA=0,attaccantiB=0;
        int[] num = {0, 0, 0, 0, 0, 0, 0, 0};
        String[] eventually = new String[40];
        Arrays.fill(eventually, "");
        Arrays.fill(players, " ");
        for (int i = 0; i < players_query.size(); i++) {
            String team = players_query.get(i).get("team").toString();
            String role = players_query.get(i).get("role").toString();
            String name = players_query.get(i).get("user").toString();
            int position = -1;
            switch (role) {
                case "Portiere":
                    position = 0;
                    break;
                case "Difensore":
                    position = 1;
                    break;
                case "Centrocampista":
                    position = 2;
                    break;
                case "Attaccante":
                    position = 3;
                    break;
                default:
                    break;
            }
            if (players_query.get(i).get("team").equals("A")) {
                eventually[position * 5 + num[position * 2]] = name + "\n(" + role.charAt(0) + ")";
                num[position * 2]++;
            } else {

                eventually[20 + position * 5 + num[position * 2 + 1]] = name + "\n(" + role.charAt(0) + ")";
                num[position * 2 + 1]++;
            }
        }
        int found = 0;
        ArrayList normalize_a = new ArrayList();
        ArrayList normalize_b = new ArrayList();
        for (int i = 0, t = 0; i < 40; i++) {
            if (!eventually[i].equals("")) {
                if(i<20)
                    normalize_a.add(eventually[i]);
                else
                    normalize_b.add(eventually[i]);
                found++;
            }
            if ((i+1) % 5 == 0) {
                if (found==0)
                    if(i<20)
                        normalize_a.add(" ");
                    else
                        normalize_b.add(" ");
                if(i==14 && found<=1)
                    normalize_a.add(" ");
                else if(i==34 && found<=1)
                    normalize_b.add(" ");
                found = 0;
            }
        }




        if(normalize_a.size()>5){
            for(int i = normalize_a.size()-1;i>=0;i--) {
                if (normalize_a.get(i).equals(" "))
                    normalize_a.remove(i);
                if (normalize_a.size() <= 5)
                    break;
            }
        }

        if(normalize_b.size()>5) {
            for (int i = normalize_b.size() - 1; i >= 0; i--) {
                if (normalize_b.get(i).equals(" ")) {
                    normalize_b.remove(i);
                }
                if (normalize_b.size() <= 5)
                    break;
            }
        }


        for(int i = 0;i<10;i++)
            if(i<5)
                players[i] = normalize_a.get(i).toString();
            else
                players[i] = normalize_b.get(i%5).toString();
    }


    public class MyClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            selectedPlayer = v;
        }
    }

}
