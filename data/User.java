package com.americanaeuroparobotics.safeguard.data;

import com.americanaeuroparobotics.safeguard.data.enums.Diagnosis;
import com.americanaeuroparobotics.safeguard.data.enums.Subscription;
import com.google.firebase.database.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.List;

@IgnoreExtraProperties
public class User {
    @Exclude private String id;
    private String firstName;
    private String lastName;
    private List<Illness> illnesses;
    private boolean agreedToTerms;
    private boolean allowsLocationTracking;
    private boolean allowsNotifications;
    private Subscription subscription;


    public User(){
        this.subscription = Subscription.FREE;
        this.allowsNotifications = true;
    }

    public User(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.subscription = Subscription.FREE;
        this.allowsNotifications = true;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }


    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<Illness> getIllnesses() {
        return illnesses;
    }

    public boolean isAgreedToTerms() {
        return agreedToTerms;
    }

    public void setAgreedToTerms(boolean agreedToTerms) {
        this.agreedToTerms = agreedToTerms;
    }

    public boolean isAllowsLocationTracking() {
        return allowsLocationTracking;
    }

    public void setAllowsLocationTracking(boolean allowsLocationTracking) {
        this.allowsLocationTracking = allowsLocationTracking;
    }

    public boolean isAllowsNotifications() {
        return allowsNotifications;
    }

    public void setAllowsNotifications(boolean allowsNotifications) {
        this.allowsNotifications = allowsNotifications;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public void setIllnesses(List<Illness> illnesses) {
        this.illnesses = illnesses;
    }
    @Exclude
    public Diagnosis findCurrentDiagnosis(){
        if (illnesses == null) return Diagnosis.NEGATIVE;
        for (Illness i : illnesses){
            if (i.getEndTime() == null){
                return i.getDiagnosis();
            }
        }
        return Diagnosis.NEGATIVE;
    }
    @Exclude
    public Illness findCurrentillness(){
        if (illnesses == null) return null;
        for (Illness ill : illnesses){
            if (ill.getEndTime() == null) return ill;
        }
        return null;
    }
}
