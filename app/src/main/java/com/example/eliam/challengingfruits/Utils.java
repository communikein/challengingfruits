package com.example.eliam.challengingfruits;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;


/**
 * Created by eliam on 01/06/2016.
 */
public class Utils {

    public static final SimpleDateFormat dayMonth =
            new SimpleDateFormat("dd/MM", Locale.getDefault());
    public static final SimpleDateFormat dateTime =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    public static final SimpleDateFormat date =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    public static final SimpleDateFormat time =
            new SimpleDateFormat("HH:ss", Locale.getDefault());
    public static final SimpleDateFormat year =
            new SimpleDateFormat("yyyy", Locale.getDefault());
    public static final SimpleDateFormat databaseDateTime =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    public static final DecimalFormat latLngFormat = new DecimalFormat("##.######");

    public static UserInfo user;
    public static ArrayList<Challenge> challenges = new ArrayList<>();

    public static void loadImage(final String imagePath, final ImageView target,
                                 final ProgressBar progress){
        if (progress != null) {
            progress.setVisibility(View.VISIBLE);
            target.setVisibility(View.GONE);
        }

        (new AsyncTask<Void, Void, Bitmap>(){
            @Override
            protected Bitmap doInBackground(Void... params) {
                File file = new File(imagePath);

                Bitmap bmp = null;
                if (file.exists())
                    bmp = BitmapFactory.decodeFile(file.getAbsolutePath());

                return bmp;
            }

            @Override
            protected void onPostExecute(Bitmap image) {
                target.setImageBitmap(image);
                if (progress != null) {
                    progress.setVisibility(View.GONE);
                    target.setVisibility(View.VISIBLE);
                }
            }
        }).execute();
    }

    public static void initFacebook(Context context){
        FacebookSdk.sdkInitialize(context);
        //perhaps a bit excessive
        FacebookSdk.addLoggingBehavior(LoggingBehavior.GRAPH_API_DEBUG_INFO);
        FacebookSdk.addLoggingBehavior(LoggingBehavior.DEVELOPER_ERRORS);
        FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_RAW_RESPONSES);
        FacebookSdk.setApplicationId(context.getString(R.string.facebook_app_id));
    }
}
