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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OwnerHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String username;
    FirebaseFirestore db = StaticInstance.getInstance();
    ArrayList<Match> match;
    Dialog infoDialog;
    DateFormat formatter;
    EditText matchDatePicker;
    ListView listView;
    private final String DATEREGEX = "^(0[1-9]|[1-2][0-9]|3[0-1])\\/(0[1-9]|1[0-2])\\/(20[0-9][0-9])$";
    private Pattern datePattern;
    private Matcher dateMatcher;
    private Calendar actualCalendar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        infoDialog = new Dialog(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_home);
        actualCalendar = Calendar.getInstance();
        Date actualDate = actualCalendar.getTime();
        formatter = new SimpleDateFormat("dd/MM/yyyy");
        String today = formatter.format(actualDate);
        matchDatePicker = findViewById(R.id.match_date);
        matchDatePicker.setText(today);
        Toolbar toolbar = findViewById(R.id.toolbar);
        datePattern = Pattern.compile(DATEREGEX);

        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        username =  StaticInstance.username;

        Log.e("TAG", username);
        listView = findViewById(R.id.dailyMatch);

        ImageButton searchButton = findViewById(R.id.search_button);
        searchButton.callOnClick();

    }

    public boolean isValidDate(String date) {
        dateMatcher = datePattern.matcher(date);
        if(dateMatcher.matches()) {
            Log.e("TAG", date + " - " + "matches");
            int day = Integer.parseInt(dateMatcher.group(1));
            int month = Integer.parseInt(dateMatcher.group(2));
            int year = Integer.parseInt(dateMatcher.group(3));

            dateMatcher.reset();

            int actualYear = actualCalendar.get(Calendar.YEAR);
            int actualMonth = actualCalendar.get(Calendar.MONTH) + 1;
            int actualDay = actualCalendar.get(Calendar.DAY_OF_MONTH);

            Log.e("TAG", day + " " + actualDay);
            Log.e("TAG", month + " " + actualMonth);
            Log.e("TAG", year + " " + actualYear);


            if(year < 2018 || year > actualYear) {
                Log.e("TAG", "First test");
                return false;
            }

            if(year == actualYear) {
                if(month > actualMonth) {
                    Log.e("TAG", "Second test");
                    return false;
                } else if(month == actualMonth && day > actualDay){
                    Log.e("TAG", "Third test");
                    return false;
                }
            }

        } else {
            return false;
        }
        return true;
    }

    public void showMatch(View w) {
        match = new ArrayList<>();
        String match_date = String.valueOf(matchDatePicker.getText());
        if(isValidDate(match_date)) {
            db.collection("matches")
                    .whereEqualTo("pitchmanager", username)
                    .whereEqualTo("date", match_date)
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

        } else {
            matchDatePicker.setError("Please, insert a valid date.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        StaticInstance.currentActivity = this;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
                        txtclose = infoDialog.findViewById(R.id.txtclose);
                        txtclose.setText("X");
                        txtclose.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                infoDialog.dismiss();
                            }
                        });

                        TextView address_view = infoDialog.findViewById(R.id.address_view);
                        TextView date_view = infoDialog.findViewById(R.id.date);
                        TextView time_view = infoDialog.findViewById(R.id.time);
                        TextView covered_view = infoDialog.findViewById(R.id.covered);
                        TextView booking_view = infoDialog.findViewById(R.id.bookingby);
                        TextView registered_view = infoDialog.findViewById(R.id.registered);

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
