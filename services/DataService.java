package com.americanaeuroparobotics.safeguard.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import com.americanaeuroparobotics.safeguard.App;
import com.americanaeuroparobotics.safeguard.data.GPSLocation;
import com.americanaeuroparobotics.safeguard.data.User;
import com.americanaeuroparobotics.safeguard.data.enums.Diagnosis;
import com.americanaeuroparobotics.safeguard.data.transit.Transit;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataService {

    private FirebaseFirestore db;

    public DataService(){
        this.db = FirebaseFirestore.getInstance();

    }


    public void registerUser(User u, Consumer<DocumentReference> callback){
        CollectionReference users = db.collection("users");
        String id = u.getId();
        if (id == null){
            constructQuery(users
                    .add(u)
                    .addOnSuccessListener(r->callback.accept(r)));
        }
        else {
            constructQuery(users.document(id)
                    .set(u));
        }
    }

    public void getUserInfo(String id, Consumer<DocumentSnapshot> callback){
        DocumentSnapshot doc = null;
        constructQuery(db.collection("users")
                .document(id)
                .get()
                .addOnSuccessListener(d->callback.accept(d)));
    }

    private <T> Task<T> constructQuery(Task<T> task){
        return task.addOnFailureListener(e-> new Handler(Looper.getMainLooper()).post(()->System.out.println(e.getMessage())));
    }

   /* public void addLocation(GPSLocation gpsLocation) {
        if (App.getUser() == null) return;
        String id = App.getUser().getId();
        CollectionReference locCol = db.collection("users/" + id + "/locations");

        constructQuery(locCol.orderBy("startTime", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(d->processLocation(d,gpsLocation)));
    }*/

    public void addLocations(Stream<GPSLocation> locations){
        User u = App.getUser();
        if (u != null && u.getId() != null){
            WriteBatch batch = db.batch();
            String userId = u.getId();
            locations.forEach(l->batch.set(db.collection("users/"+userId+"/locations").document(),l));
            constructQuery(batch.commit());
        }
    }

    public void saveCurrentUser(){
        User user = App.getUser();
        constructQuery(db.document("users/"+user.getId()).set(user));
    }

    public void getAllUsers(Consumer<Stream<User>> userConsumer){
        constructQuery(db.collection("users")
                .get()
                .addOnSuccessListener(q->userConsumer.accept(q.getDocuments().stream()
                                                                                .map(d->{
                                                                                    User u = d.toObject(User.class);
                                                                                    u.setId(d.getId());
                                                                                    return u;
                                                                                }))));
    }

    public void deleteUserLocations(String userId){
        constructQuery(db.collection("users/" + userId + "/locations")
                .get()
                .addOnSuccessListener(q->q.getDocuments().stream()
                                                            .forEach(d->d.getReference().delete())));
    }

    public void getLocationsIll(List<User> bounds, Consumer<Pair<Diagnosis,Stream<GPSLocation>>> callback){
            bounds.stream().forEach(u->u.getIllnesses().stream().forEach(ill-> {
                //Start searching 2 weeks before the symptoms started occurring
                Timestamp start = new Timestamp(new Date(ill.getStartTime().toDate().getTime() - 14 * 24 * 60 * 60 * 1000));
                long end = ill.getEndTime() == null ? Calendar.getInstance().getTimeInMillis() : ill.getEndTime().toDate().getTime();
                db.collection("users/" + u.getId() + "/locations")
                        .whereGreaterThan("endTime", start)
                        .get()
                        .addOnSuccessListener(q -> callback.accept(new Pair(ill.getDiagnosis(), q.getDocuments().stream()
                                .map(d -> d.toObject(GPSLocation.class))
                                .filter(d -> d.getStartTime().toDate().getTime() < end))));
            }));
    }

    public void getLocationsIll(Consumer<Pair<Diagnosis,Stream<GPSLocation>>> callback){
              getIllUsers(s->{
                  s.forEach(u->u.getIllnesses().stream().forEach(ill-> {
                //Start searching 2 weeks before the symptoms started occurring
                Timestamp start = new Timestamp(new Date(ill.getStartTime().toDate().getTime() - 14 * 24 * 60 * 60 * 1000));
                long end = ill.getEndTime() == null ? Calendar.getInstance().getTimeInMillis() : ill.getEndTime().toDate().getTime();
                constructQuery(db.collection("users/" + u.getId() + "/locations")
                        .whereGreaterThan("endTime", start)
                        .get()
                        .addOnSuccessListener(q -> callback.accept(new Pair(ill.getDiagnosis(), q.getDocuments().stream()
                                .map(d -> d.toObject(GPSLocation.class))
                                .filter(d -> d.getStartTime().toDate().getTime() < end)))));
            }));
        });
    }


    public void getLocationsUser(String id, Consumer<List<GPSLocation>> callback){
        constructQuery(db.collection("users/" + id + "/locations")
                .get()
                .addOnSuccessListener(q->callback.accept(q.getDocuments().stream()
                                                         .map(d->d.toObject(GPSLocation.class))
                                                         .collect(Collectors.toList()))));
    }

    public void getRecentLocationsIll(Consumer<Pair<Diagnosis,GPSLocation>> callback){
        getCurrentlyIllUsers(s->{
            s.forEach(u->{
                constructQuery(db.collection("users/" + u.getId() + "/locations")
                        .orderBy("startTime", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .addOnSuccessListener(q->q.getDocuments().stream()
                                .forEach(d->callback.accept(new Pair(u.getIllnesses().stream()
                                                                                     .filter(i->i.getEndTime() == null)
                                                                                     .map(i->i.getDiagnosis())
                                                                                     .findFirst()
                                                                                     .get(), d.toObject(GPSLocation.class))))));
            });
        });
    }

    private void getCurrentlyIllUsers(Consumer<Stream<User>> callback){
        getIllUsers(s->{
           callback.accept(s.filter(u->u.getIllnesses().stream()
                                       .anyMatch(i->i.getEndTime() == null)));
        });
    }

    private void getIllUsers(Consumer<Stream<User>> callback){
        getAllUsers(s-> {
            callback.accept(s.filter(u->u.getIllnesses() != null)
                    .peek(u -> u.setIllnesses(u.getIllnesses().stream()
                                                                       .filter(i -> i.getDiagnosis().compareTo(Diagnosis.NEGATIVE) > 0)
                                                                       .collect(Collectors.toList())))
                             .filter(u -> u.getIllnesses().size() > 0));
        });
    }

    public void submitCough(byte[] data){
        Blob blob = Blob.fromBytes(data);
        Map<String,Blob> obj = new HashMap<>();
        obj.put("data",blob);
        constructQuery(db.collection("users/" + App.getUser().getId() + "/coughs").add(obj));
    }

    /*

    public void getSymptoms(String userId, Consumer<List<Symptom>> callback){
        constructQuery(db.document("users/"+userId)
                .get()
                .addOnSuccessListener(d->callback.accept(d.toObject(User.class).getSymptoms())));
    }

    public void setSymptoms(String userId, List<Symptom> symptoms, InfectionState state){
        App.getUser().setSymptoms(symptoms);
        App.getUser().setState(state);
        constructQuery(db.document("users/"+userId)
                .update("symptoms",symptoms,"state",state));
    }
*/

  /*  private void processLocation(QuerySnapshot q, GPSLocation newLocation){
        if (q.size() > 0){
            DocumentSnapshot doc = q.getDocuments().get(0);
            GPSLocation lastLocation = doc.toObject(GPSLocation.class);
            new Handler(Looper.getMainLooper()).post(() -> System.out.println("Geopoint: startTime: " + lastLocation.getStartTime() + ", speed: " + lastLocation.getSpeed() + ", location: " + lastLocation.getLocation()));
            GeoPoint lastCoords = lastLocation.getLocation();
            GeoPoint newCoords = newLocation.getLocation();
            if (lastLocation.getEndTime() == null) {
                if (Math.abs(lastCoords.getLatitude() - newCoords.getLatitude()) > 0.00001
                        || Math.abs(lastCoords.getLongitude() - newCoords.getLongitude()) > 0.00001
                        || Math.abs(lastLocation.getAltitude() - newLocation.getAltitude()) > 1) {
                    lastLocation.setEndTime(newLocation.getStartTime());
                    constructQuery(db.collection("users/"+App.getUser().getId()+"/locations")
                            .document(doc.getId())
                            .set(lastLocation));
                    addNewLocation(newLocation);
                    new Handler(Looper.getMainLooper()).post(()->System.out.println("In a new location!"));
                }
                else{
                    new Handler(Looper.getMainLooper()).post(()->System.out.println("Still in the same location!"));
                }
            }
            else addNewLocation(newLocation);
        }
        else {
            new Handler(Looper.getMainLooper()).post(()->System.out.println("No record found!"));
            addNewLocation(newLocation);
        }
    }

    private void addNewLocation(GPSLocation loc) {
        if (loc.getSpeed() >= 1){
            long endMilis = loc.getStartTime().toDate().getTime() + 10000;
            Timestamp endTime = new Timestamp(new Date(endMilis));
            loc.setEndTime(endTime);
        }
        constructQuery(db.collection("users/"+App.getUser().getId()+"/locations").add(loc));
    }*/

    public void addTransit(Transit transit){
        String userId = App.getUser().getId();
         constructQuery(db.collection("users/" + userId + "/transits")
                 .add(transit));
    }
}
