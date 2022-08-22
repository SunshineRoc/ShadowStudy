package com.shadow.study;

import static com.tencent.shadow.sample.constant.Constant.PART_KEY_PLUGIN_BASE;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.shadow.study.plugin.PluginHelper;
import com.shadow.study.plugin.PluginLoadActivity;
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

    /**
     * 安装插件
     */
    private void installPlugin() {
        PluginHelper.getInstance().init(this);
    }

    /**
     * 加载插件
     */
    private void loadPlugin() {
        PluginHelper.getInstance().singlePool.execute(() -> {
            Intent intent = new Intent(MainActivity.this, PluginLoadActivity.class);
            intent.putExtra(Constant.KEY_PLUGIN_PART_KEY, PART_KEY_PLUGIN_BASE);
            // 设置插件的启动页
            intent.putExtra(Constant.KEY_ACTIVITY_CLASSNAME, "com.tencent.shadow.sample.plugin.PluginMainActivity");
            startActivity(intent);
        });
    }

    /**
     * 卸载插件
     */
    private void uninstallPlugin() {

    }
}