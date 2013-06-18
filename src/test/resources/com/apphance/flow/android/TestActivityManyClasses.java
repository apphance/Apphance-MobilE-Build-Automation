package com.apphance.flow.android;

import android.app.Activity;
import android.os.Bundle;

class A {}
class B extends A {}
interface I {}
class C extends B implements I {}

public class TestActivityManyClasses extends Activity {

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
