package com.hdasync.sample;

import android.app.Application;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by scott on 16/1/6.
 */
public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }
}
