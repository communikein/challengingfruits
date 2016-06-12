package com.example.eliam.challengingfruits.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.eliam.challengingfruits.Challenge;
import com.example.eliam.challengingfruits.R;
import com.example.eliam.challengingfruits.UserInfo;
import com.example.eliam.challengingfruits.UserInfoUtility;
import com.example.eliam.challengingfruits.Utils;
import com.example.eliam.challengingfruits.fragment.ChallengesFragment;
import com.example.eliam.challengingfruits.fragment.SettingsFragment;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String NAV_ITEM_ID = "navItemId";
    private static final long DRAWER_CLOSE_DELAY_MS = 250;
    private int mNavItemId = -1;
    private int startNavItemId;

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle mDrawerToggle;
    ImageView profile_img;
    TextView name_txt, mail_txt;

    private final Handler mDrawerActionHandler = new Handler();

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
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences("MENU_DATA", 0);
        int menuId = sharedPreferences.getInt("MENU_ITEM", -1);

        Bundle extras = (getIntent() != null) ? getIntent().getExtras() : null;
        parseData(savedInstanceState, extras);
        initDrawer();

        if (menuId >= 0)
            navigate(menuId);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateDrawer();
    }

    public void initDrawer(){
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView view = (NavigationView) findViewById(R.id.navigation_view);
        if (view != null) {
            view.setNavigationItemSelectedListener(this);
            // select the correct nav menu item
            view.getMenu().findItem(startNavItemId).setChecked(true);
            View header_view = view.inflateHeaderView(R.layout.nav_drawer_header);
            name_txt = (TextView) header_view.findViewById(R.id.name);
            mail_txt = (TextView) header_view.findViewById(R.id.email);
            profile_img = (ImageView) header_view.findViewById(R.id.circleView);
        }

        navigate(startNavItemId);

        // set up the hamburger icon to open and close the drawer
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, null, R.string.openDrawer,
                R.string.closeDrawer);
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_action_menu);
        drawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        updateDrawer();
    }

    public void updateDrawer() {
        name_txt.setText(String.format(getString(R.string.name_surname),
                Utils.user.getLastName(), Utils.user.getFirstName()));
        mail_txt.setText(Utils.user.getMail());

        if (Utils.user.getPicLocal() != null && Utils.user.getPicLocal().exists())
            Utils.loadImage(Utils.user.getPicLocalPath(), profile_img, null);
    }

    public void doShowMenu(View v){
        if (!drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.openDrawer(GravityCompat.START);
    }

    private void parseData(Bundle savedInstanceState, Bundle extras) {
        String mail = extras.getString(UserInfo.PARAM_MAIL);

        if (mail != null && Utils.user == null)
            Utils.user = UserInfoUtility.getUserInfo(mail, this);

        // load saved navigation state if present
        if (null == savedInstanceState) {
            startNavItemId = R.id.drawer_home;
        } else {
            startNavItemId = savedInstanceState.getInt(NAV_ITEM_ID);
        }
    }

    public void navigate(int menuItemID) {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawers();

        SharedPreferences sharedPreferences = getSharedPreferences("MENU_DATA", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (menuItemID != mNavItemId){
            mNavItemId = menuItemID;

            FragmentManager fragmentManager = getSupportFragmentManager();
            switch (menuItemID) {
                /************** MAP LIST TAB ****************/
                case R.id.drawer_home:
                    editor.putInt("MENU_ENTRY", menuItemID);

                    fragmentManager.beginTransaction()
                            .replace(R.id.container, new ChallengesFragment(), ChallengesFragment.TAG)
                            .commit();
                    break;
                /************** SETTINGS TAB ****************/
                case R.id.drawer_settings:
                    editor.putInt("MENU_ENTRY", menuItemID);

                    fragmentManager.beginTransaction()
                            .replace(R.id.container, SettingsFragment.newInstance(
                                    Utils.user.toString()), SettingsFragment.TAG)
                            .commit();
                    break;
                /************** LOGOUT TAB *****************/
                case R.id.drawer_logout:
                    editor.putInt("MENU_ENTRY", -1);

                    UserInfoUtility.removeAccount(this);

                    startActivity(new Intent(this, AuthenticateActivity.class));
                    break;
            }

            editor.apply();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case SettingsFragment.REQUEST_PERMISSION_READ_STORAGE_CHOOSE_PIC: {
                // If permission granted
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_PICK);

                    startActivityForResult(
                            Intent.createChooser(intent,getString(R.string.choose_profile_img)),
                            SettingsFragment.RESULT_LOAD_IMAGE);
                } else {
                    Snackbar.make(drawerLayout,
                            R.string.error_no_memory_permission, Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SettingsFragment.RESULT_LOAD_IMAGE &&
                resultCode == Activity.RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] imagePathColumn = {MediaStore.Images.Media.DATA};

            ContentResolver contentResolver = getContentResolver();
            Cursor imageCursor = contentResolver.query(selectedImage,
                    imagePathColumn, null, null, null);

            if (imageCursor != null){
                imageCursor.moveToFirst();

                int index = imageCursor.getColumnIndex(imagePathColumn[0]);
                String mediaPath = imageCursor.getString(index);
                imageCursor.close();

                SettingsFragment fragment = (SettingsFragment)
                        getSupportFragmentManager().findFragmentByTag(SettingsFragment.TAG);
                if (fragment != null)
                    fragment.updateProfileImage(mediaPath);
                else
                    Snackbar.make(drawerLayout, R.string.error_generic, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(NAV_ITEM_ID, mNavItemId);
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
        // update highlighted item in the navigation menu
        menuItem.setChecked(true);

        // allow some time after closing the drawer before performing real navigation
        // so the user can see what is happening
        drawerLayout.closeDrawer(GravityCompat.START);
        mDrawerActionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                navigate(menuItem.getItemId());
            }
        }, DRAWER_CLOSE_DELAY_MS);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            navigate(R.id.drawer_home);
        }
    }

    public void doBack(View view) {
        onBackPressed();
    }
}
