package com.hdasync;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void setUp() {
        Log.d("ApplicationTest", "setup");
    }

    public void testApplication() {
        Log.d("ApplicationTest", "testApplication");
    }
}