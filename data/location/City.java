package com.americanaeuroparobotics.safeguard.data.location;

import com.google.android.gms.maps.model.LatLng;

public class City extends Province {
    protected final String city;

    public City(String country, String province, String city) {
        super(country, province);
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    @Override
    public LatLng getCoordinates(){
        if (coordinates == null) initCoords(city + ", " + province + ", " + country);
        return this.coordinates;
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof City) return super.equals(o) && ((City) o).getCity().equals(city);
        return false;
    }

    @Override
    public String toString(){
        return city;
    }
}
