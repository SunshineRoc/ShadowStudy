package com.shadow.study;

import static com.tencent.shadow.sample.constant.Constant.PART_KEY_PLUGIN_APP_ONE;
import static com.tencent.shadow.sample.constant.Constant.PART_KEY_PLUGIN_APP_THREE;
import static com.tencent.shadow.sample.constant.Constant.PART_KEY_PLUGIN_APP_TWO;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.shadow.study.plugin.PluginHelper;
import com.shadow.study.utils.PermissionManager;
import com.tencent.shadow.core.common.LoggerFactory;
import com.tencent.shadow.dynamic.host.EnterCallback;
import com.tencent.shadow.sample.constant.Constant;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPermission();
        initView();
    }

    private void initView() {

        findViewById(R.id.bt_install_plugin).setOnClickListener(v -> {
            // 安装插件
            installPlugin();
        });

        findViewById(R.id.bt_load_plugin1).setOnClickListener(v -> {
            // 加载插件1
            loadPlugin(PART_KEY_PLUGIN_APP_ONE, "1", "com.tencent.shadow.sample.plugin1.PluginOneMainActivity");
        });

        findViewById(R.id.bt_load_plugin2).setOnClickListener(v -> {
            // 加载插件2
            loadPlugin(PART_KEY_PLUGIN_APP_TWO, "2", "com.tencent.shadow.sample.plugin2.PluginTwoMainActivity");
        });

        findViewById(R.id.bt_load_plugin3).setOnClickListener(v -> {
            // 加载插件3
            loadPlugin(PART_KEY_PLUGIN_APP_THREE, "3", "com.tencent.shadow.sample.plugin3.PluginThreeMainActivity");
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
        PluginHelper.getInstance().installPluginManager();
        PluginHelper.getInstance().installPlugin("1");
        PluginHelper.getInstance().installPlugin("2");
        PluginHelper.getInstance().installPlugin("3");
    }

    /**
     * 加载插件
     *
     * @param partKey
     * @param number
     * @param className
     */
    private void loadPlugin(String partKey, String number, String className) {
        PluginHelper.getInstance().singlePool.execute(() -> {

            LoggerFactory.getLogger(PluginHelper.class).info("loadPlugin() ==> 准备打开插件，插件路径：" + PluginHelper.getInstance().getPluginZipFile(number).getAbsolutePath());

            // 根据插件apk包，创建PluginManager
            HostApplication.getApplication().loadPluginManager(PluginHelper.getInstance().pluginManagerDestinationFile);

            Bundle bundle = new Bundle();
            // 插件路径
            bundle.putString(Constant.KEY_PLUGIN_ZIP_PATH, PluginHelper.getInstance().getPluginZipFile(number).getAbsolutePath());
            // 插件Key
            bundle.putString(Constant.KEY_PLUGIN_PART_KEY, partKey);
            // 宿主加载插件时显示的启动页，该启动页显示在插件进程中
            bundle.putString(Constant.KEY_ACTIVITY_CLASSNAME, className);

            // 进入插件
            HostApplication.getApplication().getPluginManager()
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

    /**
     * 初始化存储权限
     */
    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (PermissionManager.getInstance().lacksPermission(PermissionManager.PERMISSIONS_STORAGE[0])) {
                ActivityCompat.requestPermissions(this, PermissionManager.PERMISSIONS_STORAGE, PermissionManager.REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionManager.REQUEST_CODE) {
            if (permissions[0].equals(Manifest.permission.DELETE_CACHE_FILES)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 同意授权
            } else {
                // 不同意授权
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HostApplication.getApplication().getPluginManager().enter(this, Constant.FROM_ID_CLOSE, null, null);
    }
}