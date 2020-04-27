package com.americanaeuroparobotics.safeguard.data.transit;

import com.americanaeuroparobotics.safeguard.data.enums.TransitType;
import com.google.firebase.Timestamp;

public class Car extends Transit {
    private String numberPlate;

    public Car(){

    }

    public Car(String numberPlate, Timestamp startTime, Timestamp endTime){
        super(startTime, endTime, TransitType.CAR);
        this.numberPlate = numberPlate;
    }

    public String getNumberPlate() {
        return numberPlate;
    }

    public void setNumberPlate(String numberPlate) {
        this.numberPlate = numberPlate;
    }

}
