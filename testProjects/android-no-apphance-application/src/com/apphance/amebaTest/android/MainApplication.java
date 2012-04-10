package com.apphance.amebaTest.android;

import android.app.Application;
import android.util.Log;


public class MainApplication extends Application {
    private static final String TAG = MainApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "Test");
    }
}
