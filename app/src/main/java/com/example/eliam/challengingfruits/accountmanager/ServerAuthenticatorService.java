package com.example.eliam.challengingfruits.accountmanager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ServerAuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return (new ServerAuthenticator(this)).getIBinder();
    }
}
