package com.tencent.shadow.sample.plugin3;

import android.app.Application;

public class PluginThreeApplication extends Application {

    private static PluginThreeApplication instance;

    public boolean isOnCreate;

    @Override
    public void onCreate() {
        instance = this;
        isOnCreate = true;
        super.onCreate();
    }

    public static PluginThreeApplication getInstance() {
        return instance;
    }
}
