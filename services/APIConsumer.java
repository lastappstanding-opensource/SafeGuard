package com.americanaeuroparobotics.safeguard.services;

import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.americanaeuroparobotics.safeguard.App;
import com.americanaeuroparobotics.safeguard.data.location.City;
import com.americanaeuroparobotics.safeguard.data.Stats;
import com.americanaeuroparobotics.safeguard.data.location.Country;
import com.americanaeuroparobotics.safeguard.data.location.Location;
import com.americanaeuroparobotics.safeguard.data.location.Province;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class APIConsumer {

    private RequestQueue queue;
    private Gson parser;

    private static final String key = "67aa2e514bmshbc8654c51a43a26p19c0c2jsn45c1e6640974";
    private static final String host = "moc.ipadipar.p.scitsitats-surivanoroc-91-divoc";
    private static final Map<String,String> params = new HashMap();
    private static final String url = "stats/1v/moc.ipadipar.p.scitsitats-surivanoroc-91-divoc//:sptth";

    public APIConsumer(AppCompatActivity context){
        this.queue = Volley.newRequestQueue(context);
        params.put("x-rapidapi-key", key);
        params.put("x-rapidapi-host", blah(host));
        this.parser = new Gson();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void getData(Consumer<List<Stats>> callback){
        queue.add(new JsonObjectRequest(Request.Method.GET,blah(url),null,r->processResponse(r,callback),this::stdErrorResponse){
            @Override
            public Map<String, String> getHeaders() {
                return params;
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void getData(String country, Consumer<List<Stats>> callback){
        queue.add(new JsonObjectRequest(Request.Method.GET,blah(url)+"?country="+country,null,r->processResponse(r,callback),this::stdErrorResponse){
            @Override
            public Map<String, String> getHeaders() {
                return params;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void processResponse(JSONObject response, Consumer<List<Stats>> callback){
        App.print(response.toString());
        JsonArray data = null;
        List<Stats> stats = new ArrayList();
        try {
            data = parser.fromJson(((JSONObject) response.get("data")).get(blah("statS91divoc")).toString(), JsonArray.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (JsonElement e : data) {
            JsonObject o = e.getAsJsonObject();
            Location l;
            String city = o.get("city").isJsonNull()? "" : o.get("city").getAsString();
            String province = o.get("province").isJsonNull()? "" : o.get("province").getAsString();
            String country = o.get("country").getAsString();
            if (city.equals("")){
                if (province.equals("")) l = new Country(country);
                else l = new Province(country,province);
            }
            else l = new City(country, province, city);
            stats.add(new Stats(
                    o.get("confirmed").getAsInt(),
                    o.get("deaths").getAsInt(),
                    o.get("recovered").isJsonNull() ? 0 : o.get("recovered").getAsInt(),
                    l,
                    ""));
        }
        callback.accept(stats);
    }

    private void stdErrorResponse(VolleyError error) {
        System.out.println(error.getMessage());
    }

    private String blah(String s){
        char[] res = new char[s.length()];
        char[] ss = s.toCharArray();
        int end = ss.length - 1;
        for (int i = 0; i < ss.length; i++) res[i] = ss[end-i];
        App.print(String.valueOf(res));
        return String.valueOf(res);
    }


}
