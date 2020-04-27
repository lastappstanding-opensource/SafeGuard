package com.americanaeuroparobotics.safeguard.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.view.View;
import android.util.Log;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.americanaeuroparobotics.safeguard.R;
import com.americanaeuroparobotics.safeguard.data.enums.RecordingState;
import com.americanaeuroparobotics.safeguard.services.DataService;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

public class CoughActivity extends AppCompatActivity {
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;

    private Button mRecordButton = null;
    private MediaRecorder mRecorder = null;

    private Button   mPlayButton = null;
    private MediaPlayer mPlayer = null;

    private Button buttonSubmit;
    private ProgressBar progressBar;
    private TextView textViewState;

    private RecordingState state;

    private DataService db;

    private void startPlaying() {
        state = RecordingState.PLAYING;
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            int duration = mPlayer.getDuration();
            progressBar.setMax(duration);
            new Thread(()->{
                Handler mainHandler = new Handler(Looper.getMainLooper());
                while (state == RecordingState.PLAYING) {
                    mainHandler.post(() -> {
                        if (mPlayer != null)
                            progressBar.setProgress(mPlayer.getCurrentPosition());
                    });
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            mPlayer.setOnCompletionListener(mp->{
                    state = RecordingState.RECORDED;
                    mPlayButton.setText("Play");
                    new Handler(Looper.getMainLooper()).post(()->progressBar.setProgress(0));
            });
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        state = RecordingState.RECORDED;
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        state = RecordingState.RECORDING;
        progressBar.setVisibility(View.INVISIBLE);
        mPlayButton.setVisibility(View.INVISIBLE);
        textViewState.setText("Recording your cough");
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        state = RecordingState.RECORDED;
        textViewState.setText("Your recorded cough:");
        progressBar.setVisibility(View.VISIBLE);
        mPlayButton.setVisibility(View.VISIBLE);
        buttonSubmit.setVisibility(View.VISIBLE);
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    private void onClickRecord(View v) {
        if (state == RecordingState.IDLE || state == RecordingState.RECORDED){
            startRecording();
            mRecordButton.setText("Stop");
        }
        else if (state == RecordingState.RECORDING){
            stopRecording();
            mRecordButton.setText("Record");
        }
    }

    private void onClickPlay(View v) {
        if (state == RecordingState.RECORDED){
            startPlaying();
            mPlayButton.setText("Stop");
        }
        else if (state == RecordingState.PLAYING){
            stopPlaying();
            mPlayButton.setText("Play");
        }
    }

    @Override
    public void onCreate(Bundle icicle) {
        mFileName =  Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DCIM + File.separator + "BLAH";
        super.onCreate(icicle);
        setContentView(R.layout.activity_cough);
        this.mPlayButton = findViewById(R.id.buttonPlay);
        mPlayButton.setOnClickListener(this::onClickPlay);
        mPlayButton.setVisibility(View.GONE);
        this.mRecordButton = findViewById(R.id.buttonRecord);
        mRecordButton.setOnClickListener(this::onClickRecord);
        this.buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonSubmit.setOnClickListener(this::submit);
        this.progressBar = findViewById(R.id.progressBar);
        this.textViewState = findViewById(R.id.textState);

        this.db = new DataService();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        reset();
    }

    @Override
    public void onStart(){
        super.onStart();
        String[] requiredPermissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        Optional<String> maybePermission = Arrays.stream(requiredPermissions)
                .filter(p-> ContextCompat.checkSelfPermission(this,p) != PackageManager.PERMISSION_GRANTED)
                .findFirst();
        if (maybePermission.isPresent()) ActivityCompat.requestPermissions(this, requiredPermissions,53);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void submit(View v){
        if (state == RecordingState.RECORDED) {
            try {
                byte[] data = Files.readAllBytes(Paths.get(mFileName));
                db.submitCough(data);
                Toast.makeText(this, "Thank you for your input!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
            reset();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 53: {
                boolean ok = true;
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == permissions.length){
                    for (int i = 0; i < grantResults.length; i++){
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            startActivity(new Intent(CoughActivity.this, MainActivity.class));
                        }
                    }
                }
                else startActivity(new Intent(CoughActivity.this, MainActivity.class));
                break;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void reset(){
        state = RecordingState.IDLE;
        textViewState.setText("At this time, we're collecting voice samples to train an AI to possibly diagnose by coughs. We correlate with your symptoms.");
        mPlayButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.INVISIBLE);
        buttonSubmit.setVisibility(View.GONE);
    }
}
