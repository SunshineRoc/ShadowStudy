package com.tencent.shadow.sample.plugin2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.tencent.shadow.sample.plugin.app.lib.R;

import java.lang.reflect.Method;

public class PluginTwoMainActivity extends Activity {

    private final String TAG = PluginTwoMainActivity.class.getSimpleName();
    private String hostMessage; // 宿主传来的消息

    private TextView tvHostMessage; // 展示宿主传来的消息

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_main);

        Log.v(TAG, "onCreate()，打开插件2首页，进程ID=" + android.os.Process.myPid());

        initData();
        initView();
    }

    private void initData() {
        Bundle bundle = getIntent().getExtras();
        Log.v(TAG, "initData()，插件2首页解析宿主传递的信息，bundle=" + bundle);

        if (bundle != null) {
            hostMessage = bundle.getString("KEY_MESSAGE_TO_PLUGIN");
            Log.v(TAG, "initData()，插件2首页解析宿主传递的信息，hostMessage=" + hostMessage);
        }
    }

    private void initView() {

        // 展示宿主发来的信息
        tvHostMessage = findViewById(R.id.tv_host_message);
        tvHostMessage.setText(hostMessage);

        findViewById(R.id.btn_call_host).setOnClickListener(v -> {
            // 插件2调用宿主
            addSuffix("插件2调用宿主的addSuffix()方法");
        });

        findViewById(R.id.btn_call_plugin1).setOnClickListener(v -> {
            // 插件2调用插件1
            showToast("插件2调用插件1的ToastUtils");
        });

        findViewById(R.id.btn_call_plugin3).setOnClickListener(v -> {
            // 插件2调用插件3
            requestNetwork();
        });

        findViewById(R.id.btn_send_broadcast).setOnClickListener(v -> {
            // 发送广播，供其它插件接收
            sendBroadcastToOtherPlugin();
        });
    }

    /**
     * 通过反射，调用宿主中的 StringUtils 类的 addSuffix() 方法。
     * 证明不可行，需要使用跨进程通信的方式。
     */
    private void addSuffix(String message) {
        try {
            Class clazz = Class.forName("com.shadow.study.utils.StringUtils");
            Log.v(TAG, "addSuffix()，clazz=" + clazz);

            Method addSuffix = clazz.getMethod("addSuffix", String.class);
            Log.v(TAG, "addSuffix()，addSuffix=" + addSuffix);

            Object stringUtils = clazz.newInstance();
            Log.v(TAG, "addSuffix()，stringUtils=" + stringUtils);

            String result = (String) addSuffix.invoke(stringUtils, message);
            Log.v(TAG, "addSuffix()，result=" + result);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "addSuffix()，e=" + e.getMessage());
        }
    }

    /**
     * 通过反射，调用插件1中的 ToastUtils 类的 show() 方法
     */
    private void showToast(String message) {
        try {
            Class toastUtilsClazz = Class.forName("com.tencent.shadow.sample.plugin1.ToastUtils");
            Log.v(TAG, "showToast()，toastUtilsClazz=" + toastUtilsClazz);

            Method show = toastUtilsClazz.getMethod("show", Context.class, String.class);
            Log.v(TAG, "showToast()，show=" + show);

            Object toastUtils = toastUtilsClazz.newInstance();
            Log.v(TAG, "showToast()，toastUtils=" + toastUtils);

            show.invoke(toastUtils, PluginTwoMainActivity.this, message);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "showToast()，e=" + e.getMessage());
        }
    }

    /**
     * 通过反射，调用插件3中的 OkHttpManager 类的 requestGet() 方法
     */
    private void requestNetwork() {
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

    /**
     * 给插件1、插件3发送广播
     */
    private void sendBroadcastToOtherPlugin() {
        Intent intent1 = new Intent();
        intent1.setAction("com.tencent.shadow.sample.plugin1.receiver.PluginOneBroadcastReceiver.action");
        intent1.putExtra("KEY_BROADCAST_DATA", "这是插件2发送的广播消息");
        sendBroadcast(intent1);

        Intent intent2 = new Intent();
        intent2.setAction("com.tencent.shadow.sample.plugin3.receiver.PluginThreeBroadcastReceiver.action");
        intent2.putExtra("KEY_BROADCAST_DATA", "这是插件2发送的广播消息");
        sendBroadcast(intent2);
    }
}
