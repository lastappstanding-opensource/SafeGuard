package com.americanaeuroparobotics.safeguard.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.americanaeuroparobotics.safeguard.App;
import com.americanaeuroparobotics.safeguard.R;
import com.americanaeuroparobotics.safeguard.data.Illness;
import com.americanaeuroparobotics.safeguard.data.User;
import com.americanaeuroparobotics.safeguard.data.enums.Symptom;
import com.americanaeuroparobotics.safeguard.data.enums.Diagnosis;
import com.americanaeuroparobotics.safeguard.services.DataService;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class LogActivity extends AppCompatActivity {

    private RecyclerView recyclerViewSymptoms;
    private Button buttonSubmit;
    private Spinner spinnerState;

    private DataService db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        User user = App.getUser();

        Illness currentIllness = user.findCurrentillness();
        Diagnosis currentDiagnosis = currentIllness == null? Diagnosis.NEGATIVE : currentIllness.getDiagnosis();
        List<Symptom> currentSymptoms = currentIllness == null? new ArrayList<>() : currentIllness.getSymptoms();


        this.recyclerViewSymptoms = findViewById(R.id.recyclerViewSymptoms);
        recyclerViewSymptoms.setAdapter(new SymptomsAdapter(currentSymptoms));
        recyclerViewSymptoms.setLayoutManager(new LinearLayoutManager(this));
        this.buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonSubmit.setOnClickListener(this::submit);
        this.spinnerState = findViewById(R.id.spinnerState);
        ArrayAdapter spinnerAdapter = new ArrayAdapter<Diagnosis>(this, R.layout.spinner_item_text,Diagnosis.values());
        spinnerState.setAdapter(spinnerAdapter);


        Diagnosis[] states = Diagnosis.values();
        int i = 0;
        while (states[i] != currentDiagnosis) i++;
        spinnerState.setSelection(i);

        this.db = new DataService();
    }

    @Override
    protected void onResume(){
        super.onResume();
        spinnerState.setSelection(1);

    }

    private void submit(View v){
        User user = App.getUser();
        List<Symptom> symptoms = ((SymptomsAdapter)recyclerViewSymptoms.getAdapter()).getSelectedSymptoms();
        Diagnosis diagnosis = (Diagnosis) spinnerState.getSelectedItem();
        Illness currentIllness = user.findCurrentillness();
        if (symptoms.size() == 0){
            if (currentIllness != null){
                currentIllness.setEndTime(new Timestamp(new Date(Calendar.getInstance().getTimeInMillis())));
                currentIllness.setDiagnosis(diagnosis);
            }
        }
        else{
            if (currentIllness != null) {
                currentIllness.setSymptoms(symptoms);
                currentIllness.setDiagnosis(diagnosis);
            }
            else {
                if (user.getIllnesses() == null) user.setIllnesses(new ArrayList<>());
                Illness newIllness = new Illness(new Timestamp(new Date(Calendar.getInstance().getTimeInMillis())),
                                                                null,
                                                                symptoms,
                                                                diagnosis);
                user.getIllnesses().add(newIllness);
            }
        }

        db.saveCurrentUser();

        //db.setSymptoms(App.getUser().getId(),symptoms, state);
        Toast toast = Toast.makeText(this, "Thank you for your input!", Toast.LENGTH_SHORT);
        toast.show();
        startActivity(new Intent(LogActivity.this,MainActivity.class));
    }

    private class SymptomsAdapter extends RecyclerView.Adapter<SymptomsAdapter.ViewHolder> {

        private final List<SymptomViewModel> model;

        public SymptomsAdapter(List<Symptom> symptomsPositive){
            super();
            model = Arrays.stream(Symptom.values())
                    .map(s->new SymptomViewModel(s,symptomsPositive.contains(s)))
                    .collect(Collectors.toList());
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            // Inflate the custom layout
            View symptomView = inflater.inflate(R.layout.list_item_symptom, parent, false);


            // Return a new holder instance
            ViewHolder viewHolder = new ViewHolder(symptomView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.getCheckBoxSymptom().setText(model.get(position).getSymptom().getDescription());
            holder.getCheckBoxSymptom().setChecked(model.get(position).isSelected());
            holder.getCheckBoxSymptom().setOnCheckedChangeListener((c,b)->model.get(position).setSelected(b));
        }

        public List<Symptom> getSelectedSymptoms(){
            return model.stream()
                    .filter(SymptomViewModel::isSelected)
                    .map(SymptomViewModel::getSymptom)
                    .collect(Collectors.toList());
        }

        @Override
        public int getItemCount() {
            return model.size();
        }

        private class SymptomViewModel{
            private final Symptom symptom;
            private boolean selected;

            public SymptomViewModel(Symptom symptom, boolean pos){
                this.symptom = symptom;
                this.selected = pos;
            }

            public Symptom getSymptom(){
                return symptom;
            }

            public boolean isSelected(){
                return selected;
            }
            public void setSelected(boolean selected){
                this.selected = selected;
            }
        }

        private class ViewHolder extends RecyclerView.ViewHolder{

            private CheckBox checkBoxSymptom;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                this.checkBoxSymptom = itemView.findViewById(R.id.checkBoxSymptom);
            }

            public CheckBox getCheckBoxSymptom() {
                return checkBoxSymptom;
            }
        }
    }
}
