package com.americanaeuroparobotics.safeguard.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.americanaeuroparobotics.safeguard.R;
import com.americanaeuroparobotics.safeguard.data.enums.TransitType;
import com.americanaeuroparobotics.safeguard.data.transit.Bus;
import com.americanaeuroparobotics.safeguard.data.transit.Car;
import com.americanaeuroparobotics.safeguard.data.transit.Flight;
import com.americanaeuroparobotics.safeguard.data.transit.Subway;
import com.americanaeuroparobotics.safeguard.data.transit.Train;
import com.americanaeuroparobotics.safeguard.data.transit.Transit;
import com.americanaeuroparobotics.safeguard.services.DataService;
import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TransitActivity extends AppCompatActivity {
    private Spinner spinnerType;
    private Button buttonDate;
    private TextView textViewDate;
    private ViewPager viewPagerDetails;

    private Button buttonSubmit;

    private DatePickerFragment datePicker;

    private DataService db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transit);

        this.spinnerType = findViewById(R.id.spinner_type);
        ArrayAdapter spinnerAdapter = new ArrayAdapter(this, R.layout.spinner_item_text, TransitType.values());
        spinnerType.setAdapter(spinnerAdapter);
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewPagerDetails.setCurrentItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        this.buttonDate = findViewById(R.id.buttonDate);
        this.textViewDate = findViewById(R.id.textViewDate);
        this.datePicker = new DatePickerFragment(textViewDate);
        buttonDate.setOnClickListener(v->datePicker.show(getSupportFragmentManager(),"datePicker"));
        this.buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonSubmit.setOnClickListener(this::submit);
        this.viewPagerDetails = findViewById(R.id.content);
        viewPagerDetails.setAdapter(new PagerAdapter(getSupportFragmentManager(),this));

        this.db = new DataService();
    }

    private void submit(View v){
        TransitType type = (TransitType) spinnerType.getSelectedItem();
        PagerAdapter adapter = (PagerAdapter) viewPagerDetails.getAdapter();
        Transit transit = null;
        Timestamp startTime, endTime;

        switch (type){
            case CAR:
                TransitCarFragment carFragment = adapter.getCarFragment();
                String numberPlate = carFragment.getNumberPlate();
                endTime = new Timestamp(new Date(datePicker.getDate() + carFragment.getEndTime()));
                startTime = new Timestamp(new Date(datePicker.getDate() + carFragment.getStartTime()));
                transit = new Car(numberPlate, startTime, endTime);
                break;
            case SUBWAY:
                TransitBusTrainSubwayFragment subwayFragment = adapter.getSubwayFragment();
                String provider = subwayFragment.getProvider();
                String line = subwayFragment.getLine();
                startTime = new Timestamp(new Date(datePicker.getDate() + subwayFragment.getStartTime()));
                endTime = new Timestamp(new Date(datePicker.getDate() + subwayFragment.getEndTime()));
                transit = new Subway(provider, line, startTime, endTime);
                break;
            case TRAIN:
                TransitBusTrainSubwayFragment trainFragment = adapter.getTrainFragment();
                provider = trainFragment.getProvider();
                line = trainFragment.getLine();
                startTime = new Timestamp(new Date(datePicker.getDate() + trainFragment.getStartTime()));
                endTime = new Timestamp(new Date(datePicker.getDate() + trainFragment.getEndTime()));
                transit = new Train(provider, line, startTime, endTime);
                break;
            case BUS:
                TransitBusTrainSubwayFragment busFragment = adapter.getBusFragment();
                provider = busFragment.getProvider();
                line = busFragment.getLine();
                startTime = new Timestamp(new Date(datePicker.getDate() + busFragment.getStartTime()));
                endTime = new Timestamp(new Date(datePicker.getDate() + busFragment.getEndTime()));
                transit = new Bus(provider, line, startTime, endTime);
                break;
            case FLIGHT:
                FlightFragment flightFragment = adapter.getFlightFragment();
                String flightNumber = flightFragment.getFlightNumber();
                String seat = flightFragment.getSeat();
                Timestamp time = new Timestamp(new Date(datePicker.getDate()));
                transit = new Flight(flightNumber,seat, time);
        }
        db.addTransit(transit);
        Toast.makeText(this, "Transit Info Added!", Toast.LENGTH_SHORT).show();
        viewPagerDetails.setAdapter(new PagerAdapter(getSupportFragmentManager(), this));
        viewPagerDetails.setCurrentItem(spinnerType.getSelectedItemPosition());
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        private TextView view;
        private long time;

        public TimePickerFragment(TextView view){
            this.view = view;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            this.view.setText(hourOfDay + ":" + minute);
            this.time = hourOfDay * 1000 * 60 * 60 + minute * 1000 * 60;
        }

        public long getTimeOfDay(){
            return this.time;
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private TextView view;
        private long date;

        public DatePickerFragment(TextView view){
            this.view = view;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of TimePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            this.view.setText(year + "/" + month + "/" + dayOfMonth);
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
            try {
                this.date = format.parse(this.view.getText().toString()).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        public long getDate(){
            return date;
        }
    }



    public static class TransitCarFragment extends Fragment {
        private Button buttonStartTime,
                       buttonEndTime;
        private TextView textViewStartTime,
                         textViewEndTime;
        private EditText editTextNumberPlate;

        private TransitActivity context;

        private TimePickerFragment startTimePicker,
                                   endTimePicker;

        public TransitCarFragment(TransitActivity context){
            this.context = context;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_transit_car, container, false);

            buttonStartTime = rootView.findViewById(R.id.buttonStartTime);
            buttonEndTime = rootView.findViewById(R.id.buttonEndTime);
            textViewStartTime = rootView.findViewById(R.id.textViewStartTime);
            textViewEndTime = rootView.findViewById(R.id.textViewEndTime);
            editTextNumberPlate = rootView.findViewById(R.id.editTextInput);

            this.startTimePicker = new TimePickerFragment(textViewStartTime);
            this.endTimePicker = new TimePickerFragment(textViewEndTime);


            buttonStartTime.setOnClickListener(v-> startTimePicker.show(context.getSupportFragmentManager(), "datePicker"));
            buttonEndTime.setOnClickListener(v-> endTimePicker.show(context.getSupportFragmentManager(), "datePicker"));

            return rootView;
        }

        public String getNumberPlate(){
            return editTextNumberPlate.getText().toString();
        }
        public long getStartTime(){
            return startTimePicker.getTimeOfDay();
        }
        public long getEndTime(){
            return endTimePicker.getTimeOfDay();
        }
    }
    public static class TransitBusTrainSubwayFragment extends Fragment {
        private Button buttonStartTime,
                buttonEndTime;
        private TextView textViewStartTime,
                textViewEndTime;

        private TransitActivity context;

        private TimePickerFragment startTimePicker,
                endTimePicker;

        private EditText editTextProvider;
        private EditText editTextLine;

        public TransitBusTrainSubwayFragment(TransitActivity context){
            this.context = context;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_transit_bus_train_subway, container, false);

            buttonStartTime = rootView.findViewById(R.id.buttonStartTime);
            buttonEndTime = rootView.findViewById(R.id.buttonEndTime);
            textViewStartTime = rootView.findViewById(R.id.textViewStartTime);
            textViewEndTime = rootView.findViewById(R.id.textViewEndTime);

            startTimePicker = new TimePickerFragment(textViewStartTime);
            endTimePicker = new TimePickerFragment(textViewEndTime);

            editTextProvider = rootView.findViewById(R.id.input_city);
            editTextLine = rootView.findViewById(R.id.input_line);

            buttonStartTime.setOnClickListener(v-> startTimePicker.show(context.getSupportFragmentManager(), "datePicker"));
            buttonEndTime.setOnClickListener(v-> endTimePicker.show(context.getSupportFragmentManager(), "datePicker"));

            return rootView;
        }

        public String getProvider(){
            return editTextProvider.getText().toString();
        }
        public String getLine(){
            return editTextLine.getText().toString();
        }
        public long getStartTime(){
            return startTimePicker.getTimeOfDay();
        }
        public long getEndTime(){
            return endTimePicker.getTimeOfDay();
        }
    }
    public static class FlightFragment extends Fragment {

        private EditText editTextFlightNumber,
                         editTextSeat;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_transit_flight, container, false);

            this.editTextFlightNumber = rootView.findViewById(R.id.input_flight);
            this.editTextSeat = rootView.findViewById(R.id.input_seat);

            return rootView;
        }

        public String getFlightNumber(){
            return editTextFlightNumber.getText().toString();
        }
        public String getSeat(){
            return editTextSeat.getText().toString();
        }
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {

        private TransitCarFragment carFragment;
        private TransitBusTrainSubwayFragment busFragment,
                                              trainFragment,
                                              subwayFragment;
        private FlightFragment flightFragment;


        public PagerAdapter(@NonNull FragmentManager fm, TransitActivity context) {
            super(fm);
            this.carFragment = new TransitCarFragment(context);
            this.busFragment = new TransitBusTrainSubwayFragment(context);
            this.trainFragment = new TransitBusTrainSubwayFragment(context);
            this.subwayFragment = new TransitBusTrainSubwayFragment(context);
            this.flightFragment = new FlightFragment();
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0: return carFragment;
                case 1: return busFragment;
                case 2: return trainFragment;
                case 3: return subwayFragment;
                case 4: return flightFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 5;
        }

        public TransitCarFragment getCarFragment() {
            return carFragment;
        }

        public TransitBusTrainSubwayFragment getBusFragment() {
            return busFragment;
        }

        public TransitBusTrainSubwayFragment getTrainFragment() {
            return trainFragment;
        }

        public TransitBusTrainSubwayFragment getSubwayFragment() {
            return subwayFragment;
        }

        public FlightFragment getFlightFragment() {
            return flightFragment;
        }
    }
}
