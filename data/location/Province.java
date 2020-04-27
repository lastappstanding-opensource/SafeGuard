package com.americanaeuroparobotics.safeguard.data.location;

import android.location.Address;
import android.location.Geocoder;

import com.americanaeuroparobotics.safeguard.App;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

public class Province extends Country {

    protected String province;
    protected static Geocoder addressDecoder;

    public Province(String country, String province) {
        super(country);
        if (addressDecoder == null) addressDecoder = new Geocoder(App.getContext());
        this.province = province;
        this.coordinates = null;
    }

    public String getProvince() {
        return province;
    }

    @Override
    public LatLng getCoordinates() {
        if (this.coordinates == null) initCoords(this.province + ", " + this.country);
        return this.coordinates;
    }

    protected void initCoords(String addr){
        try {
            Address address = addressDecoder.getFromLocationName(addr, 1).get(0);
            this.coordinates = new LatLng(address.getLatitude(), address.getLongitude());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } catch (IndexOutOfBoundsException e){}
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof Province) return super.equals(o) && (((Province) o).getProvince().equals(province));
        return false;
    }

    @Override
    public String toString(){
        return province;
    }
}
