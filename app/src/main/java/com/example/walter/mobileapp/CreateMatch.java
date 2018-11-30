package com.example.walter.mobileapp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
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

public class CreateMatch extends AppCompatActivity {

    TextView dateView;
    Spinner chooseCity;
    Switch covered;
    DatePickerDialog.OnDateSetListener mDateSetListener;
    FirebaseFirestore db = StaticInstance.getInstance();
    StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    DatabaseReference myRef = StaticInstance.getDatabase().getReference("booking");
    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    StorageReference reference;
    CustomAdapter customAdapter;
    ArrayList<Pitch> pitches;
    ArrayList<Pitch> currentPitches;
    boolean selectedCovered;
    String selectedCity="";

    int index = 0;
    String selectedDate;


    private Context getActivity() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reference = FirebaseStorage.getInstance().getReference();

        setContentView(R.layout.activity_create_match);
        dateView = findViewById(R.id.datePicker);
        covered = findViewById(R.id.covered);
        selectedCovered = false;
        covered.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                selectedCovered=isChecked;
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
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                selectedDate  = day + "/" + month + "/" + year;
                dateView.setText(selectedDate);
                for(Pitch p : currentPitches){
                    p.removeListener();
                    p.setListener(selectedDate);
                }
            }

        };

        final ListView listView = findViewById(R.id.pitchList);

        pitches = new ArrayList<>();
        currentPitches = new ArrayList<>();

        db.collection("pitch")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<String> items = new ArrayList<>();
                            customAdapter = new CustomAdapter(getApplicationContext());
                            listView.setAdapter(customAdapter);

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String city = document.get("city").toString();
                                if (!items.contains(city)) {
                                    items.add(city);
                                }
                                String address = document.get("address").toString() + " , " + city;
                                boolean covered = (boolean) document.get("covered");
                                double price = (double) (document.get("price"));
                                final Pitch currentPitch = new Pitch(document.get("code").toString(),address, price, covered, city);

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
                                currentPitch.setListener(dateFormat.format(new Date()));
                                pitches.add(currentPitch);
                                currentPitches.add(currentPitch);

                            }
                            updateWithConstraints();
                            ArrayAdapter adapter = new ArrayAdapter<>(getActivity(), R.layout.spinneritem, items);
                            chooseCity.setAdapter(adapter);

                        } else {
                            Log.w("", "Error getting documents.", task.getException());
                        }
                    }
                });

    }

    public void updateWithConstraints() {

        ArrayList<Pitch> newPitches = new ArrayList<>();
        for (Pitch _pitch : pitches)
            if ((_pitch.getCity().equals(selectedCity) || selectedCity.equals("")) && (_pitch.isCovered() == selectedCovered)) {
                newPitches.add(_pitch);
                if(!currentPitches.contains(_pitch))
                    _pitch.setListener(selectedDate);
            }
            else if(currentPitches.contains(_pitch)) {
                //implementare == altrimenti inutile
                _pitch.removeListener();
            }
        currentPitches = newPitches;
        customAdapter.notifyDataSetChanged();

    }



    @Override
    protected void onResume() {
        super.onResume();
        StaticInstance.currentActivity = this;
    }

    class CustomAdapter extends BaseAdapter{

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
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.custom_item_list, parent, false);

            TextView pitchAddress = convertView.findViewById(R.id.pitchAddress);
            TextView pitchPrice = convertView.findViewById(R.id.pricePitch);
            TextView pitchCover = convertView.findViewById(R.id.pitchCover);
            Spinner availableTime = convertView.findViewById(R.id.pitchTime);
            try {
                Field popup = Spinner.class.getDeclaredField("mPopup");
                popup.setAccessible(true);
                // Get private mPopup member variable and try cast to ListPopupWindow
                android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(availableTime);
                // Set popupWindow height to 500px
                popupWindow.setHeight(350);
            }
            catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            }
            availableTime.setAdapter(currentPitches.get(position).getAvailableTime());
            pitchPrice.setText("Price: "+ String.valueOf(currentPitches.get(position).getPrice()) + "€");
            pitchAddress.setText(currentPitches.get(position).getAddress());
            ImageView pitchImage = convertView.findViewById(R.id.pitchImage);
            Uri imageUri = currentPitches.get(position).getUri();
            if(imageUri!=null) {
                Glide.with(convertView)
                        .load(currentPitches.get(position).getUri())
                        .into(pitchImage);
            }
            else {
                Glide.with(convertView)
                        .load(Uri.parse("android.resource://com.example.walter.mobileapp/"+R.drawable.email))
                        .into(pitchImage);
            }
            pitchCover.setText("Covered");
            if(!currentPitches.get(position).isCovered())
                pitchCover.setText("Not Covered");



            return convertView;

        }
    }
}
