package com.americanaeuroparobotics.safeguard.activities;

import androidx.annotation.RequiresApi;

import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.americanaeuroparobotics.safeguard.services.MapAPIConsumer;
import com.google.android.gms.maps.model.LatLng;

public class TestingActivity extends POIActivity {
    private MapAPIConsumer api;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.api = new MapAPIConsumer(this);
    }

    @Override
    protected void respondToClick(LatLng coords) {

    }

    @Override
    protected void respondToLongClick(LatLng coords) {

    }

    @Override
    protected void addMarkers() {
        map.getUiSettings().setScrollGesturesEnabled(false);
        maxRadius = 100;
        seekBarRadius.setProgress(100);
        gps.getLastLocation().addOnSuccessListener(this::getLocations);
        new Handler(Looper.getMainLooper()).post(()->zoomCamera(100));
    }

    @Override
    protected void updateMarkers(int distance) {

    }

    private void getLocations(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        api.getHospitals(100,new LatLng(lat, lng), this::processLocations);
    }
}
