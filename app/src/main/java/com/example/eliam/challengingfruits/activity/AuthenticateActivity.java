package com.example.eliam.challengingfruits.activity;

import android.accounts.AccountAuthenticatorActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.example.eliam.challengingfruits.R;
import com.example.eliam.challengingfruits.UserInfo;
import com.example.eliam.challengingfruits.UserInfoUtility;
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

public class AuthenticateActivity extends AccountAuthenticatorActivity {

    private CallbackManager mCallbackManager;

    // UI references
    TextView register_view;
    LoginButton loginButton;
    private EditText mEmailView;
    private EditText mPasswordView;
    private ProgressDialog progress;

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
        setContentView(R.layout.activity_authenticate);

        progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.title_logging_in));
        progress.setCancelable(false);

        Utils.user = null;

        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.show_hide_pwd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean tag = Boolean.parseBoolean(v.getTag().toString());
                showHidePassword(tag);

                if (!tag)
                    v.setBackgroundResource(R.drawable.ic_visibility_off);
                else
                    v.setBackgroundResource(R.drawable.ic_visibility_on);
                v.setTag(!tag);
            }
        });

        findViewById(R.id.email_sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        register_view = (TextView) findViewById(R.id.register_text);
        loginButton = (LoginButton) findViewById(R.id.facebook_login);

        String text = getString(R.string.register_text);
        SpannableString ss = new SpannableString(text);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                doRegister("", "", "");
            }
        };
        ss.setSpan(clickableSpan, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        register_view.setText(ss);
        register_view.setMovementMethod(LinkMovementMethod.getInstance());

        mCallbackManager = CallbackManager.Factory.create();

        setFacebookLoginButton();
    }

    private void handleLoginResult(int ris, UserInfo user, final Context context){
        showProgress(false);

        switch (ris){
            // Se il login ha avuto successo
            case ServerInterface.OK:
                Intent intent = new Intent();
                intent.putExtra(UserInfo.PARAM_MAIL, user.getMail());
                intent.putExtra(UserInfo.PARAM_PASSWORD, user.getPassword());

                // Termino il login
                setAccountAuthenticatorResult(intent.getExtras());
                setResult(RESULT_OK);

                // Avvio la pagina principale dell'app
                intent = new Intent(context, MainActivity.class);
                intent.putExtra(UserInfo.PARAM_MAIL, user.getMail());
                startActivity(intent);

                break;
            case ServerInterface.ERROR:
                mPasswordView.setError(getString(R.string.error_credentials_not_valid));
                mPasswordView.requestFocus();

                Snackbar.make(register_view, R.string.error_generic, Snackbar.LENGTH_LONG)
                        .show();
                break;
            case ServerInterface.ERROR_GENERIC:
                Snackbar.make(mEmailView, R.string.error_generic, Snackbar.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
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

                        doRegister(email, first_name, last_name);
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

    private void doRegister(String mail, String first_name, String last_name){
        Bundle extras = new Bundle();
        extras.putString(UserInfo.PARAM_MAIL, mail);
        extras.putString(UserInfo.PARAM_NAME, first_name);
        extras.putString(UserInfo.PARAM_SURNAME, last_name);

        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }
        else {
            // Check for a valid email address.
            if (TextUtils.isEmpty(email)) {
                mEmailView.setError(getString(R.string.error_field_required));
                focusView = mEmailView;
                cancel = true;
            } else if (!isEmailValid(email)) {
                mEmailView.setError(getString(R.string.error_invalid_email));
                focusView = mEmailView;
                cancel = true;
            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            Snackbar.make(register_view, R.string.error_generic, Snackbar.LENGTH_LONG).show();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            UserInfo user = new UserInfo(email, password);
            int loggedIn = (new ServerInterface()).login(user, this);

            handleLoginResult(loggedIn, user, this);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }

    private boolean isPasswordValid(String password) {
        return !TextUtils.isEmpty(password);
    }

    public void showProgress(final boolean show) {
        if (show) progress.show();
        else if(progress != null && progress.isShowing()) progress.dismiss();
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

    public void showHidePassword(boolean show) {
        int start = mPasswordView.getSelectionStart();
        int end = mPasswordView.getSelectionEnd();

        if (show)
            mPasswordView.setTransformationMethod(new PasswordTransformationMethod());
        else
            mPasswordView.setTransformationMethod(null);

        mPasswordView.setSelection(start, end);
    }
}
