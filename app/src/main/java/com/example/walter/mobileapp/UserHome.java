package com.example.walter.mobileapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
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
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class UserHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String username;
    String role;
    ListenerRegistration listenerToInvitation = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_home);
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

        View view_login = navigationView.getHeaderView(0);
        final TextView userName = view_login.findViewById(R.id.userName);
        username =StaticInstance.username;
        userName.setText("Name: "+username);

        TextView userRole = view_login.findViewById(R.id.userRole);
        role =StaticInstance.role;
        userRole.setText("Role: "+role);

        addListenerForInvitation();

    }

    private void sendNotify(String date, String time, String manager, String address, String match, String documentId, String team, String role, boolean covered) {
        Intent yesIntent = new Intent(this,YesNotify.class);
        yesIntent.putExtra("match",match);
        yesIntent.putExtra("accept",true);
        yesIntent.putExtra("document",documentId);
        yesIntent.putExtra("username",username);
        yesIntent.putExtra("team",team);
        yesIntent.putExtra("role",role);
        yesIntent.putExtra("covered",covered);
        yesIntent.putExtra("date",date);
        yesIntent.putExtra("time",time);
        yesIntent.putExtra("manager",manager);
        yesIntent.putExtra("address",address);


        yesIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent yesPendingIntent = PendingIntent.getActivity(this,0,yesIntent,PendingIntent.FLAG_ONE_SHOT);

        Intent noIntent = new Intent(this,YesNotify.class);
        noIntent.putExtra("match",match);
        noIntent.putExtra("accept",false);
        noIntent.putExtra("document",documentId);
        noIntent.putExtra("username",username);


        noIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent noPendingIntent = PendingIntent.getActivity(this,0,noIntent,PendingIntent.FLAG_ONE_SHOT);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default",
                    "YOUR_CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DISCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "default")
                .setSmallIcon(R.drawable.logo) // notification icon
                .setContentTitle("You received a new invitation:") // title for notification
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(manager+" invited you for the match that will be played in "+address+ " in "+date+" at "+time+":00"))
                .setAutoCancel(true)
                .addAction(R.drawable.addplayer,"Accept",yesPendingIntent)
                .addAction(R.drawable.addplayer,"Decline",noPendingIntent);
        mBuilder.build().flags |= Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(0, mBuilder.build());
    }

    void addListenerForInvitation(){
        Log.e("username",username);
        StaticInstance.db.collection("invite").whereEqualTo("invited",username).whereEqualTo("notified",false).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.get("invited") != null && document.get("invited").toString().equals(username)&& (boolean)document.get("notified") == false){
                                    sendNotify(document.get("date").toString(),document.get("time").toString(),document.get("manager").toString(),document.get("address").toString(),document.get("match").toString(),document.getId(),document.get("team").toString(),document.get("role").toString(),(boolean)document.get("covered"));
                                    StaticInstance.db.collection("invite").document(document.getId()).update("notified",true);
                                }
                            }
                        }
                    }
                });


        listenerToInvitation = StaticInstance.db.collection("invite").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                Log.e("ce stato un","");
                for(DocumentSnapshot document : snapshot) {
                    if(document.get("invited") != null && document.get("invited").toString().equals(username)&& (boolean)document.get("notified") == false){
                        Log.e("mando notifica,","df");
                        sendNotify(document.get("date").toString(),document.get("time").toString(),document.get("manager").toString(),document.get("address").toString(),document.get("match").toString(),document.getId(),document.get("team").toString(),document.get("role").toString(),(boolean)document.get("covered"));
                        StaticInstance.db.collection("invite").document(document.getId()).update("notified",true);
                    }
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
        AlertDialog.Builder alertadd = new AlertDialog.Builder(this);
        alertadd.setTitle("Do you really want to exit?");
        alertadd.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_home, menu);
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

        if (id == R.id.nav_createMatch) {
            Intent intent = new Intent(this, CreateMatch.class);
            startActivity(intent);
        } else if (id == R.id.nav_joinMatch) {

        } else if (id == R.id.my_matches) {
            Intent intent = new Intent(this, MyMatchesList.class);
            intent.putExtra("username",username);
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

    public void logout() {
        SharedPreferences sharedPref = getSharedPreferences("logged user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("user");
        editor.remove("role");
        editor.commit();
        if (listenerToInvitation != null){
            listenerToInvitation.remove();
            listenerToInvitation = null;
        }
        startActivity(new Intent(this, LoginActivity.class));
    }



}
