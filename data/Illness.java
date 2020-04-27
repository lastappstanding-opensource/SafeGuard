package com.americanaeuroparobotics.safeguard.data;

import com.americanaeuroparobotics.safeguard.data.enums.Symptom;
import com.americanaeuroparobotics.safeguard.data.enums.Diagnosis;
import com.google.firebase.Timestamp;

import java.util.List;

public class Illness {
    private Timestamp startTime;
    private Timestamp endTime;
    private List<Symptom> symptoms;
    private Diagnosis diagnosis;

    public Illness(){

    }

    public Illness(Timestamp startTime, Timestamp endTime, List<Symptom> symptoms, Diagnosis diagnosis) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
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

    public List<Symptom> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<Symptom> symptoms) {
        this.symptoms = symptoms;
    }

    public Diagnosis getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(Diagnosis diagnosis) {
        this.diagnosis = diagnosis;
    }
}
