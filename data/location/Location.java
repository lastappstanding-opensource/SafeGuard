package com.americanaeuroparobotics.safeguard.data.location;

import com.google.android.gms.maps.model.LatLng;

public abstract class Location {

    protected LatLng coordinates;

    abstract String decodeLocation();

    abstract public LatLng getCoordinates();

}
