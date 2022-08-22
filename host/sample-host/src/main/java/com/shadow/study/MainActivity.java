package com.shadow.study;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.tencent.shadow.dynamic.host.EnterCallback;
import com.tencent.shadow.sample.constant.Constant;

public class MainActivity extends AppCompatActivity {

    private ViewGroup mViewGroup;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {

        mViewGroup = findViewById(R.id.ll_root);

        findViewById(R.id.bt_install_plugin).setOnClickListener(v -> {
            // 安装插件
            installPlugin();
        });

        findViewById(R.id.bt_load_plugin).setOnClickListener(v -> {
            // 加载插件
            loadPlugin();
        });

        findViewById(R.id.bt_uninstall_plugin).setOnClickListener(v -> {
            // 卸载插件
            uninstallPlugin();
        });
    }

    private void installPlugin() {

    }

    private void loadPlugin() {
        PluginHelper.getInstance().singlePool.execute(() -> {
            HostApplication.getApp().loadPluginManager(PluginHelper.getInstance().pluginManagerFile);

            Bundle bundle = new Bundle();
            // 插件 zip，这几个参数也都可以不传，直接在 PluginManager 中硬编码
            bundle.putString(Constant.KEY_PLUGIN_ZIP_PATH, PluginHelper.getInstance().pluginZipFile.getAbsolutePath());
            // partKey 每个插件都有自己的 partKey 用来区分多个插件
            bundle.putString(Constant.KEY_PLUGIN_PART_KEY, getIntent().getStringExtra(Constant.KEY_PLUGIN_PART_KEY));
            // 路径举例：com.google.samples.apps.sunflower.GardenActivity
            bundle.putString(Constant.KEY_ACTIVITY_CLASSNAME, getIntent().getStringExtra(Constant.KEY_ACTIVITY_CLASSNAME));

            HostApplication.getApp().getPluginManager()
                    .enter(MainActivity.this, Constant.FROM_ID_START_ACTIVITY, bundle, new EnterCallback() {
                        @Override
                        public void onShowLoadingView(final View view) {
                            mHandler.post(() -> mViewGroup.addView(view));
                        }

                        @Override
                        public void onCloseLoadingView() {
                            finish();
                        }

                        @Override
                        public void onEnterComplete() {

                        }
                    });
        });
    }

    private void uninstallPlugin() {

    }
}