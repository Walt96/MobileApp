package com.example.walter.mobileapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class UserHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String username;
    String role;
    ListenerRegistration listenerToInvitation = null;
    ArrayList<com.example.walter.mobileapp.Notification> notifications;
    Lock lock;
    ListView notificationsList;
    CustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        notifications = new ArrayList<>();
        lock = new ReentrantLock();
        setContentView(R.layout.activity_user_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


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

        notificationsList = findViewById(R.id.notificationsList);
        notificationsList.setAdapter(adapter = new CustomAdapter());

        loadProfile();
    }

    private void loadProfile() {

        final ImageView profile = findViewById(R.id.imagePlayer);
        StaticInstance.mStorageRef.child("users/" + StaticInstance.username).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri imageUri) {
                        if(imageUri!=null) {
                            Glide.with(getApplicationContext())
                                    .load(imageUri).apply(bitmapTransform(new CircleCrop()))
                                    .into(profile);

                        }
                        else {
                            Glide.with(getApplicationContext())
                                    .load(Uri.parse("android.resource://com.example.walter.mobileapp/"+R.drawable.ic_person_black_24dp))
                                    .into(profile);
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Glide.with(getApplicationContext())
                                .load(Uri.parse("android.resource://com.example.walter.mobileapp/"+R.drawable.ic_person_black_24dp))
                                .into(profile);
                    }
                });
        StaticInstance.db.collection("users").document(username).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    ((TextView)findViewById(R.id.role)).setText(task.getResult().get("role").toString());
                    ArrayList rates = (ArrayList) task.getResult().get("rates");
                    ((TextView)findViewById(R.id.name)).setText(StaticInstance.username);

                    TextView rateText = findViewById(R.id.rate);
                    if(rates.isEmpty()) {
                        rateText.setText("No rate.");
                        return;
                    }
                    float sumRate = 0;
                    for(Object rate:rates){
                        sumRate+=Float.valueOf((Long)rate);
                    }
                    String toDisplay = String.valueOf(sumRate/rates.size());
                    rateText.setText(toDisplay.substring(0,Math.min(3,toDisplay.length())));


                }
            }
        });
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        loadNotifications();
    }

    private void loadNotifications() {
        //aggiungere controllo sul tipo di notifica se richiesta o invito
        StaticInstance.getInstance().collection("invite").whereEqualTo("invited",username).whereEqualTo("readTo",false).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    lock.lock();
                    String from = document.get("from").toString();
                    String to = username;
                    String state = document.get("accept").toString();
                    String date = document.get("date").toString();
                    String address = document.get("address").toString();
                    String id = document.getId();
                    String covered = "is covered";
                    if(!(boolean)document.get("covered"))
                        covered = "is not covered";
                    String info_match = date+" in "+address+". The pitch " + covered;
                    String match = document.get("match").toString();
                    String role = document.get("role").toString();
                    String team = document.get("team").toString();
                    String time = document.get("time").toString();
                    com.example.walter.mobileapp.Notification notification = new com.example.walter.mobileapp.Notification(id,from,to,state,info_match, match, role, team, date, time, (boolean)document.get("covered"), address);
                    int index = notifications.indexOf(notification);

                    if(index==-1) {
                        notifications.add(notification);
                        adapter.notifyDataSetChanged();
                    }else if(!state.equals(notifications.get(index).getState())){
                        notifications.set(index,notification);
                        adapter.notifyDataSetChanged();
                    }
                    lock.unlock();

                }
            }
        });

        StaticInstance.getInstance().collection("invite").whereEqualTo("from",username).whereEqualTo("readFrom",false).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    lock.lock();
                    String id = document.getId();
                    String from = username;
                    String to = document.get("invited").toString();
                    String state = document.get("accept").toString();
                    String date = document.get("date").toString();
                    String address = document.get("address").toString();
                    String covered = "is covered";
                    if(!(boolean)document.get("covered"))
                        covered = "is not covered";
                    String info_match = date+" in "+address+". The pitch " + covered;
                    String match = document.get("match").toString();
                    String role = document.get("role").toString();
                    String team = document.get("team").toString();
                    String time = document.get("time").toString();
                    com.example.walter.mobileapp.Notification notification = new com.example.walter.mobileapp.Notification(id,from,to,state,info_match, match, role, team, date, time, (boolean)document.get("covered"), address);
                    notifications.add(notification);
                    adapter.notifyDataSetChanged();
                    lock.unlock();
                }
            }
        });
    }

    private void sendNotify(String date, String time, String manager, String address, String match, String documentId, String team, String role, boolean covered) {
        Intent yesIntent = new Intent(this,HandleResponseToInvitation.class);
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

        Intent noIntent = new Intent(this,HandleResponseToInvitation.class);
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
        /*StaticInstance.db.collection("invite").whereEqualTo("invited",username).whereEqualTo("notified",false).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.get("invited") != null && document.get("invited").toString().equals(username)&& (boolean)document.get("notified") == false){
                                    sendNotify(document.get("date").toString(),document.get("time").toString(),document.get("from").toString(),document.get("address").toString(),document.get("match").toString(),document.getId(),document.get("team").toString(),document.get("role").toString(),(boolean)document.get("covered"));
                                    StaticInstance.db.collection("invite").document(document.getId()).update("notified",true);
                                }
                            }
                        }
                    }
                });
*/

        listenerToInvitation = StaticInstance.db.collection("invite").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                Log.e("ce stato un","");
                for(DocumentSnapshot document : snapshot) {
                    if(document.get("invited") != null && document.get("invited").toString().equals(username)&& (boolean)document.get("notified") == false){
                        Log.e("mando notifica,","df");
                        sendNotify(document.get("date").toString(),document.get("time").toString(),document.get("from").toString(),document.get("address").toString(),document.get("match").toString(),document.getId(),document.get("team").toString(),document.get("role").toString(),(boolean)document.get("covered"));
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


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_createMatch) {
            Intent intent = new Intent(this, CreateMatch.class);
            startActivity(intent);
        } else if (id == R.id.nav_joinMatch) {

            startActivity(new Intent(this,AiHelper.class));

        } else if (id == R.id.my_matches) {
            Intent intent = new Intent(this, MyMatchesList.class);
            intent.putExtra("username",username);
            startActivity(intent);

        }   else if (id == R.id.nav_send) {
            logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void logout() {

        if(AccessToken.getCurrentAccessToken()!= null)
            LoginManager.getInstance().logOut();
        StaticInstance.fblogged = false;

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

    class CustomAdapter extends BaseAdapter {


        public CustomAdapter(){
        }

        @Override
        public int getCount() {
            return notifications.size();
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

            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            convertView = inflater.inflate(R.layout.custom_list_notifications, parent, false);

            final TextView from = convertView.findViewById(R.id.from);
            TextView to = convertView.findViewById(R.id.to);
            TextView state = convertView.findViewById(R.id.state);
            TextView info = convertView.findViewById(R.id.info);


            final com.example.walter.mobileapp.Notification notification = notifications.get(position);
            from.setText(notification.getFrom());
            to.setText(notification.getTo());
            state.setText(notification.getState());
            info.setText(notification.getInfo_match());


            if(!notification.getTo().equals(StaticInstance.username) || !notification.getState().equals("pending")) {
                convertView.findViewById(R.id.accept).setVisibility(View.INVISIBLE);
                convertView.findViewById(R.id.decline).setVisibility(View.INVISIBLE);
            }

            (convertView.findViewById(R.id.delete)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notifications.remove(position);
                    String field = "readTo";
                    if(username.equals(notification.getFrom()))
                        field = "readFrom";
                    StaticInstance.db.collection("invite").document(notification.getId()).update(field,true);
                    adapter.notifyDataSetChanged();
                }
            });

            (convertView.findViewById(R.id.accept)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notifications.remove(position);
                    Intent intent = new Intent(getApplicationContext(), HandleResponseToInvitation.class);
                    intent.putExtra("match",notification.getMatch());
                    intent.putExtra("accept",true);
                    intent.putExtra("document",notification.getId());
                    intent.putExtra("username",notification.getTo());
                    intent.putExtra("team",notification.getTeam());
                    intent.putExtra("role",notification.getRole());
                    intent.putExtra("date",notification.getDate());
                    intent.putExtra("time",notification.getTime());
                    intent.putExtra("covered",notification.isCovered());
                    intent.putExtra("manager",notification.getFrom());
                    intent.putExtra("address",notification.getAddress());
                    startActivity(intent);
                    adapter.notifyDataSetChanged();
                }
            });

            (convertView.findViewById(R.id.decline)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notifications.remove(position);
                    StaticInstance.db.collection("invite").document(notification.getId()).update("accept",false);
                    adapter.notifyDataSetChanged();
                }
            });

            return convertView;

        }
    }


}
