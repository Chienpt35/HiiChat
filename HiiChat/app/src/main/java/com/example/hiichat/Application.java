package com.example.hiichat;

import com.example.hiichat.Data.SharedPreferenceHelper;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferenceHelper.getInstance(this);
    }
}
