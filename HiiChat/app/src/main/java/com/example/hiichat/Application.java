package com.example.hiichat;

import android.util.Log;

import com.example.hiichat.Data.SharedPreferenceHelper;
import com.example.hiichat.Data.StaticConfig;
import com.example.hiichat.Notification.FCMInit;
import com.example.hiichat.Notification.FCMSubSend;
import com.google.firebase.messaging.FirebaseMessaging;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferenceHelper.getInstance(this);
    }
}
