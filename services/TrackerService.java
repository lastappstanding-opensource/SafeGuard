package com.americanaeuroparobotics.safeguard.services;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.americanaeuroparobotics.safeguard.App;
import com.americanaeuroparobotics.safeguard.R;
import com.americanaeuroparobotics.safeguard.activities.MainActivity;
import com.americanaeuroparobotics.safeguard.data.GPSLocation;
import com.americanaeuroparobotics.safeguard.data.User;
import com.americanaeuroparobotics.safeguard.data.enums.Diagnosis;
import com.americanaeuroparobotics.safeguard.data.enums.Exposure;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TrackerService extends Worker {

    private DataService db;

    private int expectedCallbacks, callbacks;
    private List<Pair<Diagnosis, GPSLocation>> locationsIll;
    private static Exposure exposure;
    private Context context;

    private List<List<User>> illnesses;
    private List<GPSLocation> userLocations;

    public TrackerService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.db = new DataService();
        this.locationsIll = new ArrayList<>();
        this.userLocations = new ArrayList<>();
        this.context = context;
        exposure = Exposure.UNKNOWN;
        illnesses = new ArrayList<>();
        if (App.getUser() != null){
            db.getLocationsUser(App.getUser().getId(),l->userLocations = l);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized void processLocationsIll(Pair<Diagnosis, Stream<GPSLocation>> locations) {
        App.print("In processLocationsIll");
        callbacks++;
        locations.second
                .forEach(l->locationsIll.add(new Pair(locations.first,l)));
        if (callbacks == expectedCallbacks){
            callbacks = 0;
            expectedCallbacks = 0;
            //App.print("Before wait for usersLocations");

            if (userLocations.size() == 0){
                db.getLocationsUser(App.getUser().getId(),l->{
                    userLocations = l;
                    findLocationsExposure();
                });
            }
            else findLocationsExposure();
        }
    }

    private void findLocationsExposure() {
        App.print("In findLocationsExposure");
        Diagnosis worstDiagnosis = Diagnosis.NEGATIVE;
        int i = 0;
        //App.print("Number of user locations: " + userLocations.size());
        while (worstDiagnosis != Diagnosis.POSITIVE && i < userLocations.size()){
            GPSLocation locationUser = userLocations.get(i);
            GeoPoint coordsUser = locationUser.getLocation();
            long startTimeUser = locationUser.getStartTime().toDate().getTime();
            long endTimeUser = locationUser.getEndTime().toDate().getTime();
            int j = 0;
            //App.print("Size locationsIll: " + locationsIll.size());
            while (worstDiagnosis != Diagnosis.POSITIVE && j < locationsIll.size()){
                //App.print("In locationsIllLoop, diagnosis: " + locationsIll.get(j).first);
                GPSLocation locationIll = locationsIll.get(j).second;
                Diagnosis diagnosis = locationsIll.get(j).first;
                GeoPoint coordsIll = locationIll.getLocation();
                long startTimeIll = locationIll.getStartTime().toDate().getTime();
                long endTimeIll = locationIll.getEndTime().toDate().getTime();
                //For now don't take altitude into account
                if (Math.abs(coordsUser.getLatitude() - coordsIll.getLatitude()) < 0.0001
                    && Math.abs(coordsUser.getLongitude() - coordsIll.getLongitude()) < 0.0001
                    && Math.abs(locationIll.getSpeed() - locationUser.getSpeed()) < 1.0
                    && ((endTimeUser > startTimeIll && endTimeUser < endTimeIll)
                        || (startTimeUser < endTimeIll && startTimeUser > startTimeIll))) {
                    //App.print("Result of comparison: " + worstDiagnosis.compareTo(diagnosis) + ", Old worst diagnosis: " + worstDiagnosis + ", new worst diagnosis: "+diagnosis);
                    if (worstDiagnosis.compareTo(diagnosis) < 0){
                        //App.print("Old worst diagnosis: " + worstDiagnosis + ", new worst diagnosis: "+diagnosis);
                        worstDiagnosis = diagnosis;
                    }
                }
                j++;
            }
            i++;
        }
        exposure = worstDiagnosis.getExposure();
        if (exposure == Exposure.UNKNOWN) exposure = Exposure.NONE;
        App.print("Exposure at end of processing data: "+exposure.getDescription());
        if (exposure != Exposure.CERTAIN && illnesses.size() > 0){
            List<User> next = illnesses.get(0);
            expectedCallbacks = next.size();
            //App.print("New expected callbacks:" + expectedCallbacks);
            if (next != null) {
                illnesses.remove(0);
                db.getLocationsIll(next, this::processLocationsIll);
            }
        }
        else illnesses : new ArrayList<>();
    }


    @NonNull
    @Override
    public Result doWork() {
        if (App.getUser() != null) {
            App.print("User is not Null!");
            db.getAllUsers(s -> {
                List<List<User>> data = new ArrayList();
                List<User> searchData = s.filter(u->u.getIllnesses() != null)
                        .peek(u -> u.setIllnesses(u.getIllnesses().stream()
                                                                  .filter(i->i.getDiagnosis().compareTo(Diagnosis.NEGATIVE) > 0)
                                                                  .collect(Collectors.toList())))
                        .filter(u->u.getIllnesses().size() > 0)
                        .collect(Collectors.toList());

                List<User> entries = new ArrayList<>();
                entries.addAll(searchData);
                int increment = entries.size() / 10;
                App.print("Increment: " + increment);
                if (increment > 0) {
                    for (int i = 0; i < 10; i++) {
                        int end = i == 9 ? entries.size() : i + 1 * increment;
                        data.add(entries.subList(i * increment, end));
                    }
                } else {
                    data.add(searchData);
                }
                    this.illnesses = data;
                    List<User> first = illnesses.get(0);
                    expectedCallbacks = 0;
                    expectedCallbacks = first.stream()
                                             .reduce(0,(r,u)->r+u.getIllnesses().size(),(i,ii)->i+ii);
                    data.remove(0);
                    if (expectedCallbacks == 0 ){
                        exposure = Exposure.NONE;
                    }
                    else db.getLocationsIll(first, this::processLocationsIll);
            });
            if (App.getUser().isAllowsLocationTracking()) showNotification();
        }
        return Result.success();
    }

    private void showNotification(){
        if (exposure != Exposure.NONE && !App.isForeground()){
            Intent notificationIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,
                    1,
                    notificationIntent,
                    0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, App.CHANNEL_ID)
                    .setSmallIcon(R.drawable.green)
                    .setContentTitle(App.NAME)
                    .setContentText(this.exposure.toString())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
        }
    }

    public static Exposure getExposure(){
        return exposure;
    }
}
