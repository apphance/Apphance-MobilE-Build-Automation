package com.apphance.amebaTest.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class TestActivity extends Activity {
    private static final String TAG = TestActivity.class.getSimpleName();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.e(TAG, "Test");
    }
}
