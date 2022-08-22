package com.shadow.study;

import static com.tencent.shadow.sample.constant.Constant.PART_KEY_PLUGIN_MAIN_APP;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.shadow.study.plugin.PluginHelper;
import com.tencent.shadow.dynamic.host.EnterCallback;
import com.tencent.shadow.sample.constant.Constant;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {

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

            // 根据插件apk包，创建PluginManager
            HostApplication.getApp().loadPluginManager(PluginHelper.getInstance().pluginManagerFile);

            Bundle bundle = new Bundle();
            // 插件路径
            bundle.putString(Constant.KEY_PLUGIN_ZIP_PATH, PluginHelper.getInstance().pluginZipFile.getAbsolutePath());
            // 插件Key
            bundle.putString(Constant.KEY_PLUGIN_PART_KEY, PART_KEY_PLUGIN_MAIN_APP);
            // 宿主加载插件时显示的启动页，该启动页显示在插件进程中
            bundle.putString(Constant.KEY_ACTIVITY_CLASSNAME, "com.tencent.shadow.sample.plugin.PluginMainActivity");

            Logger.getLogger(MainActivity.class.getSimpleName()).log(Level.INFO, "准备打开插件");

            // 进入插件
            HostApplication.getApp().getPluginManager()
                    .enter(MainActivity.this, Constant.FROM_ID_START_ACTIVITY, bundle, new EnterCallback() {
                        @Override
                        public void onShowLoadingView(final View view) {
                            // 宿主加载插件过程中的过渡UI
//                            mHandler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    mViewGroup.addView(view);
//                                }
//                            });
                        }

                        @Override
                        public void onCloseLoadingView() {
//                            finish();
                        }

                        @Override
                        public void onEnterComplete() {

                        }
                    });
        });
    }

    /**
     * 卸载插件
     */
    private void uninstallPlugin() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HostApplication.getApp().getPluginManager().enter(this, Constant.FROM_ID_CLOSE, null, null);
    }
}