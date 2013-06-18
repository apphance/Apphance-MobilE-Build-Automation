package com.apphance.flow.android;

import android.app.Activity;
import android.os.Bundle;

/**
 * Test activity
 */
public class TestActivity extends android.app.Activity {

	final static public java.lang.String APP_KEY = "YOUR_KEY";

	/**
	 * Called when the activity is first created.
	 */
	public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Apphance.startNewSession(this, APP_KEY, Mode.QA);
    }
}
