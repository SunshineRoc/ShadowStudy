package com.tencent.shadow.sample.plugin.app.lib.demo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.tencent.shadow.sample.plugin.app.lib.R;
import com.tencent.shadow.sample.plugin.app.lib.gallery.cases.entity.UseCase;
import com.tencent.shadow.sample.plugin.app.lib.gallery.util.ToastUtil;

public class PluginDemoActivity extends Activity {

    public static class Case extends UseCase {
        @Override
        public String getName() {
            return "插件Demo";
        }

        @Override
        public String getSummary() {
            return "测试插件Demo是否正确回调";
        }

        @Override
        public Class getPageClass() {
            return PluginDemoActivity.class;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_main);
        ToastUtil.showToast(this, "onCreate");
    }
}
