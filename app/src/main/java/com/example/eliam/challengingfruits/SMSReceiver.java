package com.example.eliam.challengingfruits;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;

import com.example.eliam.challengingfruits.activity.MainActivity;

public class SMSReceiver extends BroadcastReceiver {
    public SMSReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            String sms = "";

            if (pdus != null) for (Object pdu : pdus) {
                SmsMessage tmp = SmsMessage.createFromPdu((byte[]) pdu);
                String senderMobile = tmp.getOriginatingAddress();

                sms = tmp.getMessageBody();
                Challenge challenge = Challenge.parseFromSMS(sms, senderMobile);

                DBAdapter dbAdapter = DBAdapter.getInstance(context);
                dbAdapter.open();
                dbAdapter.newChallenge(challenge);
                dbAdapter.close();

                sendNotification(context, challenge);
            }
        }
    }

    private void sendNotification(Context context, Challenge challenge) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(challenge.getWhat())
                .setContentText(challenge.getPoints() + " points, before " + challenge.printDeadline())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
