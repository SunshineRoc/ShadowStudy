package com.shadow.study;

import static com.tencent.shadow.sample.constant.Constant.PART_KEY_PLUGIN_APP_ONE;
import static com.tencent.shadow.sample.constant.Constant.PART_KEY_PLUGIN_APP_THREE;
import static com.tencent.shadow.sample.constant.Constant.PART_KEY_PLUGIN_APP_TWO;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.shadow.study.plugin.PluginHelper;
import com.shadow.study.utils.PermissionManager;
import com.shadow.study.utils.ResourceUtils;
import com.tencent.shadow.core.common.LoggerFactory;
import com.tencent.shadow.dynamic.host.EnterCallback;
import com.tencent.shadow.sample.constant.Constant;

public class HostMainActivity extends AppCompatActivity {

    private final String TAG = HostMainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ResourceUtils.getLayoutId(HostMainActivity.this, "activity_host_main"));

        Log.v(TAG, "onCreate()，打开宿主首页，进程ID=" + android.os.Process.myPid());

        initPermission();
        initView();
    }

    private void initView() {

        findViewById(ResourceUtils.getResourceId(HostMainActivity.this, "bt_install_plugin")).setOnClickListener(v -> {
            // 安装插件
            installPlugin();
        });

        findViewById(ResourceUtils.getResourceId(HostMainActivity.this, "bt_load_plugin1")).setOnClickListener(v -> {
            // 加载插件1
            loadPlugin(PART_KEY_PLUGIN_APP_ONE, "com.tencent.shadow.sample.plugin1.PluginOneMainActivity");
        });

        findViewById(ResourceUtils.getResourceId(HostMainActivity.this, "bt_load_plugin2")).setOnClickListener(v -> {
            // 加载插件2
            loadPlugin(PART_KEY_PLUGIN_APP_TWO, "com.tencent.shadow.sample.plugin2.PluginTwoMainActivity");
        });

        findViewById(ResourceUtils.getResourceId(HostMainActivity.this, "bt_load_plugin3")).setOnClickListener(v -> {
            // 加载插件3
            loadPlugin(PART_KEY_PLUGIN_APP_THREE, "com.tencent.shadow.sample.plugin3.PluginThreeMainActivity");
        });

        findViewById(ResourceUtils.getResourceId(HostMainActivity.this, "bt_uninstall_plugin")).setOnClickListener(v -> {
            // 卸载插件
            uninstallPlugin();
        });
    }

    /**
     * 安装插件
     */
    private void installPlugin() {
        PluginHelper.getInstance().installFromSDPluginDirectory(HostMainActivity.this);
    }

    /**
     * 加载插件
     *
     * @param partKey
     * @param className
     */
    private void loadPlugin(String partKey, String className) {
        PluginHelper.getInstance().singlePool.execute(() -> {

            LoggerFactory.getLogger(PluginHelper.class).info("loadPlugin() ==> 准备打开插件，context=" + HostMainActivity.this + "，getApplicationContext()=" + HostMainActivity.this.getApplicationContext());

            Bundle bundle = new Bundle();
            // 插件Key
            bundle.putString(Constant.KEY_PLUGIN_PART_KEY, partKey);
            // 宿主加载插件时显示的启动页，该启动页显示在插件进程中
            bundle.putString(Constant.KEY_ACTIVITY_CLASSNAME, className);

            // 进入插件
            HostApplication.getApplication().getPluginManager()
                    .enter(HostMainActivity.this, Constant.FROM_ID_START_ACTIVITY, bundle, new EnterCallback() {
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

        if (HostApplication.getApplication().getPluginManager() != null) {
            HostApplication.getApplication().getPluginManager().enter(this, Constant.FROM_ID_CLOSE, null, null);
        }
    }
}