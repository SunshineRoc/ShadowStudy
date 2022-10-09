package com.tencent.shadow.sample.plugin3;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.tencent.shadow.sample.plugin.app.lib.base.plugin.R;
import com.tencent.shadow.sample.plugin3.data.DialogParams;
import com.tencent.shadow.sample.plugin3.view.CommonDialog;
import com.tencent.shadow.sample.plugin3.view.PermissionWindow;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PluginThreeMainActivity extends Activity {

    private TextView tvNetworkResponse;
    private PermissionWindow permissionWindow;
    private CommonDialog commonDialog;
    private Call call;

    private final String NETWORK_URL = "https://www.baidu.com/";
    private final String TAG = PluginThreeMainActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_main);

        initView();
    }

    private void initView() {
        findViewById(R.id.bt_show_popup_window).setOnClickListener(v -> {
            showPermissionWindow();
        });

        findViewById(R.id.bt_show_dialog).setOnClickListener(v -> {
            showDialog();
        });

        tvNetworkResponse = findViewById(R.id.tv_network_response);
        findViewById(R.id.bt_request_network).setOnClickListener(V -> {
            requestNetwork();
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
     * OkHttp
     */
    private void requestNetwork() {
        if (call == null) {
            OkHttpClient okHttpClient = new OkHttpClient();
            Request.Builder builder = new Request.Builder();
            Request request = builder
                    .get()
                    .url(NETWORK_URL)
                    .build();
            call = okHttpClient.newCall(request);
        }

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure()，网络请求失败，e=" + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> tvNetworkResponse.setText("网络请求失败：" + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    final String res = response.body().string();
                    runOnUiThread(() -> tvNetworkResponse.setText(res));
                    Log.i(TAG, "onResponse()，网络请求成功，res=" + res);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "onResponse()，网络响应解析异常，e=" + e.getMessage());
                }
            }
        });
    }
}
