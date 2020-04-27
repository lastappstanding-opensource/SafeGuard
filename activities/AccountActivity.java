package com.americanaeuroparobotics.safeguard.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.americanaeuroparobotics.safeguard.App;
import com.americanaeuroparobotics.safeguard.R;
import com.americanaeuroparobotics.safeguard.data.User;
import com.americanaeuroparobotics.safeguard.data.enums.Subscription;
import com.americanaeuroparobotics.safeguard.services.DataService;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class AccountActivity extends AppCompatActivity {

    private ImageView profilePicture;
    private TextView textViewName;
    private TextView textViewSubscription;
    private Switch switchAllowLocationTracking;
    private Switch switchAllowNotifications;
    private Button buttonDeleteLocationData;
    private Button buttonLogOut;

    private DataService db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        this.profilePicture = findViewById(R.id.profilePicture);
        this.textViewName = findViewById(R.id.textViewName);
        this.textViewSubscription = findViewById(R.id.textViewSubscription);
        this.switchAllowLocationTracking = findViewById(R.id.switchLocationTracking);
        switchAllowLocationTracking.setOnCheckedChangeListener(this::saveAllowTracking);
        this.switchAllowNotifications = findViewById(R.id.switchNotifications);
        switchAllowNotifications.setOnCheckedChangeListener(this::saveAllowNotifications);
        this.buttonDeleteLocationData = findViewById(R.id.buttonDeleteLocationData);
        buttonDeleteLocationData.setOnClickListener(this::deleteUserLocations);
        this.buttonLogOut = findViewById(R.id.buttonLogOut);
        this.buttonLogOut.setOnClickListener(this::logOut);

        this.db = new DataService();
    }

    private void saveAllowTracking(CompoundButton buttonView, boolean isChecked) {
        App.getUser().setAllowsLocationTracking(isChecked);
        db.saveCurrentUser();
        Toast.makeText(this,"Settings Updated!",Toast.LENGTH_SHORT).show();
    }

    private void saveAllowNotifications(CompoundButton buttonView, boolean isChecked) {
        App.getUser().setAllowsNotifications(isChecked);
        db.saveCurrentUser();
        Toast.makeText(this,"Settings Updated!",Toast.LENGTH_SHORT).show();
    }

    private void deleteUserLocations(View v){
        db.deleteUserLocations(App.getUser().getId());
        Toast.makeText(this,"Deleted All of Your Location Data!",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume(){
        super.onResume();
        User user = App.getUser();
        String name = "Anonymous User";
        if (user.getFirstName() != null) name = user.getFirstName() + " " + user.getLastName();
        textViewName.setText(name);
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedInFb = accessToken != null && !accessToken.isExpired();
        if (isLoggedInFb){
            new Thread(this::getProfilePicture).start();
        }
        if (user.getSubscription() == null){
            user.setSubscription(Subscription.FREE);
            db.saveCurrentUser();
        }
        textViewSubscription.setText(user.getSubscription().toString());
        switchAllowLocationTracking.setChecked(user.isAllowsLocationTracking());
        switchAllowNotifications.setChecked(user.isAllowsNotifications());
    }

    private void logOut(View v){
        SharedPreferences prefs = getSharedPreferences("meta",MODE_PRIVATE);
        String id = prefs.getString("userId",null);
        if (id != null){
            prefs.edit()
                    .putString("userId",null)
                    .commit();
            App.setUser(null);
            LoginManager.getInstance().logOut();
            Toast.makeText(this,"Successfully Logged Out!",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AccountActivity.this, LoginActivity.class));
        }
        else{
            Toast.makeText(this,"Already Logged Out!",Toast.LENGTH_SHORT).show();
        }
    }

    private void getProfilePicture(){
        try {
            InputStream in = (InputStream) new URL("https://graph.facebook.com/"+ AccessToken.getCurrentAccessToken().getUserId() +"/picture?type=large").getContent();
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            new Handler(Looper.getMainLooper()).post(()->profilePicture.setImageBitmap(bitmap));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
