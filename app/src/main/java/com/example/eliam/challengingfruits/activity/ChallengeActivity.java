package com.example.eliam.challengingfruits.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;

import com.example.eliam.challengingfruits.Challenge;
import com.example.eliam.challengingfruits.R;
import com.example.eliam.challengingfruits.Utils;

import java.text.ParseException;
import java.util.Date;

public class ChallengeActivity extends AppCompatActivity {

    EditText target_txt, deadline_txt, points_txt, challenge_txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        initUI();
    }

    private void initUI() {
        target_txt = (EditText) findViewById(R.id.target_txt);
        deadline_txt = (EditText) findViewById(R.id.deadline_txt);
        points_txt = (EditText) findViewById(R.id.points_txt);
        challenge_txt = (EditText) findViewById(R.id.challenge_txt);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null)
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    doSendChallenge();
                }
            });
    }

    private void doSendChallenge() {
        Challenge c;
        Date deadline;

        try {
            deadline = Utils.date.parse(deadline_txt.getText().toString());
        } catch (ParseException e) {
            deadline = null;
        }

        if (deadline != null) {
            c = new Challenge(Utils.user.getPhone(), deadline,
                    Integer.parseInt(points_txt.getText().toString()),
                    challenge_txt.getText().toString());

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(target_txt.getText().toString(), null,
                    c.toSMS(), null, null);
        }
        else {
            Snackbar.make(target_txt, "ERROR", Snackbar.LENGTH_LONG).show();
        }

        Snackbar.make(target_txt, "You challenged", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public void doBack(View view) {
        onBackPressed();
    }
}
