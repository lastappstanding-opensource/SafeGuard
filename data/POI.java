package com.americanaeuroparobotics.safeguard.data;

import com.google.android.gms.maps.model.LatLng;

public class POI {
    private String name;
    private LatLng coords;
    private int waitTime;


    public POI(String name, LatLng coords, int waitTime) {
        this.name = name;
        this.coords = coords;
        this.waitTime = waitTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getCoords() {
        return coords;
    }

    public void setCoords(LatLng coords) {
        this.coords = coords;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }
}
