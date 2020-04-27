package com.americanaeuroparobotics.safeguard.data.transit;

import com.americanaeuroparobotics.safeguard.data.enums.TransitType;
import com.google.firebase.Timestamp;

public abstract class Transit {
    private Timestamp startTime;
    private Timestamp endTime;
    private TransitType type;

    public Transit(){

    }

    public Transit(Timestamp startTime, Timestamp endTime, TransitType type) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public TransitType getType() {
        return type;
    }
}
