package com.americanaeuroparobotics.safeguard.activities;

import androidx.annotation.RequiresApi;

import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.maps.model.LatLng;

public class SuppliesActivity extends POIActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        if (center == null) gps.getLastLocation().addOnSuccessListener(this::getLocations);
        else api.getStores(10,center, this::processLocations);
        maxRadius = 10;
        seekBarRadius.setProgress(10);
        new Handler(Looper.getMainLooper()).post(()->zoomCamera(10));
    }

    @Override
    protected void updateMarkers(int distance) {
        if (center != null) api.getStores(distance,center,this::processLocations);
    }

    private void getLocations(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        api.getStores(10, new LatLng(lat, lng), this::processLocations);
    }
}
