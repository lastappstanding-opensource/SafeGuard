package com.americanaeuroparobotics.safeguard.views;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.americanaeuroparobotics.safeguard.data.Stats;

import java.util.ArrayList;

import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

public class StatsView {
    private final TextView textCases;
    private final TextView textDeaths;
    private final TextView textCured;
    private final PieChartView chart;

    private boolean valid;
    private Stats model;

    public StatsView(TextView textCases, TextView textDeaths, TextView textCured, PieChartView chart, Stats model, boolean valid) {
        this.textCases = textCases;
        this.textDeaths = textDeaths;
        this.textCured = textCured;
        this.chart = chart;
        this.model = model;
        this.valid = valid;
    }

    public TextView getTextCases() {
        return textCases;
    }

    public TextView getTextDeaths() {
        return textDeaths;
    }

    public TextView getTextCured() {
        return textCured;
    }

    public PieChartView getChart() {
        return chart;
    }

    public Stats getModel() {
        return model;
    }

    public void setModel(Stats model) {
        this.model = model;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
        update();
    }
    public void update(){
        if (valid){
            textCases.setText("Cases: " + model.getCases());
            textDeaths.setText("Deaths: " + model.getDeaths());
            textCured.setText("Recovered: " + model.getCured());
            chart.setVisibility(View.VISIBLE);

            ArrayList<SliceValue> data = new ArrayList<>();
            data.add(new SliceValue(model.getDeaths(), Color.RED).setLabel("Deaths"));
            data.add(new SliceValue(model.getCured(), Color.GREEN).setLabel("Cured"));
            data.add(new SliceValue(model.getCases() - model.getDeaths() - model.getCured(), Color.BLUE).setLabel("Active"));
            PieChartData pieChartData = new PieChartData(data);
            pieChartData.setHasLabels(true);
            chart.setPieChartData(pieChartData);
        }
        else{
            chart.setVisibility(View.INVISIBLE);
            textCases.setText("Loading Data...");
            textDeaths.setText("");
            textCured.setText("");
        }
    }
}
