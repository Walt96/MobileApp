package com.example.walter.mobileapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyMatchesList extends AppCompatActivity {

    ArrayList<Match> matches;
    String username;
    CustomAdapter adapter;
    Lock lockMatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_matches_list);
        matches = new ArrayList<>();
        username = StaticInstance.username;
        final ListView listView = findViewById(R.id.matchlist);
        loadMatch();
        listView.setAdapter(adapter=new CustomAdapter(getApplicationContext()));
        lockMatch = new ReentrantLock();
    }

    private void loadMatch() {
        StaticInstance.getInstance().collection("matches").whereEqualTo("manager",username).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    lockMatch.lock();
                    Match toAdd = new Match(document.getId(), document.get("date").toString(), document.get("time").toString(), document.get("manager").toString(), document.get("pitchcode").toString(), true, (ArrayList) document.get("partecipants"), (ArrayList) document.get("registered"), (boolean) document.get("covered"), document.get("address").toString());
                    if(!matches.contains(toAdd)) {
                        matches.add(toAdd);
                        adapter.notifyDataSetChanged();
                    }
                    lockMatch.unlock();

                }
            }
        });

        StaticInstance.getInstance().collection("matches").whereArrayContains("partecipants",username).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot document : task.getResult()){
                    lockMatch.lock();
                    Match toAdd = new Match(document.getId(), document.get("date").toString(), document.get("time").toString(), document.get("manager").toString(), document.get("pitchcode").toString(),false,(ArrayList)document.get("partecipants"),(ArrayList)document.get("registered"),(boolean)document.get("covered"),document.get("address").toString());
                    if(!matches.contains(toAdd)) {
                        matches.add(toAdd);
                        adapter.notifyDataSetChanged();
                    }
                    lockMatch.unlock();
                }
            }
        });
        Log.e("numero",String.valueOf(matches.size()));

    }


    class CustomAdapter extends BaseAdapter {

        Context context;

        public CustomAdapter(Context applicationContext) {
            context = applicationContext;
        }

        @Override
        public int getCount() {
            return matches.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.custom_match_list, parent, false);

            TextView matchDate = convertView.findViewById(R.id.date);
            TextView matchTime = convertView.findViewById(R.id.time);
            TextView matchManager = convertView.findViewById(R.id.bookedby);

            final Match currentMatch = matches.get(position);
            matchDate.setText("Date:   " + String.valueOf(currentMatch.getDate()));

            matchTime.setText("Time:   " + currentMatch.getTime());
            matchManager.setText("Booked by:   " + currentMatch.getManager());

            ImageButton search = convertView.findViewById(R.id.search);
            search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(),HandleMatches.class);
                    intent.putExtra("match",currentMatch);
                    startActivity(intent);
                }
            });

            ImageButton confirm = convertView.findViewById(R.id.confirm);
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //confirm match
                }
            });

            ImageButton delete = convertView.findViewById(R.id.delete);
            if (currentMatch.isBookedByMe()){
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //delete
                    }
                });
            }else {
                delete.setClickable(false);
            }
            return convertView;

        }
    }
}
