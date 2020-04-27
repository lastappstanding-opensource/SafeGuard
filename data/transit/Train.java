package com.americanaeuroparobotics.safeguard.data.transit;

import com.americanaeuroparobotics.safeguard.data.enums.TransitType;
import com.google.firebase.Timestamp;

public class Train extends Transit {
    private String provider;
    private String line;

    public Train(){

    }

    public Train(String provider, String line, Timestamp startTime, Timestamp endTime) {
        super(startTime, endTime, TransitType.TRAIN);
        this.provider = provider;
        this.line = line;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }
}
