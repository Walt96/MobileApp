package com.example.walter.mobileapp;

import android.Manifest;
import android.Manifest.permission;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import it.unical.mat.embasp.base.Callback;
import it.unical.mat.embasp.base.Handler;
import it.unical.mat.embasp.base.InputProgram;
import it.unical.mat.embasp.base.Output;
import it.unical.mat.embasp.platforms.android.AndroidHandler;
import it.unical.mat.embasp.specializations.dlv.android.DLVAndroidService;

public class CreateMatch extends AppCompatActivity {

    TextView dateView;
    Spinner chooseCity;
    Switch covered;
    DatePickerDialog.OnDateSetListener mDateSetListener;
    FirebaseFirestore db = StaticInstance.db;
    StorageReference mStorageRef = StaticInstance.mStorageRef;
    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    StorageReference reference;
    CustomAdapter customAdapter;
    ArrayList<Pitch> pitches;
    ArrayList<Pitch> currentPitches;
    boolean selectedCovered;
    String selectedCity = "";
    String manager;
    String role;
    final HashMap<String, Object> saveMyMatch = new HashMap<>();
    private ArrayList<String> cities;

    String selectedDate;
    ListenerRegistration listener;
    private int CALENDAR_CODE = 10;


    private Context getActivity() {
        return this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.comeback, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //torniamo al menu se ha premuto su back
        startActivity(new Intent(this, UserHome.class));
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customAdapter = new CustomAdapter(getApplicationContext());
        cities = new ArrayList<>();

        reference = FirebaseStorage.getInstance().getReference();
        manager = StaticInstance.username;
        role = StaticInstance.role;
        setContentView(R.layout.activity_create_match);
        dateView = findViewById(R.id.datePicker);
        covered = findViewById(R.id.covered);
        selectedCovered = false;
        covered.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                selectedCovered = isChecked;
                if (isChecked)
                    covered.setText("Covered");
                else
                    covered.setText("Not Covered");

                updateWithConstraints();
            }
        });
        chooseCity = findViewById(R.id.pickCity);
        chooseCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedCity = chooseCity.getItemAtPosition(position).toString();
                updateWithConstraints();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                selectedCity = "";
                updateWithConstraints();
            }

        });
        selectedDate = dateFormat.format(new Date());
        dateView.setText(selectedDate);
        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        getActivity(),
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String day_ = "0";
                if (day >= 10)
                    day_ = day + "/";
                else
                    day_ += day + "/";
                String surplus="";
                if(month<10)
                    surplus="0";
                selectedDate = day_ + surplus + month + "/" + year;
                initPitchAvailableTime();
                dateView.setText(selectedDate);
            }

        };

        final ListView listView = findViewById(R.id.pitchList);

        pitches = new ArrayList<>();
        currentPitches = new ArrayList<>();

        //otteniamo tutti i campi dove è possibile creare una partita e attacchiamo un listener
        //per restare in ascolto di nuove prenotazioni in modo da gestirle in tempo reale
        db.collection("pitch")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<String> items = new ArrayList<>();
                            listView.setAdapter(customAdapter);

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String city = document.get("city").toString();
                                if (!items.contains(city)) {
                                    items.add(city);
                                    cities.add(city);
                                }
                                String address = document.get("address").toString() + " , " + city;
                                boolean covered = (boolean) document.get("covered");
                                double price = (double) (document.get("price"));
                                String owner = document.get("owner").toString();
                                String ownermail = document.get("ownermail").toString();
                                float lat = Float.valueOf(String.valueOf(document.get("latitude")));
                                float lon = Float.valueOf(String.valueOf(document.get("longitude")));

                                final Pitch currentPitch = new Pitch(document.get("code").toString(), address, price, covered, city, owner, ownermail);
                                currentPitch.setLat(lat);
                                currentPitch.setLon(lon);

                                mStorageRef.child("pitch/" + document.get("owner") + document.get("code")).getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Log.e("il mio uri è", uri.toString());
                                                currentPitch.setUri(uri);
                                                customAdapter.notifyDataSetChanged();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                            }
                                        });

                                pitches.add(currentPitch);
                            }

                            //aggiunta listener per gestire eventuali prenotazioni in tempo reale
                            addListenerForNewMatches();
                            initPitchAvailableTime();

                            ArrayAdapter adapter = new ArrayAdapter<>(getActivity(), R.layout.spinneritem, items);
                            chooseCity.setAdapter(adapter);
                            chooseCity.setSelection(0);


                        }
                    }
                });

    }

    //apertura mappa per la ricerca di un campo
    public void openMap(View w) {
        Intent mapIntent = new Intent(this, ShowMap.class);
        mapIntent.putExtra("cities", cities);
        mapIntent.putExtra("date", selectedDate);
        startActivity(mapIntent);
    }

    //inizializza la disponibilità sugli orari per ogni campo in base alle prenotazioni esistenti
    private void initPitchAvailableTime() {
        for (final Pitch p : pitches) {
            db.collection("matches").whereEqualTo("pitchcode", p.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    ArrayList notAvailable = new ArrayList();
                    for (DocumentSnapshot document : task.getResult()) {
                        if (document.get("date").toString().equals(selectedDate))
                            notAvailable.add(document.get("time").toString());
                    }
                    p.initWithoutThese(notAvailable);
                }
            });
        }
        updateWithConstraints();
    }

    //funzione per l'aggiunta di un listener per restare in ascolto di eventuali prenotazioni
    private void addListenerForNewMatches() {
        if (listener != null)
            listener.remove();
        listener = StaticInstance.db.collection("matches").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                for (DocumentSnapshot document : snapshot) {
                    String bookedDate = document.get("date").toString();
                    String bookedTime = document.get("time").toString();
                    String idBookedPitch = document.get("pitchcode").toString();
                    if (selectedDate.equals(bookedDate))
                        for (Pitch p : currentPitches)
                            if (p.getId().equals(idBookedPitch)) {
                                p.removeTime(Integer.valueOf(bookedTime));
                            }
                }

            }
        });

    }

    //applicazione dei filtri scelti dall'utente
    public void updateWithConstraints() {

        ArrayList<Pitch> newPitches = new ArrayList<>();
        for (Pitch _pitch : pitches)
            if ((_pitch.getCity().equals(selectedCity) || selectedCity.equals("")) && (_pitch.isCovered() == selectedCovered))
                newPitches.add(_pitch);
        currentPitches = newPitches;
        customAdapter.notifyDataSetChanged();

    }


    @Override
    protected void onResume() {
        super.onResume();
        StaticInstance.currentActivity = this;
    }

    //adapter per creare un elemento per ogni campo disponibile
    class CustomAdapter extends BaseAdapter {

        Context context;

        public CustomAdapter(Context applicationContext) {
            context = applicationContext;
        }

        @Override
        public int getCount() {
            return currentPitches.size();
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
            convertView = inflater.inflate(R.layout.custom_item_list, parent, false);

            TextView pitchAddress = convertView.findViewById(R.id.pitchAddress);
            TextView pitchPrice = convertView.findViewById(R.id.pricePitch);
            TextView pitchCover = convertView.findViewById(R.id.pitchCover);
            Spinner availableTime = convertView.findViewById(R.id.pitchTime);
            try {
                Field popup = Spinner.class.getDeclaredField("mPopup");
                popup.setAccessible(true);
                android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(availableTime);
                popupWindow.setHeight(350);
            } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            }
            availableTime.setAdapter(currentPitches.get(position).getAvailableTime());
            pitchPrice.setText("Price: " + String.valueOf(currentPitches.get(position).getPrice()) + "€");
            pitchAddress.setText("Address: " + currentPitches.get(position).getAddress());
            ImageView pitchImage = convertView.findViewById(R.id.pitchImage);
            Uri imageUri = currentPitches.get(position).getUri();
            if (imageUri != null) {
                Glide.with(convertView)
                        .load(currentPitches.get(position).getUri())
                        .into(pitchImage);
            } else {
                Glide.with(convertView)
                        .load(Uri.parse("android.resource://com.example.walter.mobileapp/" + R.drawable.login))
                        .into(pitchImage);
            }
            pitchCover.setText("Covered: Yes");
            if (!currentPitches.get(position).isCovered())
                pitchCover.setText("Covered: No");

            final Spinner time = convertView.findViewById(R.id.pitchTime);

            Button book = convertView.findViewById(R.id.bookPitch);
            book.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bookPitch(currentPitches.get(position).getId(), time.getSelectedItem().toString().split(":")[0], currentPitches.get(position).getAddress(), currentPitches.get(position).getOwner(),currentPitches.get(position).getOwnermail(),currentPitches.get(position).getLat(),currentPitches.get(position).getLon());
                }
            });
            return convertView;

        }
    }

    //funzione per la prenotazione del campo scelto
    public void bookPitch(final String pitchId, final String time, final String address, final String owner, final String ownermail, final float lat, final float lon) {

        if (!CheckConnection.isConnected(getActivity())) {
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


        } else {
            if (time.equals("OCCUPATO")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("The pitch you selected is already booked at this time, sorry!")
                        .setTitle("An error occurred");
                builder.create().show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Address: " + address + "\n" +
                        "Date: " + selectedDate + "\n" +
                        "Time: " + time + ":00")
                        .setTitle("This will be your match:");
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        final String matchCode = String.valueOf(new Date().getTime()) + manager;

                        //aggiungo la nuova partita alla collezione matches nel documento data corrente + manager in modo da renderlo univoco con la concorrenza
                        saveMyMatch.put("date", selectedDate);
                        saveMyMatch.put("time", time);
                        saveMyMatch.put("manager", manager);
                        saveMyMatch.put("pitchcode", pitchId);
                        saveMyMatch.put("address", address);
                        saveMyMatch.put("covered", selectedCovered);
                        saveMyMatch.put("code", matchCode);
                        saveMyMatch.put("pitchmanager", owner);
                        saveMyMatch.put("managermail", ownermail);
                        saveMyMatch.put("lat", lat);
                        saveMyMatch.put("lon", lon);


                        addCalendarEvent(saveMyMatch);

                    }
                }).setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.create().show();
            }

        }

    }

    //aggiunta dell'evento al calendario
    private long addCalendarEvent(HashMap<String, Object> saveMyMatch) {

        boolean hasPermission = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(permission.READ_CALENDAR)
                    == PackageManager.PERMISSION_GRANTED && checkSelfPermission(permission.WRITE_CALENDAR)
                    == PackageManager.PERMISSION_GRANTED) {
                hasPermission = true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission.READ_CALENDAR, permission.WRITE_CALENDAR}, CALENDAR_CODE);
            }
        }
        if (hasPermission) {
            AddEventToCalendar task = new AddEventToCalendar(saveMyMatch);
            task.execute();
        }
        return -1;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CALENDAR_CODE)
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addCalendarEvent(saveMyMatch);
            }
    }

    //task per l'aggiunta asincrona dell'evento al calendario
    private class AddEventToCalendar extends AsyncTask<Void, Integer, Long> {

        HashMap saveMyMatch;

        public AddEventToCalendar(HashMap saveMyMatch) {
            this.saveMyMatch = saveMyMatch;
        }

        @Override
        protected Long doInBackground(Void... voids) {

            long startMillis = 0;
            long endMillis = 0;
            Calendar beginTime = Calendar.getInstance();
            String date[] = saveMyMatch.get("date").toString().split("/");
            beginTime.set(Integer.valueOf(date[2]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[0]), Integer.valueOf(saveMyMatch.get("time").toString()), 0);
            startMillis = beginTime.getTimeInMillis();
            Calendar endTime = Calendar.getInstance();
            endTime.set(Integer.valueOf(date[2]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[0]), Integer.valueOf(saveMyMatch.get("time").toString()), 50);
            endMillis = endTime.getTimeInMillis();


            ContentResolver cr = getContentResolver();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);
            values.put(CalendarContract.Events.TITLE, "Football match");
            String is = "is";
            if ((boolean) saveMyMatch.get("covered"))
                is = "is not";
            values.put(CalendarContract.Events.DESCRIPTION, "The match was organized by you. The pitch " + is + " covered.");
            values.put(CalendarContract.Events.CALENDAR_ID, 1);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, Calendar.getInstance().getTimeZone().getID());
            values.put(CalendarContract.Events.EVENT_LOCATION, saveMyMatch.get("address").toString());
            values.put(CalendarContract.Events.HAS_ALARM, true);
            values.put(CalendarContract.Events.ALL_DAY, 0);
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            return Long.parseLong(uri.getLastPathSegment());
        }

        protected void onPostExecute(Long result) {
            saveMyMatch.put("calendarid", result);
            HashMap myProfile = new HashMap();
            myProfile.put("user", manager);
            myProfile.put("role", role);
            myProfile.put("team", "A");
            ArrayList firstRegistered = new ArrayList();
            firstRegistered.add(myProfile);
            saveMyMatch.put("registered", firstRegistered);

            ArrayList myName = new ArrayList();
            myName.add(manager);
            saveMyMatch.put("partecipants", myName);

            saveMyMatch.put("finished", false);
            ArrayList confirmed = new ArrayList();
            confirmed.add(manager);
            saveMyMatch.put("confirmed", confirmed);

            StaticInstance.db.collection("matches").document(saveMyMatch.get("code").toString())
                    .set(saveMyMatch)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            sendMailToOwnerPitch();
                            Snackbar mySnackbar = Snackbar.make(findViewById(R.id.pitchList), "Pitch booked successfully!", Snackbar.LENGTH_LONG);
                            mySnackbar.setAction("View all matches", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getActivity(), MyMatchesList.class);
                                    startActivity(intent);
                                }
                            });
                            mySnackbar.show();
                        }
                    });


        }

        //creazione della mail da mandare al proprietario del campo
        private void sendMailToOwnerPitch() {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL  , new String[]{saveMyMatch.get("managermail").toString()});
            i.putExtra(Intent.EXTRA_SUBJECT, "Booking pitch");
            i.putExtra(Intent.EXTRA_TEXT   , "Hi, I have booked your pitch located in "+saveMyMatch.get("address")+" for the day "+saveMyMatch.get("date")+" at the "+saveMyMatch.get("time")+ ":00.");
            try {
                startActivity(Intent.createChooser(i, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
