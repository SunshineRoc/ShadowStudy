package com.example.hotfix.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.shadow.study.HostMainActivity;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.v("MainActivity", "onCreate()，打开APP首页，进程ID=" + android.os.Process.myPid());

        initView();
    }

    private void initView() {
        findViewById(R.id.bt_open_plugin).setOnClickListener(view -> {
            // 打开SDK首页
            startActivity(new Intent(MainActivity.this, HostMainActivity.class));

            showSdkToast("打开SDK首页");
        });
    }

    /**
     * 通过反射，调用SDK宿主中的 HostToastUtils 类的 show() 方法
     */
    private void showSdkToast(String message) {
        try {
            Class toastUtilsClazz = Class.forName("com.shadow.study.utils.HostToastUtils");
            Log.v(TAG, "showSdkToast()，toastUtilsClazz=" + toastUtilsClazz);

            Method show = toastUtilsClazz.getMethod("show", Context.class, String.class);
            Log.v(TAG, "showSdkToast()，show=" + show);

            Object toastUtils = toastUtilsClazz.newInstance();
            Log.v(TAG, "showSdkToast()，toastUtils=" + toastUtils);

            show.invoke(toastUtils, MainActivity.this, message);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "showToast()，e=" + e.getMessage());
        }
    }
}