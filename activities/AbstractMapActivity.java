package com.americanaeuroparobotics.safeguard.activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.americanaeuroparobotics.safeguard.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMapActivity extends AppCompatActivity  implements OnMapReadyCallback {

    protected GoogleMap map;
    protected FusedLocationProviderClient gps;
    protected Geocoder addressDecoder;
    protected List<Marker> markers;
    protected LatLng center;

    private PopupWindow popup;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addressDecoder = new Geocoder(this);
        this.markers = new ArrayList<>();
        this.gps = LocationServices.getFusedLocationProviderClient(this);
    }

    protected void initView(int view){
        setContentView(view);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync( this);
    }
    @Override
    protected void onPause(){
        super.onPause();
        if (popup != null) popup.dismiss();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(AbstractMapActivity.this);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(AbstractMapActivity.this);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(AbstractMapActivity.this);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });
        map.setOnMapClickListener(this::respondToClick);
        map.setOnMapLongClickListener(this::respondToLongClick);
        gps.getLastLocation().addOnSuccessListener(this, this::moveToLocation);
        addMarkers();
    }

    private void moveToLocation(Location loc){
        if (loc != null){
            this.center = new LatLng(loc.getLatitude(), loc.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(loc.getLatitude(), loc.getLongitude())));
        }
    }



    abstract protected void respondToClick(LatLng coords);
    abstract protected void respondToLongClick(LatLng coords);
    abstract protected  void addMarkers();
}
