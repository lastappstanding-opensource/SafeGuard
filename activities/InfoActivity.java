package com.americanaeuroparobotics.safeguard.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.americanaeuroparobotics.safeguard.R;
import com.americanaeuroparobotics.safeguard.data.Stats;
import com.americanaeuroparobotics.safeguard.data.location.Country;
import com.americanaeuroparobotics.safeguard.services.APIConsumer;
import com.americanaeuroparobotics.safeguard.views.StatsView;
import com.hbb20.CountryCodePicker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.widget.Button;

import java.util.List;

public class InfoActivity extends AppCompatActivity {

    private CountryCodePicker ccp;
    private APIConsumer api;

    private Stats global, country;
    private StatsView countryView, globalView;

    private Button buttonGlobalMap;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ccp = (CountryCodePicker) findViewById(R.id.country);
        ccp.setOnCountryChangeListener(this::updateCountry);

        this.countryView = new StatsView(findViewById(R.id.textCases), findViewById(R.id.textDeaths), findViewById(R.id.textCured), findViewById(R.id.chart), country, false);
        countryView.setValid(false);
        this.globalView = new StatsView(findViewById(R.id.globalCases), findViewById(R.id.globalDeaths), findViewById(R.id.globalCured), findViewById(R.id.chartGlobal), global, false);
        globalView.setValid(false);
        this.buttonGlobalMap = findViewById(R.id.buttonMapGlobal);
        buttonGlobalMap.setOnClickListener(e->startActivity(new Intent(this, MapActivity.class)));
        /*buttonCountryMap.setOnClickListener(e->{
            Intent intent = new Intent(this, CountryMapActivity.class);
            intent.putExtra("COUNTRY", ccp.getSelectedCountryName());
            startActivity(intent);
        });*/


        api = new APIConsumer(this);
        api.getData(l->updateStats(global,globalView,l));
        api.getData(Country.decodeLocation(ccp.getSelectedCountryName()), l -> updateStats(country,countryView, l));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateCountry(){
        countryView.setValid(false);
        api.getData(Country.decodeLocation(ccp.getSelectedCountryName()), l->updateStats(country,countryView, l));
    }

    private void updateStats(Stats model, StatsView view, List<Stats> stats){
        int cases = 0, deaths = 0, cured = 0;
        for (Stats s : stats){
            cases += s.getCases();
            deaths += s.getDeaths();
            cured += s.getCured();
         }
        model = new Stats(cases, deaths, cured, (Country)stats.get(0).getLocation(), "");
        view.setModel(model);
        view.setValid(true);
    }

}
