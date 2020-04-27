package com.americanaeuroparobotics.safeguard.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.americanaeuroparobotics.safeguard.App;
import com.americanaeuroparobotics.safeguard.R;
import com.americanaeuroparobotics.safeguard.services.DataService;
import com.americanaeuroparobotics.safeguard.views.StaticViewPager;
import com.google.android.material.tabs.TabLayout;

import java.util.Arrays;
import java.util.Optional;

public class SetupActivity extends AppCompatActivity {

    private StaticViewPager pager;
    private TabLayout selectedPageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        this.pager = findViewById(R.id.pager);
        pager.setAdapter(new PagerAdapter(getSupportFragmentManager(), pager, this));
        this.selectedPageView = findViewById(R.id.tabDots);
        selectedPageView.setupWithViewPager(pager, true);

        LinearLayout tabStrip = ((LinearLayout)selectedPageView.getChildAt(0));
        for(int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }

    }


    public static class SetupStartFragment extends Fragment {

        private ViewPager parent;

        public SetupStartFragment(ViewPager parent){
            this.parent = parent;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_setup_start, container, false);
            Button continueButton = rootView.findViewById(R.id.buttonTerms);
            continueButton.setOnClickListener(v->parent.setCurrentItem(1));
            return rootView;
        }
    }

    public static class SetupTermsFragment extends Fragment {

        private ViewPager parent;
        private  DataService db;

        public SetupTermsFragment(ViewPager parent){
            this.parent = parent;
            this.db = new DataService();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_setup_terms, container, false);
            Button agreeButton = rootView.findViewById(R.id.buttonAgree);
            agreeButton.setOnClickListener(v->{
                parent.setCurrentItem(2);
                App.getUser().setAgreedToTerms(true);
                db.saveCurrentUser();
            });
            Button disAgreeButton = rootView.findViewById(R.id.buttonDisagree);
            disAgreeButton.setOnClickListener(v->System.exit(0));
            return rootView;
        }
    }

    public static class SetupTrackingFragment extends Fragment {
        private ViewPager pager;
        private  DataService db;
        private Activity parent;

        public SetupTrackingFragment(ViewPager pager, Activity parent){
            this.pager = pager;
            this.db = new DataService();
            this.parent = parent;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_setup_tracking, container, false);
            Button agreeButton = rootView.findViewById(R.id.buttonAgree);
            agreeButton.setOnClickListener(v->{
                String[] requiredPermissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION};
                Optional<String> maybePermission = Arrays.stream(requiredPermissions)
                        .filter(p->ContextCompat.checkSelfPermission(parent,p) != PackageManager.PERMISSION_GRANTED)
                        .findFirst();
                if (maybePermission.isPresent()) ActivityCompat.requestPermissions(parent, requiredPermissions,52);
                App.getUser().setAllowsLocationTracking(true);
                db.saveCurrentUser();
                pager.setCurrentItem(3);
            });
            Button disAgreeButton = rootView.findViewById(R.id.buttonDisagree);
            disAgreeButton.setOnClickListener(v->pager.setCurrentItem(3));
            return rootView;
        }


        public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
            switch (requestCode) {
                case 52: {
                    boolean ok = true;
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length == permissions.length){
                        for (int i = 0; i < grantResults.length; i++){
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                                if (permissions[i].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)){
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                                        ok = false;
                                    }
                                }
                                else{
                                    ok = false;
                                }
                            }
                        }
                    }
                    else ok = false;
                    App.getUser().setAllowsLocationTracking(ok);
                    db.saveCurrentUser();
                    break;
                }
                // other 'case' lines to check for other
                // permissions this app might request
            }
        }
    }

    public static class SetupDoneFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_setup_done, container, false);
            Button startButton = rootView.findViewById(R.id.buttonStart);
            startButton.setOnClickListener(v->startActivity(new Intent(getContext(),MainActivity.class)));
            return rootView;
        }
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {

        private ViewPager pager;
        private Activity parent;

        public PagerAdapter(@NonNull FragmentManager fm, ViewPager pager, Activity parent) {
            super(fm);
            this.pager = pager;
            this.parent = parent;
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0: return new SetupStartFragment(pager);
                case 1: return new SetupTermsFragment(pager);
                case 2: return new SetupTrackingFragment(pager, parent);
                case 3: return new SetupDoneFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 4;
        }
    }
}


