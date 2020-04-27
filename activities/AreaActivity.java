package com.americanaeuroparobotics.safeguard.activities;

import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;

import com.americanaeuroparobotics.safeguard.R;
import com.americanaeuroparobotics.safeguard.data.GPSLocation;
import com.americanaeuroparobotics.safeguard.data.enums.Diagnosis;
import com.americanaeuroparobotics.safeguard.services.DataService;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.GeoPoint;


public class AreaActivity extends AbstractMapActivity {

    private DataService db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.layout.activity_map);
        this.db = new DataService();
    }

    @Override
    protected void respondToClick(LatLng coords) {

    }

    @Override
    protected void respondToLongClick(LatLng coords) {

    }

    @Override
    protected void addMarkers() {
        db.getRecentLocationsIll(this::addLocationsMap);
    }

    private void addLocationsMap(Pair<Diagnosis, GPSLocation> pair) {
        gps.getLastLocation().addOnSuccessListener(l->{
            GeoPoint loc = pair.second.getLocation();
            if (Math.abs(loc.getLatitude() - l.getLatitude()) < 1.0
            &&  Math.abs(loc.getLongitude() - l.getLongitude()) < 1.0){
                new Handler(this.getMainLooper()).post(() ->
                        this.markers.add(map.addMarker(new MarkerOptions()
                                .position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                                .title(getTitle(pair.first)))));
            }
        });
    }

    private String getTitle(Diagnosis diagnosis) {
        switch (diagnosis) {
            case UNKNOWN:
                return "Possible";
            case POSITIVE:
                return "Certain";
            default:
                return "";
        }
    }
}
