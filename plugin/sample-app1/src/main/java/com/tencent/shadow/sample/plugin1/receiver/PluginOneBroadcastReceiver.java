package com.tencent.shadow.sample.plugin1.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class PluginOneBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = PluginOneBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive()，插件1收到广播：" + intent);

        if (intent != null) {
            String message = intent.getStringExtra("KEY_BROADCAST_DATA");
            Log.v(TAG, "onReceive()，插件1收到广播：" + message);

            Toast.makeText(context, "插件1收到广播：" + message, Toast.LENGTH_LONG).show();
        }
    }
}
