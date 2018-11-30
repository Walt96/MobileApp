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
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
    Spinner dropdown;
    DatePickerDialog.OnDateSetListener mDateSetListener;
    FirebaseFirestore db = StaticInstance.getInstance();
    StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    DatabaseReference myRef = StaticInstance.getDatabase().getReference("booking");
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    StorageReference reference;

    ArrayList<Pitch> pitches;

    int index = 0;


    private Context getActivity() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reference = FirebaseStorage.getInstance().getReference();

        setContentView(R.layout.activity_create_match);
        dateView = findViewById(R.id.datePicker);
        dropdown = findViewById(R.id.pickCity);
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
                String date = month + "/" + day + "/" + year;
                dateView.setText(date);
            }

        };

        final ListView listView = findViewById(R.id.pitchList);

        pitches = new ArrayList<>();
        db.collection("pitch")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<String> items = new ArrayList<>();
                            final CustomAdapter customAdapter = new CustomAdapter(getApplicationContext());
                            listView.setAdapter(customAdapter);

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String city = document.get("city").toString();
                                if (!items.contains(city)) {
                                    items.add(city);
                                }
                                String address = document.get("address").toString() + " , " + city;
                                boolean covered = (boolean) document.get("covered");
                                double price = (double) (document.get("price"));
                                final Pitch currentPitch = new Pitch(address, price, covered);
                                Log.e("cerco in ", "pitch/" + document.get("code"));

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
                                final Date date = new Date();
                                Log.e("data",dateFormat.format(date));
                                StaticInstance.getInstance().collection("booking").document(document.get("code").toString()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable DocumentSnapshot snapshot,
                                                        @Nullable FirebaseFirestoreException e) {
                                        if(snapshot.get("prenotazioni") != null){
                                            ArrayList<String> nonDisponibili = new ArrayList<>();
                                            ArrayList<HashMap<String,Object>> prenotazioni = (ArrayList<HashMap<String,Object>>)snapshot.get("prenotazioni");
                                            for(HashMap<String,Object> prenotazione:prenotazioni){
                                                if(dateFormat.format(date).equals(prenotazione.get("data")))
                                                nonDisponibili.add(prenotazione.get("ora").toString());

                                            }
                                            currentPitch.initWithoutThese(nonDisponibili);
                                        }
                                    }
                                });
                                currentPitch.initWithoutThese(new ArrayList<String>());
                                pitches.add(currentPitch);

                            }
                            ArrayAdapter adapter = new ArrayAdapter<>(getActivity(), R.layout.spinneritem, items);
                            dropdown.setAdapter(adapter);

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

    class CustomAdapter extends BaseAdapter{

        Context context;

        public CustomAdapter(Context applicationContext) {
            context = applicationContext;
        }

        @Override
        public int getCount() {
            return pitches.size();
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
            availableTime.setAdapter(pitches.get(position).getAvailableTime());
            pitchPrice.setText("Price: "+ String.valueOf(pitches.get(position).getPrice()) + "€");
            pitchAddress.setText(pitches.get(position).getAddress());
            ImageView pitchImage = convertView.findViewById(R.id.pitchImage);
            Uri imageUri = pitches.get(position).getUri();
            if(imageUri!=null) {
                Glide.with(convertView)
                        .load(pitches.get(position).getUri())
                        .into(pitchImage);
            }
            else {
                Glide.with(convertView)
                        .load(Uri.parse("android.resource://com.example.walter.mobileapp/"+R.drawable.email))
                        .into(pitchImage);
            }
            pitchCover.setText("Covered");
            if(!pitches.get(position).isCovered())
                pitchCover.setText("Not Covered");



            return convertView;

        }
    }
}
