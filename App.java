package com.americanaeuroparobotics.safeguard;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.view.ViewGroup.LayoutParams;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.americanaeuroparobotics.safeguard.R;
import com.americanaeuroparobotics.safeguard.data.User;

import java.util.concurrent.atomic.AtomicBoolean;

public class App extends Application
    implements Application.ActivityLifecycleCallbacks {

    private static Context mContext;
    private static User user;
    public static final String CHANNEL_ID = "NOTIFICATION_CHANNEL_ID";
    public static final String NAME = "SafeGuard";
    private static final AtomicBoolean foreground = new AtomicBoolean(true);


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        createNotificationChannel();
        this.registerActivityLifecycleCallbacks(this);
    }

    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Background Services",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public static PopupWindow loadSplash(){

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);

        // Inflate the custom layout/view
        View splashView = inflater.inflate(R.layout.splash,null);


        PopupWindow loadScreen = new PopupWindow(
                splashView,
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );
        return loadScreen;
    }



    public static Context getContext(){
        return mContext;
    }

    public static User getUser(){
        return user;
    }
    public static void setUser(User u){
        user = u;
    }
    public static boolean isForeground(){
        return foreground.get();
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        this.foreground.set(true);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        this.foreground.set(false);
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    public static void print(String text){
        //new Handler(Looper.getMainLooper()).post(()->System.out.println(text));
        Log.d("PRINT",text);
    }
}
