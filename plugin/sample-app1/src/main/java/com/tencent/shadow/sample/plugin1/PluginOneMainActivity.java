package com.tencent.shadow.sample.plugin1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.tencent.shadow.sample.plugin.app.lib.base.plugin.R;

public class PluginOneMainActivity extends Activity {

    private final String TAG = PluginOneMainActivity.class.getSimpleName();
    private ImageView imageView;
    private final String IMAGE_URL = "https://lmg.jj20.com/up/allimg/1114/102920105033/201029105033-6-1200.jpg";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_main);

        Log.v(TAG, "onCreate()，打开插件首页，context=" + this);
        Log.v(TAG, "onCreate()，打开插件1首页，进程ID=" + android.os.Process.myPid());

        initView();
    }

    private void initView() {
        imageView = findViewById(R.id.iv_image);

        findViewById(R.id.btn_show_toast).setOnClickListener(v -> ToastUtils.show(PluginOneMainActivity.this, "这是插件1的首页"));

        findViewById(R.id.btn_send_broadcast).setOnClickListener(v -> {
            // 给插件2发送广播
            Intent intent = new Intent();
            intent.setAction("com.tencent.shadow.sample.plugin2.receiver.PluginTwoBroadcastReceiver.action");
            intent.putExtra("KEY_BROADCAST_DATA", "这是插件1发送的广播消息");
            sendBroadcast(intent);
        });

        findViewById(R.id.btn_show_image).setOnClickListener(v -> {
            Glide.with(PluginOneMainActivity.this)
                    .setDefaultRequestOptions(
                            new RequestOptions()
                                    .frame(1000000)
                                    .centerCrop())
                    .load(R.drawable.tupian)
                    .into(imageView);
        });
    }
}
