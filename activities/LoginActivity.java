package com.americanaeuroparobotics.safeguard.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import com.americanaeuroparobotics.safeguard.App;
import com.americanaeuroparobotics.safeguard.R;
import com.americanaeuroparobotics.safeguard.data.User;
import com.americanaeuroparobotics.safeguard.services.DataService;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.json.JSONException;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private Button buttonAnonymous;
    private DataService db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        callbackManager = CallbackManager.Factory.create();

        this.db = new DataService();
        this.buttonAnonymous = findViewById(R.id.buttonAnonymous);
        buttonAnonymous.setOnClickListener(this::registerAnonymous);



        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile","email", "user_friends", "user_location", "user_photos"));


        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        PopupWindow splash = App.loadSplash();
                        findViewById(R.id.layout).post(()->splash.showAtLocation(findViewById(R.id.layout), Gravity.CENTER,0,0));
                        String id = loginResult.getAccessToken().getUserId();
                        db.getUserInfo(id, d->{
                            User user = d.toObject(User.class);
                            if (user == null){
                                GraphRequest fbRequest = GraphRequest.newMeRequest(
                                        AccessToken.getCurrentAccessToken(),
                                        (o,r)-> {
                                            try {
                                                User u = new User(o.getString("first_name"), o.getString("last_name"));
                                                u.setId(id);
                                                App.setUser(u);
                                                db.registerUser(u, rr -> {
                                                });
                                                writePreferences(id);
                                                splash.dismiss();
                                                startActivity(new Intent(LoginActivity.this, SetupActivity.class));
                                            } catch (JSONException e) {
                                                new Handler(Looper.getMainLooper()).post(()->e.printStackTrace());
                                            }
                                        });
                                Bundle params = new Bundle();
                                params.putString("fields", "first_name,last_name");
                                fbRequest.setParameters(params);
                                fbRequest.executeAsync();
                            }
                            else{
                                user.setId(id);
                                App.setUser(user);
                                writePreferences(id);
                                splash.dismiss();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            }
                        });
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        System.out.println(exception.getMessage());
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        Intent homepage = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(homepage);
    }


    private void writePreferences(String id){
        getSharedPreferences("meta",MODE_PRIVATE)
                .edit()
                .putString("userId", id)
                .commit();
        new Handler(this.getMainLooper()).post(() ->System.out.println(getSharedPreferences("meta",MODE_PRIVATE).getString("userId","No key found")));
    }

    private void registerAnonymous(View v){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_login);
        dialog.findViewById(R.id.textTitle).setFocusable(false);
        dialog.findViewById(R.id.buttonSubmit).setOnClickListener(vv->registerAnonymously(vv,dialog));
        TextView viewFirstName = dialog.findViewById(R.id.textFirstName);
        TextView viewLastName = dialog.findViewById(R.id.textLastName);
        ((CheckBox)dialog.findViewById(R.id.checkBoxAnonymous)).setOnCheckedChangeListener((bv,b)->{
            if (b){
                viewFirstName.setVisibility(View.INVISIBLE);
                viewLastName.setVisibility(View.INVISIBLE);
            }
            else {
                viewFirstName.setVisibility(View.VISIBLE);
                viewLastName.setVisibility(View.VISIBLE);
            }
        });
        dialog.show();
    }

    private void registerAnonymously(View v, Dialog d){
        PopupWindow splash = App.loadSplash();
        findViewById(R.id.layout).post(()->splash.showAtLocation(findViewById(R.id.layout), Gravity.CENTER,0,0));
        User user = new User();
        App.setUser(user);
        if (!((CheckBox)d.findViewById(R.id.checkBoxAnonymous)).isChecked()){
            user.setFirstName(((EditText)d.findViewById(R.id.textFirstName)).getText().toString());
            user.setLastName(((EditText)d.findViewById(R.id.textLastName)).getText().toString());
        }
        db.registerUser(user, r->{
            String id = r.getId();
            App.getUser().setId(id);
            writePreferences(id);
        });
        splash.dismiss();
        d.dismiss();
        startActivity(new Intent(LoginActivity.this, SetupActivity.class));
    }
}
