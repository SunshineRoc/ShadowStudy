package com.tencent.shadow.sample.plugin2;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.tencent.shadow.sample.plugin.app.lib.R;

public class PluginTwoMainActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_main);
    }
}
