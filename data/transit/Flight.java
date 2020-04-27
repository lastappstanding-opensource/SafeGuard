package com.americanaeuroparobotics.safeguard.data.transit;

import com.americanaeuroparobotics.safeguard.data.enums.TransitType;
import com.google.firebase.Timestamp;

public class Flight extends Transit {
    private String flightNumber;
    private String seat;

    public Flight(){

    }

    public Flight(String flightNumber, String seat, Timestamp time) {
        super(time, time, TransitType.FLIGHT);
        this.flightNumber = flightNumber;
        this.seat = seat;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }
}
