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
            Log.e("ecco",players[i-1]);

            Button currentPlayer = fragmentView.findViewById(getResources().getIdentifier("a" + String.valueOf(i), "id", "com.example.walter.mobileapp"));
            currentPlayer.setOnClickListener(listener);
            currentPlayer.setText(players[i-1]);
            Log.e("il player a"+i,"ha valore"+players[i-1]);
        }

        ((RelativeLayout) fragmentView.findViewById(R.id.pitch)).setOnTouchListener(new View.OnTouchListener() {
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


    private void orderPlayer(ArrayList<HashMap> players_query, String[] players) {
        //portieriA = 0, portieriB = 0, difensoriA = 0, difensoriB = 0, centrocampistiA=0,centrocampistiB=0,attaccantiA=0,attaccantiB=0;
        int[] num = {0,0,0,0,0,0,0,0};
        String[] eventually = new String[40];
        Arrays.fill(eventually,"");
        for(int i=0;i<players_query.size();i++) {
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
                Log.e("in posizione"+(position*5+num[position*2]),"metto "+name+role);
                eventually[position*5 + num[position * 2]] = name + "\n(" + role.charAt(0) + ")";
                num[position * 2]++;
            } else {
                Log.e("in posizione"+(position*5+num[position*2]),"metto "+name+role);
                eventually[20 + position*5 + num[position * 2 + 1]] = name + "\n(" + role.charAt(0) + ")";
                num[position * 2 + 1]++;
            }
        }
        players[0] = eventually[0];
        players[1] = eventually[5];
        players[2] = eventually[10];
        players[3] = eventually[11];
        players[4] = eventually[15];
        Log.e("èvero",players[4]);

        players[5] = eventually[20];
        players[6] = eventually[25];
        players[7] = eventually[30];
        players[8] = eventually[31];
        players[9] = eventually[35];

    }


    public class MyClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            selectedPlayer = v;
        }
    }

}
