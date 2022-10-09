package com.tencent.shadow.sample.plugin3;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.tencent.shadow.sample.plugin.app.lib.base.plugin.R;
import com.tencent.shadow.sample.plugin3.view.NetworkInfoView;

public class SecondActivity extends Activity {

    private Handler handler;
    private NetworkInfoView networkInfoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_second);

        initView();
    }

    private void initView() {
        networkInfoView = findViewById(R.id.network_info_view);

        if (handler == null) {
            handler = new Handler();
        }
        handler.postDelayed(() -> networkInfoView.setTopTitle("系统公告")
                .setInformation("尊敬的用户：系统正在维护，请稍后再试。")
                .refresh(), 3 * 1000);
    }
}
