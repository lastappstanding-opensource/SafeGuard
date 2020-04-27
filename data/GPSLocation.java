package com.americanaeuroparobotics.safeguard.data;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

public class GPSLocation {
    private GeoPoint location;
    private double speed;
    private Timestamp startTime;
    private Timestamp endTime;
    private double altitude;

    public GPSLocation(){

    }

    public GPSLocation(GeoPoint location, double speed, Timestamp startTime, Timestamp endTime, double altitude) {
        this.location = location;
        this.speed = speed;
        this.startTime = startTime;
        this.endTime = endTime;
        this.altitude = altitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public double getSpeed() {
        return speed;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    @Override
    public boolean equals(Object o){
        return (o instanceof GPSLocation && ((GPSLocation) o).getStartTime().toDate().getTime() == this.getStartTime().toDate().getTime());
    }
}
