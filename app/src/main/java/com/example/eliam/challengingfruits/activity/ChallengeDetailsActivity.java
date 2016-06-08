package com.example.eliam.challengingfruits.activity;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.eliam.challengingfruits.Challenge;
import com.example.eliam.challengingfruits.R;
import com.example.eliam.challengingfruits.UserInfo;
import com.github.lzyzsd.circleprogress.DonutProgress;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.concurrent.TimeUnit;


public class ChallengeDetailsActivity extends AppCompatActivity {

    DonutProgress days_progress, hours_progress, minutes_progress;
    TextView ownerName_txt, what_txt, deadline_txt;

    long leftMillis;
    Challenge challenge;

    Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            ex.printStackTrace();
            int i=0;
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(handler);
        setContentView(R.layout.activity_challenge_details);

        Bundle extras = (getIntent() != null) ? getIntent().getExtras() : null;
        parseData(extras);

        if (challenge == null)
            finish();
        else
            initUI();
    }

    public void parseData(Bundle extras) {
        if (extras != null) {
            String tmp = extras.getString(Challenge.CHALLENGE_JSON);

            try {
                JSONObject obj = new JSONObject(tmp);

                challenge = new Challenge(obj);
            } catch (JSONException e) {
                challenge = null;
            }
        }
    }

    public void initUI() {
        days_progress = (DonutProgress) findViewById(R.id.arc_progress_days);
        hours_progress = (DonutProgress) findViewById(R.id.arc_progress_hours);
        minutes_progress = (DonutProgress) findViewById(R.id.arc_progress_minutes);

        hours_progress.setMax(23);
        minutes_progress.setMax(59);

        long currentMillis = (new Date()).getTime();
        long jobMillis = challenge.getDeadline().getTime();
        leftMillis = jobMillis - currentMillis;
        long tmpMillis = leftMillis;

        long days = TimeUnit.MILLISECONDS.toDays(tmpMillis);
        tmpMillis = tmpMillis - TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(tmpMillis);
        tmpMillis = tmpMillis - TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(tmpMillis);

        days_progress.setProgress((int) days);
        hours_progress.setProgress((int) hours);
        minutes_progress.setProgress((int) minutes);

        ownerName_txt = (TextView) findViewById(R.id.owner_name_txt);
        what_txt = (TextView) findViewById(R.id.what_txt);
        deadline_txt = (TextView) findViewById(R.id.deadline_txt);

        ownerName_txt.setText(challenge.getChallengedBy());
        what_txt.setText(getString(R.string.what_value, challenge.getWhat()));
        deadline_txt.setText(getString(R.string.deadline_value, challenge.printDeadline()));
    }

}
