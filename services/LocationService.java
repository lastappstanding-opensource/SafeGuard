package com.americanaeuroparobotics.safeguard.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.americanaeuroparobotics.safeguard.App;
import com.americanaeuroparobotics.safeguard.R;
import com.americanaeuroparobotics.safeguard.activities.MainActivity;
import com.americanaeuroparobotics.safeguard.data.GPSLocation;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;


public class LocationService extends Service{
    private DataService db;
    private LocationRequest locationRequest;
    private SortedSet<GPSLocation> buffer;
    private long timePassed;
    private long updateInterval;
    private double lastKnownAltitude;
    private int numMerged;

    @Override
    public void onCreate() {
        super.onCreate();
        this.db = new DataService();
        this.buffer = new TreeSet<GPSLocation>((l1,l2)->(int)(l1.getStartTime().toDate().getTime()-l2.getStartTime().toDate().getTime()));
        this.timePassed = 0;
        this.updateInterval = 60000;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0,
                notificationIntent,
                0);

        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setContentTitle("Location Data")
                .setContentText("Tracking your location to ensure your safety.")
                .setSmallIcon(R.drawable.green)
                .setContentIntent(pendingIntent)
                .build();

        this.locationRequest = LocationRequest.create();
        locationRequest.setInterval(60000);
        locationRequest.setFastestInterval(30000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (App.getUser() != null && App.getUser().isAllowsLocationTracking()) {
                    GPSLocation last = null;
                    if (buffer.size() > 0) last = buffer.last();
                    timePassed += updateInterval;
                    for (Location location : locationResult.getLocations()) {
                        GPSLocation newLocation = new GPSLocation(new GeoPoint(location.getLatitude(), location.getLongitude()),
                                location.getSpeed(),
                                new Timestamp(new Date(location.getTime())),
                                null,
                                location.getAltitude());
                        processLocation(newLocation);

                        //For testing we send buffer at each update, in prod we send it once per hour
                        sendBuffer();
                    /*if (timePassed > 3600000){
                        timePassed = 0;
                        sendBuffer();
                    }*/
                    }
                    GPSLocation newLast = null;
                    if (buffer.size() > 0) newLast = buffer.last();
                    if (newLast == last) increaseUpdateInterval();
                    else resetUpdateInterval();
                }
                else{
                    LocationServices.getFusedLocationProviderClient(LocationService.this).removeLocationUpdates(this);
                    LocationService.this.stopSelf();
                }
            }
        }

            ;

        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        startForeground(1,notification);


        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void processLocation(GPSLocation loc){
        //App.print("New location");
        GPSLocation last = null;
        if (buffer.size() > 0) last = buffer.last();
        if (loc.getAltitude() == 0) loc.setAltitude(lastKnownAltitude);
        if (last == null) addBuffer(loc);
        else {
            GeoPoint lastCoords = last.getLocation();
            GeoPoint newCoords = loc.getLocation();
            if (last.getEndTime() == null) {
                //Ignore altitude for now
                double diffLat = Math.abs(lastCoords.getLatitude() - newCoords.getLatitude());
                double diffLong = Math.abs(lastCoords.getLongitude() - newCoords.getLongitude());
                double hyst;
                if (last.getSpeed() == 0 && loc.getSpeed() == 0) hyst = 0.0001;
                else hyst = 0.00001;
                if (diffLat > hyst ||  diffLong > hyst){
                    last.setEndTime(loc.getStartTime());
                    //App.print("Location differs from old location");
                    addBuffer(loc);
                }
                else{
                    mergeLocations(last,loc);
                    //App.print("Location does not differ from old location");
                    //Weighted average, value of 4 is heuristic
                    lastKnownAltitude = last.getAltitude();
                }
            }
            else addBuffer(loc);
        }
    }

    private void mergeLocations(GPSLocation loc1, GPSLocation loc2){
        numMerged++;
        loc1.setAltitude((numMerged * loc1.getAltitude() + loc2.getAltitude())/(numMerged+1));
        GeoPoint newCoords = new GeoPoint((numMerged*loc1.getLocation().getLatitude() + loc2.getLocation().getLatitude())/(numMerged + 1),
                (numMerged * loc1.getLocation().getLongitude() + loc2.getLocation().getLongitude())/(numMerged + 1));
        loc1.setLocation(newCoords);
        //App.print("Merged locations");
    }

    private void addBuffer(GPSLocation loc){
        numMerged = 0;
        if (loc.getSpeed() >= 1){
            //App.print("Setting end time of new location");
            long endMilis = loc.getStartTime().toDate().getTime() + 10000;
            Timestamp endTime = new Timestamp(new Date(endMilis));
            loc.setEndTime(endTime);
        }
        lastKnownAltitude = loc.getAltitude();
        this.buffer.add(loc);
        App.print(printBuffer());
    }

    private String printBuffer(){
        String ret = "";
        for (GPSLocation loc : buffer){
            ret += "\nLocation: Latitude: "+loc.getLocation().getLatitude() + " Longitude: " + loc.getLocation().getLongitude() + "startTime: " + loc.getStartTime() + " endTime: " + loc.getEndTime() + " speed: " + loc.getSpeed() + " altitude: "+loc.getAltitude();
        }
        return ret;
    }

    private void sendBuffer(){
        GPSLocation last = null;
        if (buffer.size() > 0) last = buffer.last();
        if (last != null) {
            if (last.getEndTime() == null) buffer.remove(last);
            db.addLocations(buffer.stream());
            buffer.clear();
            if (last.getEndTime() == null) buffer.add(last);
        }
    }

    private void compactBuffer(){
        GPSLocation[] locs = (GPSLocation[]) buffer.toArray();
        int i = 0;
        while (i < locs.length){
            GPSLocation loc = locs[i];
            if (loc.getSpeed() == 0)
            i++;
        }
    }

    private void increaseUpdateInterval(){
        if (updateInterval < 600000){
            updateInterval *= 1.2;
            locationRequest.setInterval(updateInterval);
        }
    }
    private void resetUpdateInterval(){
        updateInterval = 60000;
        locationRequest.setInterval(updateInterval);
    }
}
