package com.americanaeuroparobotics.safeguard.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.americanaeuroparobotics.safeguard.R;

public class SettingsActivity extends AppCompatActivity {

    private Button buttonAccount;
    private Button buttonTerms;
    private Button buttonHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        this.buttonAccount = findViewById(R.id.buttonAccount);
        buttonAccount.setOnClickListener(v->startActivity(new Intent(SettingsActivity.this,AccountActivity.class)));
        this.buttonTerms = findViewById(R.id.buttonTerms);
        buttonTerms.setOnClickListener(v->loadPopup(R.string.terms));
        this.buttonHelp = findViewById(R.id.buttonHelp);
        buttonHelp.setOnClickListener(v->loadPopup(R.string.help));
    }

    private void loadPopup(int content){


        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        // Inflate the custom layout/view
        View popupView = inflater.inflate(R.layout.popup_text,null);

        Button backButton = popupView.findViewById(R.id.buttonBack);
        TextView contentView = popupView.findViewById(R.id.content);
        contentView.setText(content);



        PopupWindow popup = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        backButton.setOnClickListener(v->popup.dismiss());
        popup.showAtLocation(findViewById(R.id.layout), Gravity.CENTER,0,0);
    }


}
