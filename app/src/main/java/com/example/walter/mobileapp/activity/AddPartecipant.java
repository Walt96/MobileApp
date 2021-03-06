package com.example.walter.mobileapp.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.walter.mobileapp.utility.CheckConnection;
import com.example.walter.mobileapp.object.Match;
import com.example.walter.mobileapp.R;
import com.example.walter.mobileapp.utility.StaticInstance;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class AddPartecipant extends Fragment {


    View fragmentView;
    TextView playerSearched;
    TextView userSearched;
    TextView roleSearched;
    TextView scoreSearched;
    ImageView imageSearched;
    Match handledMatch;
    Button invitePlayer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.add_partecipant, container, false);

        playerSearched = fragmentView.findViewById(R.id.playerSearched);
        userSearched = fragmentView.findViewById(R.id.username);
        roleSearched = fragmentView.findViewById(R.id.role);
        scoreSearched = fragmentView.findViewById(R.id.userScore);
        imageSearched = fragmentView.findViewById(R.id.userImage);

        //bottone ricerca utente
        ((ImageButton) (fragmentView.findViewById(R.id.search))).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchPlayer();
            }
        });

        //bottone invita utente
        invitePlayer = ((Button) (fragmentView.findViewById(R.id.Invite)));
        invitePlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckConnection.isConnected(getActivity()))
                    addPartecipant();
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("You don't have internet connection, please check it!")
                            .setTitle("An error occurred");
                    builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).setPositiveButton("Check now", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                        }
                    });
                    builder.create().show();
                }
            }
        });

        handledMatch = (Match) getArguments().get("match");
        return fragmentView;
    }

    //funzione di invito del partecipante
    private void addPartecipant() {
        final String username = userSearched.getText().toString();
        final String role = roleSearched.getText().toString();

        if (!userSearched.getText().equals("")) {
            AlertDialog.Builder alertadd = new AlertDialog.Builder(getActivity());
            LayoutInflater factory = LayoutInflater.from(getActivity());
            final View view = factory.inflate(R.layout.alert_add_player, null);
            ((TextView) (view.findViewById(R.id.playerusername))).setText(username);
            ((TextView) (view.findViewById(R.id.playerrole))).setText(role);
            alertadd.setTitle("You are inviting this player. Choose his team:");
            alertadd.setView(view);


            //invito del player in base al team
            //team A
            alertadd.setNegativeButton("A", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dlg, int sumthin) {
                    StaticInstance.db.collection("matches").document(handledMatch.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                ArrayList currentPartecipants = (ArrayList) task.getResult().get("partecipants");
                                if (currentPartecipants.contains(username))
                                    invitePlayer.setError("Watch out, this player is already in your match!");
                                else if (currentPartecipants.size() == 10)
                                    invitePlayer.setError("Watch out, your match contains already 10 players!");
                                else {
                                    final HashMap newInvite = new HashMap();
                                    newInvite.put("match", handledMatch.getId());
                                    newInvite.put("date", handledMatch.getDate());
                                    newInvite.put("time", handledMatch.getTime());
                                    newInvite.put("address", handledMatch.getAddress());
                                    newInvite.put("manager", handledMatch.getManager());
                                    newInvite.put("from", StaticInstance.username);
                                    newInvite.put("covered", handledMatch.isCovered());
                                    newInvite.put("invited", username);
                                    newInvite.put("notified", false);
                                    newInvite.put("accept", "pending");
                                    newInvite.put("role", role);
                                    newInvite.put("team", "A");
                                    newInvite.put("readFrom", false);
                                    newInvite.put("readTo", false);
                                    StaticInstance.db.collection("invite")
                                            .add(newInvite)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference aVoid) {
                                                    Snackbar mySnackbar = Snackbar.make(fragmentView.findViewById(R.id.layout), "Player invited, wait for response!", Snackbar.LENGTH_LONG);
                                                    mySnackbar.show();
                                                }
                                            });
                                }

                            }
                        }
                    });
                }
            });
            //teamB
            alertadd.setPositiveButton("B", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dlg, int sumthin) {
                    StaticInstance.db.collection("matches").document(handledMatch.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                ArrayList currentPartecipants = (ArrayList) task.getResult().get("partecipants");
                                if (currentPartecipants.contains(username))
                                    invitePlayer.setError("Watch out, this player is already in your match!");
                                else {
                                    final HashMap newInvite = new HashMap();
                                    newInvite.put("match", handledMatch.getId());
                                    newInvite.put("date", handledMatch.getDate());
                                    newInvite.put("time", handledMatch.getTime());
                                    newInvite.put("address", handledMatch.getAddress());
                                    newInvite.put("manager", handledMatch.getManager());
                                    newInvite.put("covered", handledMatch.isCovered());
                                    newInvite.put("invited", username);
                                    newInvite.put("from", StaticInstance.username);
                                    newInvite.put("notified", false);
                                    newInvite.put("accept", "pending");
                                    newInvite.put("role", role);
                                    newInvite.put("team", "B");
                                    newInvite.put("readFrom", false);
                                    newInvite.put("readTo", false);
                                    StaticInstance.db.collection("invite")
                                            .add(newInvite)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference aVoid) {
                                                    Snackbar mySnackbar = Snackbar.make(fragmentView.findViewById(R.id.layout), "Player invited, wait for response!", Snackbar.LENGTH_LONG);
                                                    mySnackbar.show();
                                                }
                                            });
                                }

                            }
                        }
                    });
                }
            });
            alertadd.setNeutralButton("Decline", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dlg, int sumthin) {

                }
            });

            alertadd.show();
        }
    }

    //funzione di ricerca del player e caricamento dei rispettivi dati
    void searchPlayer() {
        StaticInstance.db.collection("users").whereEqualTo("player", true).whereEqualTo("username", playerSearched.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() == 0) {
                                playerSearched.setError("No player found with this username!");
                            } else {
                                StaticInstance.mStorageRef.child("users/" + playerSearched.getText().toString()).getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri imageUri) {
                                                if (imageUri != null) {
                                                    Glide.with(getView())
                                                            .load(imageUri).apply(bitmapTransform(new CircleCrop()))
                                                            .into(imageSearched);

                                                } else {
                                                    Glide.with(getView())
                                                            .load(Uri.parse("android.resource://com.example.walter.mobileapp/" + R.drawable.ic_person_black_24dp))
                                                            .into(imageSearched);
                                                }

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                Glide.with(getView())
                                                        .load(Uri.parse("android.resource://com.example.walter.mobileapp/" + R.drawable.ic_person_black_24dp))
                                                        .into(imageSearched);
                                            }
                                        });

                                DocumentSnapshot player = task.getResult().getDocuments().get(0);

                                userSearched.setText(player.get("username").toString());
                                roleSearched.setText(player.get("role").toString());

                                ArrayList rates = (ArrayList) player.get("rates");
                                float sum = 0;
                                for (int i = 0; i < rates.size(); i++)
                                    sum += Float.valueOf(String.valueOf((Long) rates.get(i)));
                                String rate_string = "-";
                                if (rates.size() != 0) {
                                    rate_string = String.valueOf((sum / rates.size()));
                                }
                                scoreSearched.setText(rate_string.substring(0, Math.min(4, rate_string.length())));
                            }
                        }
                    }
                });

    }
}
