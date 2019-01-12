package com.example.walter.mobileapp.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.walter.mobileapp.object.Match;
import com.example.walter.mobileapp.R;
import com.example.walter.mobileapp.utility.StaticInstance;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class InfoMatch extends Fragment {

    View fragmentView;
    Match handledMatch;
    String pitchCode;
    String owner;

    public InfoMatch() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.info_match, container, false);
        Match handledMatch = (Match) getArguments().get("match");
        pitchCode = handledMatch.getPitchCode();
        owner = handledMatch.getPitchOwner();

        loadImage(((ImageView) (fragmentView.findViewById(R.id.pitchImage))));
        View buttonNotUsed = fragmentView.findViewById(R.id.confirm);
        ((ViewGroup) (buttonNotUsed.getParent())).removeView(buttonNotUsed);
        ((TextView) (fragmentView.findViewById(R.id.date))).setText(handledMatch.getDate());
        ((TextView) (fragmentView.findViewById(R.id.time))).setText(handledMatch.getTime());
        ((TextView) (fragmentView.findViewById(R.id.bookingby))).setText(handledMatch.getManager());
        ((TextView) (fragmentView.findViewById(R.id.registered_))).setText(String.valueOf(handledMatch.getRegistered().size()));
        ((TextView) (fragmentView.findViewById(R.id.address_view))).setText(String.valueOf(handledMatch.getAddress()));
        ((TextView) (fragmentView.findViewById(R.id.covered))).setText(String.valueOf(handledMatch.isCovered()));
        return fragmentView;
    }

    private void loadImage(final ImageView pitchImage) {
        StaticInstance.mStorageRef.child("pitch/" + owner + pitchCode).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri imageUri) {
                        if (imageUri != null) {
                            Glide.with(fragmentView)
                                    .load(imageUri)
                                    .into(pitchImage);
                        } else {
                            Glide.with(fragmentView)
                                    .load(Uri.parse("android.resource://com.example.walter.mobileapp/" + R.drawable.login))
                                    .into(pitchImage);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {

                    }
                });
    }

}
