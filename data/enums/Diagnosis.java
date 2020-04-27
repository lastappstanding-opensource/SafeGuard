package com.americanaeuroparobotics.safeguard.data.enums;

public enum Diagnosis {
    NEGATIVE("I Tested Negative", Exposure.NONE),
    UNKNOWN("I Did Not Get Tested Yet          ▼", Exposure.POSSIBLE),
    POSITIVE("I Tested Positive", Exposure.CERTAIN);

    private String description;
    private Exposure exposure;

    Diagnosis(String description, Exposure exposure){
        this.description = description;
        this.exposure = exposure;
    }

    public String getDescription(){
        return description;
    }

    public Exposure getExposure(){
        return exposure;
    }

    @Override
    public String toString(){
        return description;
    }
}
