package com.example.eliam.challengingfruits.activity;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.eliam.challengingfruits.R;
import com.example.eliam.challengingfruits.UserInfo;
import com.example.eliam.challengingfruits.UserInfoUtility;
import com.example.eliam.challengingfruits.Utils;
import com.example.eliam.challengingfruits.accountmanager.AccountGeneral;
import com.example.eliam.challengingfruits.accountmanager.ServerInterface;

import java.io.File;

public class RegisterActivity extends AccountAuthenticatorActivity {

    private final int RESULT_LOAD_IMAGE = 89;
    public final int REQUEST_PERMISSION_READ_STORAGE = 91;

    private String mEmail, mFirstName, mLastName;
    private File mProfileImage = null;

    private AccountManager mAccountManager;

    // UI references.
    private EditText mNameView, mSurnameView, mEmailView, mPhoneView, mPasswordView;
    private ImageView mUserImage;
    private ProgressDialog progress;
    private ProgressBar progressBar;


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
        setContentView(R.layout.activity_register);

        if (getIntent() != null) {
            mEmail = getIntent().getStringExtra(UserInfo.PARAM_MAIL);
            mFirstName = getIntent().getStringExtra(UserInfo.PARAM_NAME);
            mLastName = getIntent().getStringExtra(UserInfo.PARAM_SURNAME);
        }
        mAccountManager = AccountManager.get(this);

        // Set up the signup form.
        mUserImage = (ImageView) findViewById(R.id.circleView);
        mNameView = (EditText) findViewById(R.id.name);
        mSurnameView = (EditText) findViewById(R.id.surname);
        mEmailView = (EditText) findViewById(R.id.email);
        mPhoneView = (EditText) findViewById(R.id.phone);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.register_button || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        mUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // All devices previous API 23 has all the permissions granted at the moment
                // the app is installed
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    // Check whether the app has the permission to read the storage.
                    if (ContextCompat.checkSelfPermission(getBaseContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_PERMISSION_READ_STORAGE);
                    }
                    else {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_PICK);

                        startActivityForResult(Intent.createChooser(intent,
                                getString(R.string.choose_profile_img)),
                                RESULT_LOAD_IMAGE);
                    }
                }
                else {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_PICK);

                    startActivityForResult(Intent.createChooser(intent,
                            getString(R.string.choose_profile_img)),
                            RESULT_LOAD_IMAGE);
                }
            }
        });

        mNameView.setText(mFirstName);
        mSurnameView.setText(mLastName);
        mEmailView.setText(mEmail);

        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        if (mRegisterButton != null)
            mRegisterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptRegister();
                }
            });

        progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.registering));
        progress.setCancelable(false);

        progressBar = (ProgressBar) findViewById(R.id.progress);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] imagePathColumn = {MediaStore.Images.Media.DATA};

            Cursor imageCursor = getContentResolver().query(selectedImage,
                    imagePathColumn, null, null, null);

            if (imageCursor != null){
                imageCursor.moveToFirst();

                int index = imageCursor.getColumnIndex(imagePathColumn[0]);
                String mediaPath = imageCursor.getString(index);
                imageCursor.close();

                mProfileImage = new File(mediaPath);

                Utils.loadImage(mediaPath, mUserImage, progressBar);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_READ_STORAGE: {
                // If permission granted
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_PICK);

                    startActivityForResult(Intent.createChooser(intent,
                            getString(R.string.choose_profile_img)),
                            RESULT_LOAD_IMAGE);
                } else {
                    Snackbar.make(mUserImage, R.string.error_no_memory_permission,
                            Snackbar.LENGTH_SHORT)
                            .setAction(R.string.permit_app, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                REQUEST_PERMISSION_READ_STORAGE);
                                    }
                                }
                            })
                            .show();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, AuthenticateActivity.class);
        startActivity(intent);
    }

    private UserInfo finishRegister(UserInfo user, Context context) {
        Intent intent = new Intent();
        intent.putExtra(UserInfo.PARAM_MAIL, user.getMail());
        intent.putExtra(UserInfo.PARAM_NAME, user.getFirstName());
        intent.putExtra(UserInfo.PARAM_SURNAME, user.getLastName());
        intent.putExtra(UserInfo.PARAM_PHONE, user.getPhone());
        intent.putExtra(UserInfo.PARAM_PASSWORD, user.getPassword());
        intent.putExtra(UserInfo.PARAM_LOCAL_PROFILE_PIC, user.getPicLocalPath());

        String accountName = intent.getStringExtra(UserInfo.PARAM_MAIL);
        String accountPassword = intent.getStringExtra(UserInfo.PARAM_PASSWORD);
        final Account account = new Account(accountName, AccountGeneral.ACCOUNT_TYPE);
        Account[] accounts = mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);
        boolean createAccount = true;

        for (Account account1 : accounts)
            if (account1.name.equals(accountName)) createAccount = false;

        if (createAccount)
            mAccountManager.addAccountExplicitly(account, accountPassword, intent.getExtras());

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);

        return UserInfoUtility.setUserInfo(intent, context, user.getPicLocalPath());
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptRegister() {
        // Reset errors.
        mNameView.setError(null);
        mSurnameView.setError(null);
        mEmailView.setError(null);
        mPhoneView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String name = mNameView.getText().toString();
        String surname = mSurnameView.getText().toString();
        String email = mEmailView.getText().toString();
        String phone = mPhoneView.getText().toString();
        String password = mPasswordView.getText().toString();
        String picPath = "";

        if (mProfileImage != null)
            picPath = mProfileImage.getAbsolutePath();

        boolean cancel = false;
        View focusView = null;

        // Check if the user has entered his first name
        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        }
        // Check if the user has entered his last name
        else if (TextUtils.isEmpty(surname)) {
            mSurnameView.setError(getString(R.string.error_field_required));
            focusView = mSurnameView;
            cancel = true;
        }
        // Check for a valid email address.
        else if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }
        else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }
        // Check if the user has entered his phone number
        else if (TextUtils.isEmpty(phone)) {
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        }
        else if (!isPhoneValid(phone)) {
            mPhoneView.setError(getString(R.string.error_missing_phone_prefix));
            focusView = mPhoneView;
            cancel = true;
        }
        // Check if the user has entered the password
        else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            Utils.user = new UserInfo(email, password, name, surname, phone);
            Utils.user.setPicLocal(picPath);
            if (!(new ServerInterface()).userAlreadyRegistered(Utils.user, this)) {
                Utils.user = finishRegister(Utils.user, this);

                if (Utils.user != null) {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                }
                else
                    Snackbar.make(mNameView, R.string.error_generic,
                            Snackbar.LENGTH_SHORT).show();
            }
            else
                Snackbar.make(mNameView, R.string.error_mail_already_used,
                        Snackbar.LENGTH_SHORT).show();
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }

    private boolean isPhoneValid(String phone) {
        return phone.startsWith("+") || phone.startsWith("00");
    }

    public void showProgress(final boolean show) {
        if (show) progress.show();
        else if(progress != null && progress.isShowing()) progress.dismiss();
    }

    public void doBack(View view) {
        Intent intent = new Intent(this, AuthenticateActivity.class);
        startActivity(intent);
    }
}
