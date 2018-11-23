package com.example.walter.mobileapp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;

public class CreateMatch extends AppCompatActivity {

    TextView dateView;
    Spinner dropdown;
    DatePickerDialog.OnDateSetListener mDateSetListener;
    FirebaseFirestore db = StaticDbInstance.getInstance();

    ArrayList<String> addresses;
    ArrayList<Boolean> covered;
    ArrayList<Double> prices;
    ArrayList<Image> images;
    ArrayList<ArrayList<String>> time;


    private Context getActivity() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        addresses = new ArrayList<>();
        covered = new ArrayList<>();
        prices = new ArrayList<Double>();
        images = new ArrayList<>();
        time = new ArrayList<>();

        db.collection("pitch")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int size = task.getResult().size();
                            ArrayList<String> items = new ArrayList<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String city = document.get("city").toString();
                                if(!items.contains(city)) {
                                    items.add(city);
                                }

                                addresses.add(document.get("address").toString()+ " , " +city);
                                covered.add((boolean)document.get("covered"));
                                prices.add((double)(document.get("price")));
                            }

                            ArrayAdapter adapter = new ArrayAdapter<>(getActivity(), R.layout.spinneritem, items);
                            dropdown.setAdapter(adapter);

                            CustomAdapter customAdapter = new CustomAdapter(getApplicationContext());
                            listView.setAdapter(customAdapter);
                        } else {
                            Log.w("", "Error getting documents.", task.getException());
                        }
                    }
                });


    }

    class CustomAdapter extends BaseAdapter{

        Context context;

        public CustomAdapter(Context applicationContext) {
            context = applicationContext;
        }

        @Override
        public int getCount() {
            return addresses.size();
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

            ImageView pitchImage = convertView.findViewById(R.id.pitchImage);
            TextView pitchAddress = convertView.findViewById(R.id.pitchAddress);
            TextView pitchPrice = convertView.findViewById(R.id.pricePitch);
            TextView pitchCover = convertView.findViewById(R.id.pitchCover);

            pitchImage.setImageResource(R.drawable.ic_launcher_background);
            pitchPrice.setText(prices.get(position).toString() + "€");
            pitchAddress.setText(addresses.get(position));

            pitchCover.setText("Covered");
            if(!covered.get(position))
                pitchCover.setText("Not Covered");

            return convertView;

        }
    }
}
