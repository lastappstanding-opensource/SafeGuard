package com.americanaeuroparobotics.safeguard.data.location;

import android.content.res.XmlResourceParser;

import com.americanaeuroparobotics.safeguard.App;
import com.americanaeuroparobotics.safeguard.R;
import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;

import java.util.HashMap;
import java.util.Map;

public class Country extends Location {
    protected String country;
    private static final Map<String,LatLng> countryCoords = loadCountryCoords();


    public Country(String country) {
        this.country = encodeLocation(country);
    }

    private static Map<String,LatLng> loadCountryCoords(){
        Map<String,LatLng> ret = new HashMap<>();

        Map<String,String> coords = getHashMapResource(App.getContext().getResources().getXml(R.xml.coordinates));
        for (Map.Entry<String,String> e : coords.entrySet()) {
            String[] latLngStr = ((String) e.getValue()).split(",");
            ret.put(e.getKey(), new LatLng(Double.parseDouble(latLngStr[0]), Double.parseDouble(latLngStr[1])));
        }
        return ret;
    }

    private static Map<String,String> getHashMapResource(XmlResourceParser parser) {
        Map<String,String> map = null;
        String key = null, value = null;

        try {
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (parser.getName().equals("map")) map = new HashMap();
                    else if (parser.getName().equals("entry")) {
                        key = parser.getAttributeValue(null, "key");
                        if (null == key) {
                            parser.close();
                            return null;
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (parser.getName().equals("entry")) {
                        map.put(key, value);
                        key = null;
                        value = null;
                    }
                } else if (eventType == XmlPullParser.TEXT) {
                    if (null != key) {
                        value = parser.getText();
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return map;
    }

    public String getCountry() {
        return country;
    }

    public static String decodeLocation(String s){
        String ret;
        switch (s){
            case "United States": ret = "US"; break;
            default: ret = s;
        }
        return ret;
    }

    public String decodeLocation(){
        return Country.decodeLocation(country);
    }


    private String encodeLocation(String s){
        String ret;
        switch (s){
            case "US": ret = "United States"; break;
            default: ret = s;
        }
        return ret;
    }

    @Override
    public LatLng getCoordinates() {
        if (coordinates == null) {
            this.coordinates = countryCoords.get(country);
        }
        return this.coordinates;
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof Country){
            String other =  ((Country) o).getCountry();
            return other.equals(country);
        }
        return false;
    }

    @Override
    public String toString(){
        return country;
    }
}
