package com.example.eliam.challengingfruits.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.eliam.challengingfruits.activity.AuthenticateActivity;
import com.example.eliam.challengingfruits.activity.MainActivity;
import com.example.eliam.challengingfruits.R;
import com.example.eliam.challengingfruits.UserInfo;
import com.example.eliam.challengingfruits.UserInfoUtility;
import com.example.eliam.challengingfruits.Utils;
import com.example.eliam.challengingfruits.accountmanager.ServerInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsFragment extends Fragment {

    public static final String TAG = "SETTINGSFRAGMENT";

    public static final int RESULT_LOAD_IMAGE = 89;
    public static final int REQUEST_PERMISSION_READ_STORAGE_CHOOSE_PIC = 91;
    private static final String PARAM_USER = "PARAM_USER";

    ImageView logout_icon;
    CircleImageView profile_img;
    TextView name_txt, city_txt;
    EditText editFirstName_text, editLastName_text,
            editMail_text, editPassword_text, editPhone_text;
    Button confirm_btt;
    ProgressDialog progress;
    ProgressBar progressBar;

    private UserInfo mUser;
    private MainActivity mainActivity;
    private File img = null;

    Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            ex.printStackTrace();
            int i = 0;
        }
    };

    public SettingsFragment() {}

    public static SettingsFragment newInstance(String user) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            try {
                JSONObject obj = new JSONObject(getArguments().getString(PARAM_USER));
                mUser = new UserInfo(obj, getActivity());
            }catch (JSONException e){
                mUser = null;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(handler);
        mainActivity = (MainActivity) getActivity();
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        UserInfoUtility.getUserInfo(Utils.user.getMail(), getActivity());

        progress = new ProgressDialog(getActivity());
        progress.setMessage(getString(R.string.title_updating));
        progress.setCancelable(false);

        progressBar = (ProgressBar) view.findViewById(R.id.progress);

        name_txt = (TextView) view.findViewById(R.id.name_text);
        profile_img = (CircleImageView) view.findViewById(R.id.profile_img);
        logout_icon = (ImageView) view.findViewById(R.id.action_logout);
        editFirstName_text = (EditText) view.findViewById(R.id.edit_first_name_text);
        editLastName_text = (EditText) view.findViewById(R.id.edit_last_name_text);
        editMail_text = (EditText) view.findViewById(R.id.edit_mail_text);
        editPassword_text = (EditText) view.findViewById(R.id.edit_password_text);
        editPhone_text = (EditText) view.findViewById(R.id.edit_phone_text);
        confirm_btt = (Button) view.findViewById(R.id.confirm_btt);

        name_txt.setText(String.format(getString(R.string.name_surname),
                mUser.getFirstName(), mUser.getLastName()));
        editFirstName_text.setText(mUser.getFirstName());
        editLastName_text.setText(mUser.getLastName());
        editMail_text.setText(mUser.getMail());
        editPassword_text.setText(mUser.getPassword());
        editPhone_text.setText(mUser.getPhone());

        Utils.loadImage(mUser.getPicLocalPath(), profile_img, progressBar);

        profile_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // All devices prevoius API 16 has READ_EXTERNAL_STORAGE permission by default
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){

                    // Check whether the app has the permission to read the storage.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_PERMISSION_READ_STORAGE_CHOOSE_PIC);
                    }
                    else {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_PICK);

                        getActivity().startActivityForResult(
                                Intent.createChooser(intent, getString(R.string.choose_profile_img)),
                                RESULT_LOAD_IMAGE);
                    }
                }
                else {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_PICK);

                    getActivity().startActivityForResult(
                            Intent.createChooser(intent, getString(R.string.choose_profile_img)),
                            RESULT_LOAD_IMAGE);
                }
            }
        });
        logout_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserInfoUtility.removeAccount(getActivity());

                startActivity(new Intent(getActivity(), AuthenticateActivity.class));
            }
        });
        confirm_btt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doConfirm();
            }
        });
    }

    public void doConfirm(){
        showProgress(true);
        mUser.setFirstName(editFirstName_text.getText().toString());
        mUser.setLastName(editLastName_text.getText().toString());
        mUser.setMail(editMail_text.getText().toString());
        mUser.setPhone(editPhone_text.getText().toString());
        mUser.setPassword(editPassword_text.getText().toString());

        Utils.user = (new ServerInterface()).updateAccount(mUser, getActivity());

        showProgress(false);
        if (Utils.user != null)
            new AlertDialog.Builder(getActivity())
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setMessage(mainActivity.getString(R.string.data_saved))
                    .setCancelable(false)
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mainActivity.updateDrawer();
                            mainActivity.navigate(R.id.drawer_home);
                        }
                    })
                    .show();
        else
            Snackbar.make(confirm_btt, R.string.error_generic,
                    Snackbar.LENGTH_SHORT).show();
    }

    public void updateProfileImage(String mediaPath){
        img = new File(mediaPath);
        profile_img.setImageResource(R.drawable.ic_person);

        Utils.loadImage(mediaPath, profile_img, progressBar);
    }

    public void showProgress(final boolean show) {
        if (show) progress.show();
        else if(progress != null && progress.isShowing()) progress.dismiss();
    }
}
