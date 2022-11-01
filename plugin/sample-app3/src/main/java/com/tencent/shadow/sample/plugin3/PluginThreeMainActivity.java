package com.tencent.shadow.sample.plugin3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.tencent.shadow.sample.plugin.app.lib.base.plugin.R;
import com.tencent.shadow.sample.plugin3.data.DialogParams;
import com.tencent.shadow.sample.plugin3.view.CommonDialog;
import com.tencent.shadow.sample.plugin3.view.PermissionWindow;

import java.lang.reflect.Method;

public class PluginThreeMainActivity extends Activity {

    private TextView tvNetworkResponse;
    private PermissionWindow permissionWindow;
    private CommonDialog commonDialog;

    private final String TAG = PluginThreeMainActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_main);

        Log.v(TAG, "onCreate()，打开插件3首页，进程ID=" + android.os.Process.myPid());

        initView();
    }

    private void initView() {
        findViewById(R.id.bt_show_popup_window).setOnClickListener(v -> {
            showPermissionWindow();
        });

        findViewById(R.id.bt_show_dialog).setOnClickListener(v -> {
            showDialog();
        });

        findViewById(R.id.bt_show_view).setOnClickListener(v -> {
            showCustomView();
        });

        tvNetworkResponse = findViewById(R.id.tv_network_response);
        findViewById(R.id.bt_request_network).setOnClickListener(V -> {
            requestGet();
        });
    }

    /**
     * 展示PopupWindow
     */
    private void showPermissionWindow() {
        if (permissionWindow == null) {
            permissionWindow = new PermissionWindow(PluginThreeMainActivity.this);
        }
        permissionWindow.setTitle("需要申请设备信息权限")
                .setMessage("需要申请设备信息权限，读取Android-id、IMEI，目的为保护帐号安全。拒绝或取消授权，不影响使用其他服务。")
                .showHorizontalCenter();
    }

    /**
     * 展示Dialog
     */
    private void showDialog() {
        if (commonDialog == null) {
            commonDialog = new CommonDialog(PluginThreeMainActivity.this);
        }
        commonDialog.setMessage("弹窗测试")
                .setClickListener(new DialogParams.ClickListener() {
                    @Override
                    public void onClickYesListener() {
                        Toast.makeText(PluginThreeMainActivity.this, "点击确定按钮", Toast.LENGTH_SHORT).show();
                        commonDialog.dismiss();
                    }

                    @Override
                    public void onClickNoListener() {
                        Toast.makeText(PluginThreeMainActivity.this, "点击取消按钮", Toast.LENGTH_SHORT).show();
                        commonDialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * 打开自定义View
     */
    private void showCustomView() {
        startActivity(new Intent(PluginThreeMainActivity.this, SecondActivity.class));
    }

    /**
     * 通过反射调用 OkHttpManager 类的 requestGet() 方法
     */
    private void requestGet() {
        try {
            Class okHttpManagerClazz = Class.forName("com.tencent.shadow.sample.plugin3.network.OkHttpManager");
            Log.v(TAG, "requestNetwork()，okHttpManagerClazz=" + okHttpManagerClazz);

//            // 获取类的构造器
//            Constructor constructor = okHttpManagerClazz.getDeclaredConstructor();
//            // 把构造器私有权限放开
//            constructor.setAccessible(true);
//            Method getInstance = okHttpManagerClazz.getMethod("getInstance");
//            Log.v(TAG, "requestNetwork()，getInstance=" + getInstance);

            Object okHttpManager = okHttpManagerClazz.newInstance();
            Log.v(TAG, "requestNetwork()，okHttpManager=" + okHttpManager);

//            Class responseListenerClazz = Class.forName("com.tencent.shadow.sample.plugin3.network.IResponseListener");
//            Log.v(TAG, "requestNetwork()，responseListenerClazz=" + responseListenerClazz);

//            Object responseListener = responseListenerClazz.newInstance();
//            Log.v(TAG, "requestNetwork()，responseListener=" + responseListener);

            Method requestGet = okHttpManagerClazz.getMethod("requestGet", String.class, Class.forName("com.tencent.shadow.sample.plugin3.network.IResponseListener"));
            Log.v(TAG, "requestNetwork()，requestGet=" + requestGet);

//            requestGet.invoke(okHttpManager, "https://www.baidu.com/", responseListener);
            requestGet.invoke(okHttpManager, "https://www.baidu.com/", null);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "requestNetwork()，e=" + e.getMessage());
        }
    }
}
