package com.example.walter.mobileapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class FacebookLogin extends AppCompatActivity {


    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private ProfileTracker mProfileTracker;
    private ShareDialog shareDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_login);

        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>(){

            @Override
            public void onSuccess(LoginResult loginResult) {
                final AccessToken token = loginResult.getAccessToken();
                Log.e("TAG", "Logged");

                if(Profile.getCurrentProfile() == null) {
                    mProfileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                            Profile profile = Profile.getCurrentProfile();
                            Log.v("facebook - profile", currentProfile.getFirstName());
                            Log.v("facebook - profile", profile.getFirstName());
                            Log.e("MATTEO SUCA", profile.getFirstName() + " " + profile.getLastName());
                            GraphRequest request = GraphRequest.newMeRequest(
                                    token,
                                    new GraphRequest.GraphJSONObjectCallback() {
                                        @Override
                                        public void onCompleted(JSONObject object, GraphResponse response) {
                                            Log.v("LoginActivity", response.toString());

                                            // Application code
                                            try {
                                                String id = object.getString("id");
                                                String email = object.getString("email");
                                                String birthday = object.getString("birthday"); // 01/31/1980 format
                                                Log.e("TAG", email + " " + birthday);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                            Bundle parameters = new Bundle();
                            parameters.putString("fields", "id,name,email,gender,birthday");
                            request.setParameters(parameters);
                            request.executeAsync();
                            mProfileTracker.stopTracking();
                        }
                    };
                    // no need to call startTracking() on mProfileTracker
                    // because it is called by its constructor, internally.
                } else {
                    Profile profile = Profile.getCurrentProfile();
                    Log.v("facebook - profile", profile.getFirstName());
                    Log.e("TAG", profile.getFirstName() + " " + profile.getLastName());
                    GraphRequest request = GraphRequest.newMeRequest(
                            loginResult.getAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {
                                    Log.v("LoginActivity", response.toString());

                                    // Application code
                                    try {
                                        String email = object.getString("email");
                                        String birthday = object.getString("birthday"); // 01/31/1980 format
                                        Log.e("TAG", email + " " + birthday);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });


    }

    Target target = new Target() {

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            Log.e("TAG", "Showing image");
            SharePhoto sharePhoto = new SharePhoto.Builder().setBitmap(bitmap).setCaption("Ho appena creato una partita! Scarica anche tu l'applicazione").build();
            if(ShareDialog.canShow(SharePhotoContent.class)) {
                SharePhotoContent content = new SharePhotoContent.Builder().addPhoto(sharePhoto).build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            Log.e("TAG", "Failed load bitmap");
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };


    public void sharepost(View w) {
        Log.e("TAG", "Clicked");
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Log.e("TAG", "Callback success");
                Toast.makeText(FacebookLogin.this, "Share Successful!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Log.e("TAG", "Callback cancel");
                Toast.makeText(FacebookLogin.this, "Share Cancel!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("TAG", error.getMessage());
                Toast.makeText(FacebookLogin.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        //Picasso.get().load("https://static.comicvine.com/uploads/scale_small/10/100647/6198653-batman+12.jpg").into(target);
       /* ShareLinkContent linkContent = new ShareLinkContent.Builder().setQuote("Quote").setContentUrl(Uri.parse("http://youtube.com")).build();
        if(ShareDialog.canShow(ShareLinkContent.class)) {

            shareDialog.show(linkContent);

        }*/

        ShareOpenGraphObject object = new ShareOpenGraphObject.Builder()
                .putString("og:type", "product")
                .putString("og:title", "Sample Course")
                .putString("og:description", "Ho appena creato ouna partita! Scarica anche tu l'applicazione!.")
                .putString("og:image", "https://static.comicvine.com/uploads/scale_small/10/100647/6198653-batman+12.jpg")
                .build();
        ShareOpenGraphAction action = new ShareOpenGraphAction.Builder()
                .setActionType("games.achieves")
                .putObject("product", object)
                .build();
        ShareOpenGraphContent content = new ShareOpenGraphContent.Builder()
                .setPreviewPropertyName("product")
                .setAction(action)
                .build();
        shareDialog.show(content);




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
