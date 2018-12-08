package com.example.walter.mobileapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class AddPartecipant extends Fragment {

    View fragmentView;
    TextView playerSearched;
    TextView userSearched;
    TextView roleSearched;
    TextView scoreSearched;
    ImageView imageSearched;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        fragmentView =  inflater.inflate(R.layout.add_partecipant, container, false);

        playerSearched = fragmentView.findViewById(R.id.playerSearched);
        userSearched = fragmentView.findViewById(R.id.username);
        roleSearched = fragmentView.findViewById(R.id.role);
        scoreSearched = fragmentView.findViewById(R.id.userScore);
        imageSearched = fragmentView.findViewById(R.id.userImage);
        ((ImageButton)(fragmentView.findViewById(R.id.search))).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchPlayer();
            }
        });

        Match handledMatch = (Match) getArguments().get("match");
        return fragmentView;
    }

    void searchPlayer(){
        Log.e("cerco",playerSearched.getText().toString());
        StaticInstance.db.collection("users").whereEqualTo("player",true).whereEqualTo("username",playerSearched.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(task.getResult().size()==0){
                                playerSearched.setError("No player found with this username!");
                            }else {
                                DocumentSnapshot player = task.getResult().getDocuments().get(0);
                                userSearched.setText(player.get("username").toString());
                                roleSearched.setText(player.get("role").toString());
                                //scoreSearched.setText(player.get("role").toString());
                                //caricare l'immagine
                            }
                        }
                    }
                });

    }
}
