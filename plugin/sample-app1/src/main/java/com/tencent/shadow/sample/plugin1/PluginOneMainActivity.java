package com.tencent.shadow.sample.plugin1;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.tencent.shadow.sample.plugin.app.lib.base.plugin.R;

public class PluginOneMainActivity extends Activity {

    private ImageView imageView;
    private final String IMAGE_URL = "https://lmg.jj20.com/up/allimg/1114/102920105033/201029105033-6-1200.jpg";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_main);

        Log.v("PluginOneMainActivity", "onCreate()，打开插件首页，context=" + this);

        initView();
    }

    private void initView() {
        imageView = findViewById(R.id.iv_image);

        findViewById(R.id.btn_show_toast).setOnClickListener(v -> ToastUtils.show(PluginOneMainActivity.this, "这是插件1的首页"));

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
