package com.americanaeuroparobotics.safeguard.services;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.americanaeuroparobotics.safeguard.App;
import com.americanaeuroparobotics.safeguard.data.POI;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MapAPIConsumer {
    private static final String KEY = "AIzaSyB_FnNztUvEOLQZW3XIvS9eMQV-6vK52ZE";
    private static final String TAG = "MAPAPI";
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/place/search/json";

    private RequestQueue queue;
    private Gson parser;

    public MapAPIConsumer(AppCompatActivity context){
        this.queue = Volley.newRequestQueue(context);
        this.parser = new Gson();
    }

    private void getData(String url, Consumer<List<POI>> callback){
        queue.add(new JsonObjectRequest(Request.Method.GET,url,null, r->processResponse(r,callback),this::stdErrorResponse));
    }

    private String constructURL(LatLng coords, String type, int radius){
        return BASE_URL + "?location=" + coords.latitude + "," + coords.longitude + "&radius="+radius+"&fields=formatted_address,name&key=" + KEY + "&types=" + type;
    }

    public void getStores(int radius, LatLng coords, Consumer<List<POI>> callback){
        getData(constructURL(coords, "grocery_or_supermarket",radius*1000), callback);
    }
    public void getHospitals(int radius, LatLng coords, Consumer<List<POI>> callback){
        getData(constructURL(coords, "hospital",radius*1000), callback);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void processResponse(JSONObject response, Consumer<List<POI>> callback) {

        App.print("Found response: " + response);


        JsonArray data = null;
        List<POI> stats = new ArrayList();
        try {
            data = parser.fromJson( response.get("results").toString(), JsonArray.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        App.print("Array : " + data);

        for (JsonElement e : data) {
            JsonObject o = e.getAsJsonObject();
            POI poi;

            String name = o.get("name").getAsString();
            JsonObject geometry = o.get("geometry").getAsJsonObject();
            JsonObject location = geometry.get("location").getAsJsonObject();
            double lat = location.get("lat").getAsDouble();
            double lng = location.get("lng").getAsDouble();

            poi = new POI(name, new LatLng(lat,lng), 0);
            stats.add(poi);
        }

        callback.accept(stats);

//  public POI(String name, LatLng coords, int waitTime

            /*
            Location l;
            String city = o.get("city").getAsString();
            String province = o.get("province").getAsString();
            String country = o.get("country").getAsString();
            if (city.equals("")){
                if (province.equals("")) l = new Country(country);
                else l = new Province(country,province);
            }
            else l = new City(country, province, city);
            stats.add(new Stats(
                    o.get("confirmed").getAsInt(),
                    o.get("deaths").getAsInt(),
                    o.get("recovered").getAsInt(),
                    l,
                    ""));
        }
        callback.accept(stats);

         */
    }

    private void stdErrorResponse(VolleyError error) {
        Log.e(TAG, error.getMessage());
    }


}
