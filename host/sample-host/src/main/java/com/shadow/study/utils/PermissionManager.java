package com.shadow.study.utils;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.shadow.study.HostApplication;

import java.util.Observable;

public class PermissionManager extends Observable {

    private static Context context;
    private static PermissionManager instance;

    public static final int REQUEST_CODE = 1000;

    // 初始化所需权限 - 存储权限
    public static String[] PERMISSIONS_STORAGE = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    // 初始化所需权限 - 读取设备状态权限
    public static String PERMISSIONS_PHONE = Manifest.permission.READ_PHONE_STATE;

    private PermissionManager() {
        this.context = HostApplication.getApplication();
    }

    public static PermissionManager getInstance() {

        if (instance == null) {
            synchronized (PermissionManager.class) {
                if (instance == null) {
                    instance = new PermissionManager();
                }
            }
        }

        return instance;
    }

    /**
     * Check permissions,all permission lack！
     */
    public boolean lacksPermissions(String... permissions) {

        boolean allPermissionGrand = true;
        for (String permission : permissions) {
            allPermissionGrand = allPermissionGrand && lacksPermission(permission);
        }
        return allPermissionGrand;
    }

    /**
     * Check lack Permissions
     */
    public boolean lacksPermission(String permission) {
        return !selfPermissionGranted(context, permission);
    }

    /**
     * 检查权限是否授权
     * 注意：Android 6.0M的权限判断兼容问题
     * 1、targetSDKVersion < 23时使用，使用PermissionChecker.checkSelfPermission
     * 2、targetSDKVersion >= 23，使用ContextCompat.checkSelfPermission
     *
     * @param context
     * @param permission
     * @return
     */
    public static boolean selfPermissionGranted(Context context, String permission) {
        boolean ret = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permission != null) {
            if (context.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.M) {
                ret = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
            } else {
                ret = PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED;
            }
        }
        return ret;
    }

    /**
     * 请求某个权限
     *
     * @param permission
     * @param requestCode
     */
    public void requestRealPermissions(String[] permission, int requestCode) {
        if (context != null && permission != null) {
            ActivityCompat.requestPermissions((Activity) context, permission, requestCode);
        }
    }
}