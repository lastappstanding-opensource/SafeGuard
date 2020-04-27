package com.americanaeuroparobotics.safeguard.data.enums;

import android.graphics.Color;

public enum Exposure {
    UNKNOWN("NO KNOWN EXPOSURE", Color.parseColor("#148625")),
    NONE("NO KNOWN EXPOSURE", Color.parseColor("#148625")),
    POSSIBLE("EXPOSURE TO UNDIAGNOSED USERS WITH SYMPTOMS", Color.parseColor("#861414")),
    CERTAIN("EXPOSURE TO DIAGNOSED USERS", Color.parseColor("#861414"));

    private String description;
    private int color;

    Exposure(String description, int color){
        this.description = description;
        this.color = color;
    }

    public String getDescription(){
        return description;
    }

    @Override
    public String toString(){
        return description;
    }

    public int getColor(){
        return color;
    }
}
