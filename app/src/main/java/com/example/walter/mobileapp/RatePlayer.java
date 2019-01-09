package com.example.walter.mobileapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firestore.admin.v1beta1.Progress;

import java.util.ArrayList;
import java.util.HashMap;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class RatePlayer extends AppCompatActivity {

    String username;
    String matchcode;
    ArrayList<Player> players;
    PlayersAdapter adapter;
    ListView listView;
    Button confirm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_players);
        players = new ArrayList<>();
        username = StaticInstance.username;
        matchcode = getIntent().getStringExtra("matchcode");
        confirm = findViewById(R.id.Confirm);

        StaticInstance.getInstance().collection("matches").document(matchcode).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                    if(task.getResult()!=null) {
                        ArrayList<HashMap> registered = (ArrayList) task.getResult().get("registered");
                        for(int i = 0;i<registered.size();i++) {

                            final Player player = new Player(registered.get(i).get("role").toString(),registered.get(i).get("team").toString(),registered.get(i).get("user").toString(),null);
                            if(player.getUsername().equals(StaticInstance.username))
                                continue;
                            StaticInstance.mStorageRef.child("users/" + registered.get(i).get("user")).getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            player.setUri(uri);
                                            adapter.notifyDataSetChanged();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                        }
                                    });
                            players.add(player);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
        });

        listView = findViewById(R.id.list);
        listView.setAdapter(adapter = new PlayersAdapter());
    }

    public void confirmRates(View v) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getRate() == 0) {
                confirm.setError("Please rate all the players: 0 is not admitted.");
                return;
            }
        }

        for(int i = 0;i<players.size();i++) {
            final int finalI = i;
            StaticInstance.db.collection("users").document(players.get(i).getUsername()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()) {
                        ArrayList rates = (ArrayList) task.getResult().get("rates");
                        rates.add(players.get(finalI).getRate());
                        StaticInstance.db.collection("users").document(players.get(finalI).getUsername()).update("rates", rates);
                    }
                }
            });
        }
        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.list), "Your rates have been saved!", Snackbar.LENGTH_LONG);
        mySnackbar.show();
        Intent intent = new Intent(this,RateMatch.class);
        intent.putExtra("matchcode",matchcode);
        startActivity(intent);

    }

    private class PlayersAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return players.size();
        }

        @Override
        public Object getItem(int position) {
            return players.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            convertView = inflater.inflate(R.layout.list_rate, parent, false);

            TextView userRate = convertView.findViewById(R.id.userRate);
            TextView teamRate = convertView.findViewById(R.id.teamRate);
            TextView roleRate = convertView.findViewById(R.id.roleRate);
            ImageView imageRate = convertView.findViewById(R.id.imageRate);
            NumberPicker ratePicker = convertView.findViewById(R.id.ratePicker);
            ratePicker.setMinValue(0);
            ratePicker.setMaxValue(10);
            ratePicker.setWrapSelectorWheel(true);
            ratePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                    players.get(position).setRate(newVal);
                }
            });

            final Player currentPlayer = players.get(position);

            userRate.setText(currentPlayer.getUsername());
            teamRate.setText(currentPlayer.getTeam());
            roleRate.setText(currentPlayer.getRole());

            Uri imageUri = currentPlayer.getUri();
            if(imageUri!=null) {
                Glide.with(convertView)
                        .load(currentPlayer.getUri()).apply(bitmapTransform(new CircleCrop()))
                        .into(imageRate);
            }
            return convertView;
        }
    }
}
