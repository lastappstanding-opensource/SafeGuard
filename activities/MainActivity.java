package com.americanaeuroparobotics.safeguard.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.americanaeuroparobotics.safeguard.App;
import com.americanaeuroparobotics.safeguard.R;
import com.americanaeuroparobotics.safeguard.data.User;
import com.americanaeuroparobotics.safeguard.data.enums.Exposure;
import com.americanaeuroparobotics.safeguard.services.DataService;
import com.americanaeuroparobotics.safeguard.services.LocationService;
import com.americanaeuroparobotics.safeguard.services.TrackerService;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {


    private TextView textViewName;


    private TextView textViewExposure;

    private Button buttonMap;
    private Button buttonLog;
    private Button buttonArea;
    private Button buttonTransit;
    private Button buttonCough;
    private Button buttonSupplies;
    private Button buttonTesting;
    private Button buttonInfo;
    private Button buttonSettings;

    private DataService db;

    private PopupWindow splash, popup;
    private boolean active;

    private static final int REQUEST_PERMISSIONS=1;

    private Thread exposureUpdater;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);




        this.textViewName = findViewById(R.id.textViewName);

        this.textViewExposure = findViewById(R.id.textViewExposure);

        this.buttonMap = findViewById(R.id.buttonMap);
        buttonMap.setOnClickListener(v->startMapActivity(MapActivity.class));
        this.buttonLog = findViewById(R.id.buttonLog);
        buttonLog.setOnClickListener(v->startActivity(LogActivity.class));
        this.buttonArea = findViewById(R.id.buttonArea);
        buttonArea.setOnClickListener(v->startMapActivity(AreaActivity.class));
        this.buttonTransit = findViewById(R.id.buttonTransit);
        buttonTransit.setOnClickListener(v->startActivity(TransitActivity.class));
        this.buttonCough = findViewById(R.id.buttonCough);
        buttonCough.setOnClickListener(v->startActivity(CoughActivity.class));
        this.buttonSupplies = findViewById(R.id.buttonSupplies);
        buttonSupplies.setOnClickListener(v->startMapActivity(SuppliesActivity.class));
        this.buttonTesting = findViewById(R.id.buttonTesting);
        buttonTesting.setOnClickListener(v->startMapActivity(TestingActivity.class));
        this.buttonInfo = findViewById(R.id.buttonInfo);
        buttonInfo.setOnClickListener(v->startActivity(InfoActivity.class));
        this.buttonSettings = findViewById(R.id.buttonSettings);
        buttonSettings.setOnClickListener(v->startActivity(SettingsActivity.class));

            //new Thread(this::getProfilePicture).start();
            String[] requiredPermissions = checkPermissions();
            if (requiredPermissions.length > 0){
                ActivityCompat.requestPermissions(this, requiredPermissions,REQUEST_PERMISSIONS);
            }
            this.db = new DataService();




        logIn();

        WorkManager.getInstance(this).cancelAllWork();
        PeriodicWorkRequest trackerRequest = new PeriodicWorkRequest.Builder(TrackerService.class, 15, TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueue(trackerRequest);
    }




    protected void startActivity(Class c){
        startActivity(new Intent(this,c));
    }

    private void startMapActivity(Class c){
        if (App.getUser() != null){
            if (App.getUser().isAllowsLocationTracking()){
                startActivity(new Intent(this,c));
            }
            else accessDenied();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (splash != null) {
            splash.dismiss();
            splash = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();



        if (App.getUser() != null){
            loggedIn();
        }
        this.active = true;
        //App.print("Launching workRequest");
        //OneTimeWorkRequest trackerRequest = new OneTimeWorkRequest.Builder(TrackerService.class).build();
        new Thread(this::updateExposureView).start();
    }

    @Override
    protected void onPause(){
        super.onPause();
        this.active = false;
        if (popup != null) popup.dismiss();
    }




    @RequiresApi(api = Build.VERSION_CODES.N)
    private String[] checkPermissions() {
        String[] permissions = {
                Manifest.permission.FOREGROUND_SERVICE
        };
        return Arrays.stream(permissions)
                .filter(p->ContextCompat.checkSelfPermission(this,p) != PackageManager.PERMISSION_GRANTED)
                .toArray(String[]::new);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == permissions.length){
                    for (int i = 0; i < grantResults.length; i++){
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            if (permissions[i].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)){
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) System.exit(0);
                            }
                            else System.exit(0);
                        }
                    }
                }
                break;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logIn(){

        User u = App.getUser();
        if (u == null) {
            splash = App.loadSplash();
            findViewById(R.id.layout).post(()->splash.showAtLocation(findViewById(R.id.layout), Gravity.CENTER,0,0));
            String userId = getSharedPreferences("meta",Context.MODE_PRIVATE).getString("userId", null);
            System.out.println("Found userId in main activity: "+userId);
            if (userId == null) {
                Intent login = new Intent(this, LoginActivity.class);
                startActivity(login);
            } else {
                db.getUserInfo(userId, d -> {
                    User user = d.toObject(User.class);
                    if (user == null) startActivity(new Intent(MainActivity.this,LoginActivity.class));
                    else {
                        user.setId(userId);
                        App.setUser(user);
                        loggedIn();
                        new Handler(Looper.getMainLooper()).post(()->splash.dismiss());
                    }
                });
            }
        }
        else {
            loggedIn();
        }
    }

    private void loggedIn(){
        User user = App.getUser();
        if (!user.isAgreedToTerms()) startActivity(new Intent(MainActivity.this,SetupActivity.class));
        //String firstName = user.getFirstName();
        //if (firstName == null) firstName = "anonymous user";
        //textViewName.setText("Welcome " + firstName + "!");

        //Check exposure. Every hour in prod, every 15 minutes in testing
        //PeriodicWorkRequest trackerRequest = new PeriodicWorkRequest.Builder(TrackerService.class, 1, TimeUnit.HOURS).build();
        if (user.isAllowsLocationTracking()){
            Intent locationIntent = new Intent(this,LocationService.class);
            ContextCompat.startForegroundService(this,locationIntent);
        }
    }

    private void updateExposureView(){
        while (active) {
            try {
                Exposure exposure = TrackerService.getExposure();
                if (exposure != null) {
                    //App.print(exposure.toString());
                    new Handler(Looper.getMainLooper()).post(() -> {
                        TextView exposureView = findViewById(R.id.textViewExposure);
                        exposureView.setText(exposure.toString());
                        exposureView.getBackground().setTint(exposure.getColor());
                    });
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void accessDenied(){
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        // Inflate the custom layout/view
        View popupView = inflater.inflate(R.layout.popup_location_tracking,null);
        popup = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        Button buttonBack = popupView.findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v->popup.dismiss());
        Button buttonSettings = popupView.findViewById(R.id.buttonSettings);
        buttonSettings.setOnClickListener(v->startActivity(new Intent(MainActivity.this, AccountActivity.class)));
        findViewById(R.id.layout).post(()->popup.showAtLocation(findViewById(R.id.layout), Gravity.CENTER,0,0));
    }

}
