package com.americanaeuroparobotics.safeguard.data.enums;

public enum Symptom {
    COUGH("Dry Cough"),
    FEVER("Fever"),
    SHORT_BREATH("Shortness of Breath"),
    FATIGUE("Fatigue"),
    NO_TASTE("Loss of Taste & Smell"),
    CHEST_PAIN("Chest Pain"),
    GASTRO("Gastrointestinal Issues"),
    CONFUSION("Confusion or No Arousal"),
    BLUE_FACE("Bluish Lips or Face");

    private String description;

    Symptom(String description){
        this.description = description;
    }

    public String getDescription(){
        return this.description;
    }
}
