package com.example.eliam.challengingfruits.activity;

import android.Manifest;
import android.accounts.AccountAuthenticatorActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.eliam.challengingfruits.R;
import com.example.eliam.challengingfruits.UserInfo;
import com.example.eliam.challengingfruits.Utils;
import com.example.eliam.challengingfruits.accountmanager.ServerInterface;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;

public class SplashActivity extends AccountAuthenticatorActivity {

    private static final int REQUEST_PERMISSION_READ_SMS = 16;

    private CallbackManager mCallbackManager;

    ImageView icon;
    LoginButton loginButton;

    Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            ex.printStackTrace();
            int i=0;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(handler);
        Utils.initFacebook(getApplicationContext());
        setContentView(R.layout.activity_splash);

        icon = (ImageView) findViewById(R.id.app_logo);
        loginButton = (LoginButton) findViewById(R.id.facebook_login);

        mCallbackManager = CallbackManager.Factory.create();
        setFacebookLoginButton();

        // All devices previous API 23 has all the permissions granted at the moment
        // the app is installed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // Check whether the app has the permission to read the storage.
            if (ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.SEND_SMS}, REQUEST_PERMISSION_READ_SMS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_READ_SMS: {
                // If permission granted
                if (grantResults.length != 2
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED
                        || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(icon, R.string.grant_permission_sms,
                            Snackbar.LENGTH_LONG)
                            .setAction("GRANT", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // All devices previous API 23 has all the permissions granted at the moment
                                    // the app is installed
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{
                                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                                Manifest.permission.SEND_SMS},
                                                REQUEST_PERMISSION_READ_SMS);
                                    }
                                }
                            })
                            .show();
                }
            }
        }
    }

    public void setFacebookLoginButton(){
        loginButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        loginButton.setReadPermissions(Collections.singletonList("public_profile, email"));
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                facebookLoginSuccess(loginResult);
            }

            @Override
            public void onCancel() {
                int i = 0;
            }

            @Override
            public void onError(FacebookException error) {
                int i = 0;
            }
        });
    }

    private void facebookLoginSuccess(LoginResult loginResult){
        Log.v("LoginActivity login", loginResult.toString());
        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String email = object.getString("email");
                    UserInfo tmp = new UserInfo(email, "");
                    if ((new ServerInterface()).userAlreadyRegistered(tmp, getBaseContext())) {
                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
                        intent.putExtra(UserInfo.PARAM_MAIL, email);
                        startActivity(intent);
                    }
                    else {
                        String first_name = object.getString("first_name");
                        String last_name = object.getString("last_name");

                        startRegisterProcess(email, first_name, last_name);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,first_name,last_name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void doLogin(View view) {
        Intent intent = new Intent(this, AuthenticateActivity.class);
        startActivity(intent);
    }

    public void doRegister(View view) {
        startRegisterProcess(null, null, null);
    }

    private void startRegisterProcess(String mail, String name, String surname) {
        Intent intent = new Intent(this, RegisterActivity.class);
        if (mail != null)
            intent.putExtra(UserInfo.PARAM_MAIL, mail);
        if (name != null)
            intent.putExtra(UserInfo.PARAM_NAME, name);
        if (surname != null)
            intent.putExtra(UserInfo.PARAM_SURNAME, surname);
        startActivity(intent);
    }
}
