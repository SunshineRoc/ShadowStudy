package com.tencent.shadow.sample.plugin3.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class PluginThreeBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = PluginThreeBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive()，插件3收到广播：" + intent);

        if (intent != null) {
            String message = intent.getStringExtra("KEY_BROADCAST_DATA");
            Log.v(TAG, "onReceive()，插件3收到广播：" + message);

            Toast.makeText(context, "插件3收到广播：" + message, Toast.LENGTH_LONG).show();
        }
    }
}
