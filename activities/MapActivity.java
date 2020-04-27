package com.americanaeuroparobotics.safeguard.activities;

import androidx.annotation.RequiresApi;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.americanaeuroparobotics.safeguard.data.enums.MapMode;
import com.americanaeuroparobotics.safeguard.R;
import com.americanaeuroparobotics.safeguard.data.Stats;
import com.americanaeuroparobotics.safeguard.data.location.Country;
import com.americanaeuroparobotics.safeguard.data.location.Province;
import com.americanaeuroparobotics.safeguard.data.location.Location;
import com.americanaeuroparobotics.safeguard.services.APIConsumer;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MapActivity extends AbstractMapActivity {

    private List<Stats> model;
    private Bitmap tagIcon;
    private MapMode mode;
    private String country;
    private double totalCases;
    private String province;
    private ProgressDialog loadDialog;
    private APIConsumer api;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.layout.activity_map);
        model = new ArrayList();
        this.tagIcon = ((BitmapDrawable) getResources().getDrawable(R.drawable.bio_hazard)).getBitmap();
        this.mode = MapMode.GLOBAL;
        this.api = new APIConsumer(this);
        loadData();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void arrangeStatsGlobal(List<Stats> stats) {
        model.addAll(this.mergeStats(stats));
        if (this.map != null) addMarkers();
        loadDialog.dismiss();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void arrangeStatsCountry(List<Stats> stats){
        if (stats.size() > 1){
            List<Stats> filtered = this.mergeStats(stats);
            clearMarker(new Country(((Country)filtered.get(0).getLocation()).getCountry()));
            model.addAll(filtered);
            addMarkersParallel(filtered);
        }
        else showDialog("No more detailed data available for "+this.country);
        loadDialog.dismiss();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addMarkersParallel(List<Stats> list){
        double max = totalCases;
        new Thread (()->list.stream()
                    .parallel()
                    .forEach(c->addMarker(c,calcR(max,c))))
                .start();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void arrangeStatsProvince(List<Stats> list, Address addr){
        try{
            Province province = findProvince(list,addr);
            loadDialog.setMessage("Loading data for " + province + "...");
            List<Stats> filtered = list.stream()
                    .filter(c->province.equals(c.getLocation()))
                    .collect(Collectors.toList());
            if (filtered.size() > 1){
                clearMarker(findProvince(model,addr));
                model.addAll(filtered);
                addMarkersParallel(filtered);
            }
            else showDialog("No more detailed data available for " + province.toString());
        } catch(IndexOutOfBoundsException e){
            showDialog("Not a valid location!");
            this.mode = MapMode.COUNTRY;
        }
        loadDialog.dismiss();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Province findProvince(List<Stats> list, Address addr){
        List<String> provinces = list.stream()
                .filter(c->c.getLocation() instanceof Province)
                .map(c->((Province)c.getLocation()).getProvince())
                .collect(Collectors.toList());
        String province = null;
        if (provinces.contains(addr.getAdminArea())) province = addr.getAdminArea();
        else if (provinces.contains(addr.getSubAdminArea())) province = addr.getSubAdminArea();
        else if (provinces.contains(addr.getLocality())) province = addr.getLocality();
        else if (provinces.contains(addr.getSubLocality())) province = addr.getSubLocality();
        if (province != null) return new Province(addr.getCountryName(), province);
        throw new IndexOutOfBoundsException();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private boolean compareProvince(Address addr){
            return ((addr.getAdminArea() != null && addr.getAdminArea().equals(province))
                    || (addr.getSubAdminArea() != null && addr.getSubAdminArea().equals(province))
                    || (addr.getLocality() != null && addr.getLocality().equals(province))
                    || (addr.getSubLocality() != null && addr.getSubLocality().equals(province)));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void loadData() {
        loadDialog = ProgressDialog.show(this, "", "Loading global data...", true);
        api.getData(this::arrangeStatsGlobal);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    void loadData(Country country){
        loadDialog = ProgressDialog.show(this, "", "Loading data for " + country.toString() + "...", true);
        api.getData(country.decodeLocation(),this::arrangeStatsCountry);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    void loadData(Address addr){
        loadDialog = ProgressDialog.show(this, "", "Loading data for " + province.toString() + "...", true);
        api.getData(new Country(addr.getCountryName()).decodeLocation(),(l->arrangeStatsProvince(l,addr)));
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void respondToLongClick(LatLng latLng) {
        this.map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        try {
            Address addr = addressDecoder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0);
            if (this.mode == MapMode.GLOBAL) {
                this.mode = MapMode.COUNTRY;
                String country = addr.getCountryName();
                this.country = country;
                Country c = new Country(country);
                loadData(c);
            } else if (this.mode == MapMode.COUNTRY) {
                if (addr.getCountryName().equals(this.country)){
                    this.mode = MapMode.PROVINCE;
                    this.province = findProvince(model, addr).getProvince();
                    loadData(addr);
                }
                else {
                    this.country = addr.getCountryName();
                    Country c = new Country(country);
                    loadData(c);
                }
            }
            else{
                if (compareProvince(addr)){
                    showDialog("No more valid data available for " + province);
                }
                else {
                    if (this.country.equals(addr.getCountryName())){
                        this.province = findProvince(model, addr).getProvince();
                        loadData(addr);
                    }
                    else{
                        this.mode = MapMode.COUNTRY;
                        this.country = addr.getCountryName();
                        Country c = new Country(country);
                        loadData(c);
                    }
                }
            }
        } catch (IOException e) {
        e.printStackTrace();
    }catch (IndexOutOfBoundsException e) {
            if (this.mode == MapMode.COUNTRY) this.mode = MapMode.GLOBAL;
            else if (this.mode == MapMode.PROVINCE) this.mode = MapMode.COUNTRY;
        showDialog("Not a valid location!");
    }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void clearMarker(Location c) {
        model.stream()
                .map(s->s.getLocation())
                .filter(l->c.equals(l))
                .map(l->l.getCoordinates())
                .forEach(cc-> markers.stream()
                                    .filter(m->m.getPosition().equals(cc))
                                    .findFirst()
                                    .ifPresent(m->m.setVisible(false)));
    }

    @Override
    protected void respondToClick(LatLng coords){
        showDialog("Hold a specific country or region to see its details.");
    }

    private void showDialog(String message){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.message_dialog);
        ((TextView)dialog.findViewById(R.id.textMessage)).setText(message);
        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void addMarkers(){
        if (model.size() > 0 ) {
            double max = getTotalCases(model);
            this.totalCases = max;
            new Thread(() -> model.stream()
                    .parallel()
                    .forEach(s -> addMarker(s, calcR(max, s)))).start();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void addMarker(Stats stats, int r){
        if (r > 5) {
            final int rr = (r < 20)? 20 : r;
            LatLng loc = stats.getLocation().getCoordinates();
            if (loc != null) {
                    new Handler(this.getMainLooper()).post(() ->
                            this.markers.add(map.addMarker(new MarkerOptions()
                                    .position(loc)
                                    .title(stats.getLocation().toString())
                                    .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(tagIcon, rr, rr, false)))
                                    .snippet("cases: " + stats.getCases()
                                            + "\nDeaths: " + stats.getDeaths()
                                            + "\nCured: " + stats.getCured()))));
                }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected double getTotalCases(List<Stats> stats){
        return (double) stats.stream()
                .mapToInt(c->c.getCases())
                .sum();
    }
    protected int calcR(double max, Stats stats){
        return (int) (Math.sqrt((1/max)*stats.getCases())*500.0);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private List<Stats> mergeStats(List<Stats> list){
        List<Stats> merged = new ArrayList<>();
        Stats buffer = null;
        while (list.size() > 0) {

            if (this.mode == MapMode.GLOBAL || list.get(0).getLocation() instanceof Province) {
                Location l;
                if (this.mode == MapMode.GLOBAL) l = new Country(((Country) list.get(0).getLocation()).getCountry());
                else l = new Province(((Country) list.get(0).getLocation()).getCountry(), ((Province) list.get(0).getLocation()).getProvince());

                int cases = 0, deaths = 0, cured = 0;
                int i = 0;
                while (i < list.size()) {
                    if (l.equals(list.get(i).getLocation())) {
                        cases += list.get(i).getCases();
                        deaths += list.get(i).getDeaths();
                        cured += list.get(i).getCured();
                        list.remove(i);
                    } else i++;
                }
                    merged.add(new Stats(cases, deaths, cured, l, ""));
            }
            else{
                buffer = list.get(0);
                list.remove(0);
            }
        }
        if (buffer != null){
            int sum = merged.stream()
                    .mapToInt(c->c.getCases())
                    .sum();
            if (sum < buffer.getCases()) merged.add(buffer);
        }
        return merged;
    }
}
