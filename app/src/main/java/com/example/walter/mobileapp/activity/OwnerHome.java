package com.example.walter.mobileapp.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.walter.mobileapp.utility.CheckConnection;
import com.example.walter.mobileapp.object.Match;
import com.example.walter.mobileapp.R;
import com.example.walter.mobileapp.utility.StaticInstance;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OwnerHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseFirestore db = StaticInstance.getInstance();
    String username;
    ArrayList<Match> match;
    Dialog infoDialog;
    EditText matchDatePicker;
    ListView listView;
    DateFormat formatter;

    private final String DATEREGEX = "^(0[1-9]|[1-2][0-9]|3[0-1])\\/(0[1-9]|1[0-2])\\/(20[0-9][0-9])$";
    private Pattern datePattern;
    private Matcher dateMatcher;
    private Calendar actualCalendar;
    private String email;


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

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        username = StaticInstance.username;

        listView = findViewById(R.id.dailyMatch);

        ImageButton searchButton = findViewById(R.id.selectButton);
        searchButton.callOnClick();

        if (!CheckConnection.isConnected(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

        View view_login = navigationView.getHeaderView(0);

        final TextView userName = view_login.findViewById(R.id.userName);
        username = StaticInstance.username;
        userName.setText("Name: " + username);

        TextView userRole = view_login.findViewById(R.id.userMail);
        email = StaticInstance.email;
        userRole.setText("Email: " + email);


    }

    // Funzione utilizzata per verificare che la data immessa sia corretta
    public boolean isValidDate(String date) {
        dateMatcher = datePattern.matcher(date);
        if (dateMatcher.matches()) {
            int day = Integer.parseInt(dateMatcher.group(1));
            int month = Integer.parseInt(dateMatcher.group(2));
            int year = Integer.parseInt(dateMatcher.group(3));

            dateMatcher.reset();

            int actualYear = actualCalendar.get(Calendar.YEAR);
            int actualMonth = actualCalendar.get(Calendar.MONTH) + 1;
            int actualDay = actualCalendar.get(Calendar.DAY_OF_MONTH);

            if (year < 2018 || year > actualYear) {
                return false;
            }

            if (year == actualYear) {
                if (month > actualMonth) {
                    return false;
                } else if (month == actualMonth && day > actualDay) {
                    return false;
                }
            }

        } else {
            return false;
        }
        return true;
    }

    // Funzione con la quale si richiedono le partite da visualizzare.
    public void showMatch(View w) {
        match = new ArrayList<>();
        String match_date = String.valueOf(matchDatePicker.getText());
        if (isValidDate(match_date)) {
            db.collection("matches")
                    .whereEqualTo("pitchmanager", username)
                    .whereEqualTo("date", match_date)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {

                                final OwnerHome.CustomAdapter customAdapter = new OwnerHome.CustomAdapter(getApplicationContext());
                                listView.setAdapter(customAdapter);

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String id = document.getId();
                                    String manager = document.get("manager").toString();

                                    String date = document.get("date").toString();
                                    String address = document.get("address").toString();
                                    String time = document.get("time").toString() + ":00";
                                    ArrayList registered = (ArrayList) document.get("registered");
                                    boolean covered = document.getBoolean("covered");
                                    final Match currentMatch = new Match(id, date, time, manager, address, registered, covered);

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
        AlertDialog.Builder alertadd = new AlertDialog.Builder(this);
        alertadd.setTitle("Do you really want to exit?");
        alertadd.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

                logout();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertadd.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.owner_home, menu);
        return true;
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.create_pitch) {
            Intent intent = new Intent(this, CreatePitch.class);
            startActivity(intent);
            // Handle the camera action
        } else if (id == R.id.show_pitches) {
            Intent intent = new Intent(this, OwnerPitches.class);
            startActivity(intent);
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


            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                    if (currentMatch.isCovered()) {
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

            TextView managerView = convertView.findViewById(R.id.manager_view);
            TextView addressView = convertView.findViewById(R.id.address_view);
            TextView dateView = convertView.findViewById(R.id.date_view);
            TextView timeView = convertView.findViewById(R.id.time_view);

            managerView.setText(currentMatch.getManager());
            addressView.setText(currentMatch.getAddress());
            dateView.setText(currentMatch.getDate());
            timeView.setText(currentMatch.getTime());


            return convertView;

        }
    }

    private void logout() {

        if (AccessToken.getCurrentAccessToken() != null)
            LoginManager.getInstance().logOut();
        StaticInstance.fblogged = false;

        SharedPreferences sharedPref = getSharedPreferences("logged user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("user");
        editor.commit();
        startActivity(new Intent(this, LoginActivity.class));
    }


}
