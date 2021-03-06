package com.example.walter.mobileapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.walter.mobileapp.object.Match;
import com.example.walter.mobileapp.R;

public class HandleMatches extends AppCompatActivity {

    private Match handledMatch;
    Bundle bundle = new Bundle();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.info:
                    bundle.putSerializable("match", handledMatch);
                    replaceFragment(new InfoMatch());
                    return true;
                case R.id.teams:
                    bundle.putSerializable("match", handledMatch);
                    replaceFragment(new ShowTeams());
                    return true;
                case R.id.addplayer:
                    bundle.putSerializable("match", handledMatch);
                    replaceFragment(new AddPartecipant());
                    return true;
            }
            return false;
        }
    };

    private void replaceFragment(Fragment newFragment) {
        newFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, newFragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handle_matches);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Intent intent = getIntent();
        handledMatch = (Match) intent.getSerializableExtra("match");
        navigation.setSelectedItemId(R.id.info);

    }

}
