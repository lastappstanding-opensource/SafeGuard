package com.americanaeuroparobotics.safeguard.activities;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.americanaeuroparobotics.safeguard.R;
import com.americanaeuroparobotics.safeguard.data.POI;
import com.americanaeuroparobotics.safeguard.services.MapAPIConsumer;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public abstract class POIActivity extends AbstractMapActivity {
    protected SeekBar seekBarRadius;
    protected MapAPIConsumer api;
    protected int maxRadius;
    protected TextView textViewProgress;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.layout.activity_poi);
        this.textViewProgress = findViewById(R.id.textViewRadius);
        this.seekBarRadius = findViewById(R.id.seekBarRadius);
        seekBarRadius.setMin(10);
        seekBarRadius.setMax(100);
        seekBarRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                zoomCamera(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int dist = seekBar.getProgress();
                if (dist > maxRadius){
                    maxRadius = dist;
                    updateMarkers(dist);
                }
            }
        });

        this.api = new MapAPIConsumer(this);
    }

    protected void zoomCamera(int progress){
        double zoom = log2(31250/progress);
        if (map != null) map.moveCamera(CameraUpdateFactory.zoomTo((int)zoom));
        textViewProgress.setText("Search radius: " + progress + "km");
    }


    @Override
    protected void respondToClick(LatLng coords) {

    }

    @Override
    protected void respondToLongClick(LatLng coords) {

    }

    protected abstract void updateMarkers(int distance);

    private double log2(double in){
        return Math.log(in)/Math.log(2);
    }

    protected void processLocations(List<POI> pois) {
        for (POI poi : pois){
            new Handler(this.getMainLooper()).post(() ->
                    this.markers.add(map.addMarker(new MarkerOptions()
                            .position(new LatLng(poi.getCoords().latitude, poi.getCoords().longitude))
                            .title(poi.getName()))));
        }
    }
}
