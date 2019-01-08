package com.example.walter.mobileapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class OwnerPitches extends AppCompatActivity {

    FirebaseFirestore db = StaticInstance.getInstance();
    StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    DatabaseReference myRef = StaticInstance.getDatabase().getReference("booking/");
    ArrayList<Pitch> pitches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_pitches);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final ListView listView = findViewById(R.id.pitches);
        pitches = new ArrayList<>();
        db.collection("pitch")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            final OwnerPitches.CustomAdapter customAdapter = new OwnerPitches.CustomAdapter(getApplicationContext());
                            listView.setAdapter(customAdapter);

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getId();
                                String city = document.get("city").toString();
                                String address= document.get("address").toString() + " , " + city;
                                boolean covered=(boolean)document.get("covered");
                                double price = (double) (document.get("price"));
                                final Pitch currentPitch = new Pitch(id, address,price,covered,city,document.get("owner").toString());
                                Log.e("cerco in ","pitch/"+document.get("code"));

                                mStorageRef.child("pitch/" + document.get("owner")+document.get("code")).getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Log.e("il mio uri è",uri.toString());
                                                currentPitch.setUri(uri);
                                                customAdapter.notifyDataSetChanged();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                // Handle any errors
                                            }
                                        });

                                myRef.child(document.get("code").toString()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        dataSnapshot.getValue();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        // Failed to read value
                                        Log.e("Hey", "Failed to read app title value.", error.toException());
                                    }
                                });
                                currentPitch.initWithoutThese(new ArrayList<String>());
                                pitches.add(currentPitch);

                            }
                        } else {
                            Log.w("", "Error getting documents.", task.getException());
                        }
                    }
                });

    }

    class CustomAdapter extends BaseAdapter {

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
            convertView = inflater.inflate(R.layout.custom_owner_item, parent, false);

            TextView pitchAddress = convertView.findViewById(R.id.pitchAddress);
            TextView pitchPrice = convertView.findViewById(R.id.pricePitch);
            TextView pitchCover = convertView.findViewById(R.id.pitchCover);
            //Spinner availableTime = convertView.findViewById(R.id.pitchTime);
            Button modify = convertView.findViewById(R.id.modifyButton);
            final int index = position;
            modify.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                   Pitch current = pitches.get(index);

                   Intent intent = new Intent(getActivity(), ModifyPitch.class);
                   intent.putExtra("id", current.getId());
                   intent.putExtra("address", current.getAddress());
                   intent.putExtra("price", String.valueOf(current.getPrice()));
                   intent.putExtra("covered", String.valueOf(current.isCovered()));
                   intent.putExtra("city", current.getCity());
                   startActivity(intent);
                }
            });

            /*try {
                Field popup = Spinner.class.getDeclaredField("mPopup");
                popup.setAccessible(true);
                // Get private mPopup member variable and try cast to ListPopupWindow
               // android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(availableTime);
                // Set popupWindow height to 500px
                popupWindow.setHeight(350);
            }
            catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            }*/
           // availableTime.setAdapter(pitches.get(position).getAvailableTime());
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
                        .load(Uri.parse("android.resource://com.example.walter.mobileapp/"+R.drawable.login))
                        .into(pitchImage);
            }
            pitchCover.setText("Covered");
            if(!pitches.get(position).isCovered())
                pitchCover.setText("Not Covered");

            return convertView;

        }
    }

    private Context getActivity() {
        return this;
    }

}
