package com.example.walter.mobileapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class OwnerHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String username;
    FirebaseFirestore db = StaticInstance.getInstance();
    StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    DatabaseReference myRef = StaticInstance.getDatabase().getReference("booking/");
    ArrayList<Match> match;
    Dialog infoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        infoDialog = new Dialog(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        username =  StaticInstance.username;

        Log.e("TAG", username);
        final ListView listView = findViewById(R.id.dailyMatch);
        match = new ArrayList<>();

        Date date = Calendar.getInstance().getTime();
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        //String today = formatter.format(date);
        String today = "26/12/2018";
        db.collection("matches")
                .whereEqualTo("pitchmanager", username)
                .whereEqualTo("date", today)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.e("TAG", "Query completed " + task.getResult().size());
                            final OwnerHome.CustomAdapter customAdapter = new OwnerHome.CustomAdapter(getApplicationContext());
                            listView.setAdapter(customAdapter);

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getId();
                                String manager = document.get("manager").toString();
                                String date = document.get("date").toString();
                                String address= document.get("address").toString();
                                String time = document.get("time").toString() + ":00";
                                ArrayList registered = (ArrayList) document.get("registered");
                                boolean covered = document.getBoolean("covered");
                                final Match currentMatch = new Match(id, date, time, manager, address, registered, covered);
                                Log.e("TAG", manager + " - " + date + " - " + address + " - " + time);
                                match.add(currentMatch);
                            }

                        } else {
                            Log.w("", "Error getting documents.", task.getException());
                        }
                    }
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        StaticInstance.currentActivity = this;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.owner_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.create_pitch) {
            Intent intent = new Intent(this,CreatePitch.class);
            startActivity(intent);
            // Handle the camera action
        } else if (id == R.id.show_pitches) {
            Intent intent = new Intent(this, OwnerPitches.class);
            startActivity(intent);
        } else if (id == R.id.nav_map) {
            Intent intent = new Intent(this, AddressMap.class);
            startActivity(intent);
        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {
            logout();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    class CustomAdapter extends BaseAdapter {

        Context context;

        public CustomAdapter(Context applicationContext) {
            context = applicationContext;
        }

        @Override
        public int getCount() {
            return match.size();
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
        public View getView(int position, View convertView, ViewGroup parent) {

            final Match currentMatch = match.get(position);
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.match_view, parent, false);


            convertView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                    {
                        infoDialog.setContentView(R.layout.info_dialog);
                        TextView txtclose;
                        txtclose =(TextView) infoDialog.findViewById(R.id.txtclose);
                        txtclose.setText("X");
                        txtclose.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                infoDialog.dismiss();
                            }
                        });

                        TextView address_view = (TextView) infoDialog.findViewById(R.id.address_view);
                        TextView date_view = (TextView) infoDialog.findViewById(R.id.date);
                        TextView time_view = (TextView) infoDialog.findViewById(R.id.time);
                        TextView covered_view = (TextView) infoDialog.findViewById(R.id.covered);
                        TextView booking_view = (TextView) infoDialog.findViewById(R.id.bookingby);
                        TextView registered_view = (TextView) infoDialog.findViewById(R.id.registered);

                        address_view.setText(currentMatch.getAddress());
                        date_view.setText(currentMatch.getDate());
                        time_view.setText(currentMatch.getTime());
                        String covered = "No";
                        if(currentMatch.isCovered()) {
                            covered = "Yes";
                        }
                        covered_view.setText(covered);
                        booking_view.setText(currentMatch.getManager());
                        registered_view.setText(String.valueOf(currentMatch.getRegistered().size()));

                        //manager_view.setText(currentMatch.getManager());
                        infoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                        infoDialog.show();
                    }
            });

            TextView managerView = convertView.findViewById(R.id.address_view);
            TextView addressView = convertView.findViewById(R.id.address_view);
            TextView dateView = convertView.findViewById(R.id.date_view);
            TextView timeView = convertView.findViewById(R.id.time_view);

            managerView.setText(currentMatch.getManager());
            addressView.setText(currentMatch.getAddress());
            dateView.setText(currentMatch.getDate());
            timeView.setText(currentMatch.getTime());

            Log.e("TAG", "Loaded");

            return convertView;

        }
    }

    private Context getActivity() {
        return this;
    }

    private void logout() {
        SharedPreferences sharedPref = getSharedPreferences("logged user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("user");
        editor.commit();
        startActivity(new Intent(this, LoginActivity.class));
    }


}
