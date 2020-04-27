package com.americanaeuroparobotics.safeguard.data;

import com.americanaeuroparobotics.safeguard.data.location.Location;

public class Stats {

    private int cases;
    private int deaths;
    private int cured;
    private final Location location;
    private String updated;


    public Stats(int cases, int deaths, int cured, Location loc, String updated) {
        this.cases = cases;
        this.deaths = deaths;
        this.cured = cured;
        this.location = loc;
        this.updated = updated;
    }

    public int getCases() {
        return cases;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getCured() {
        return cured;
    }

    public String getUpdated() {
        return updated;
    }

    public void setCases(int cases) {
        this.cases = cases;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public void setCured(int cured) {
        this.cured = cured;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public Location getLocation() {
        return location;
    }
}
