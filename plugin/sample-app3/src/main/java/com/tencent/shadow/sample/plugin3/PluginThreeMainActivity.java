package com.tencent.shadow.sample.plugin3;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.tencent.shadow.sample.plugin.app.lib.base.plugin.R;
import com.tencent.shadow.sample.plugin3.data.DialogParams;
import com.tencent.shadow.sample.plugin3.view.CommonDialog;
import com.tencent.shadow.sample.plugin3.view.PermissionWindow;

public class PluginThreeMainActivity extends Activity {

    private PermissionWindow permissionWindow;
    private CommonDialog commonDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_main);

        initView();
    }

    private void initView() {
        findViewById(R.id.bt_show_popup_window).setOnClickListener(v -> {
            if (permissionWindow == null) {
                permissionWindow = new PermissionWindow(PluginThreeMainActivity.this);
            }
            permissionWindow.setTitle("需要申请设备信息权限")
                    .setMessage("需要申请设备信息权限，读取Android-id、IMEI，目的为保护帐号安全。拒绝或取消授权，不影响使用其他服务。")
                    .showHorizontalCenter();
        });

        findViewById(R.id.bt_show_dialog).setOnClickListener(v -> {
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
        });
    }
}
