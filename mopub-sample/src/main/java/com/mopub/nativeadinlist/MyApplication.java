package com.mopub.nativeadinlist;

import android.app.Application;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MopubController.getInstance(this);

    }
}
