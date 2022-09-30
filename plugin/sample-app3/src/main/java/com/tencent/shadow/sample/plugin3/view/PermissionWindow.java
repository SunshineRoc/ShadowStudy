package com.tencent.shadow.sample.plugin3.view;

import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.tencent.shadow.sample.plugin.app.lib.base.plugin.R;
import com.tencent.shadow.sample.plugin3.utils.DisplayUtils;

/**
 * 显示权限信息的对话框
 */
public class PermissionWindow extends PopupWindow {

    private Activity activity;
    private View rootView;
    private TextView tvTitle;
    private TextView tvMessage;

    private static final float WINDOW_RATIO_WIDTH = 0.9f; // 窗口宽度与屏幕宽度的比率
    private static final float WINDOW_RATIO_MARGIN_TOP = 0.06f; // 窗口顶部与屏幕顶部间距占屏幕高度的比率

    public PermissionWindow(Activity activity) {
        // 对话框宽度默认设置为屏幕宽度的 WINDOW_RATIO_WIDTH 倍
        this(activity, (int) (DisplayUtils.getScreenWidth(activity) * WINDOW_RATIO_WIDTH), WindowManager.LayoutParams.WRAP_CONTENT);
    }

    /**
     * @param activity
     * @param width    窗口宽度
     * @param height   窗口高度
     */
    public PermissionWindow(Activity activity, int width, int height) {
        super(null, width, height, false);
        this.activity = activity;
        setAnimationStyle(R.style.WindowAnimationStyle);
        setClippingEnabled(false);
        setOutsideTouchable(true);

        rootView = View.inflate(activity, R.layout.window_permission, null);
        tvTitle = rootView.findViewById(R.id.tv_title);
        tvMessage = rootView.findViewById(R.id.tv_message);
        setContentView(rootView);
    }

    /**
     * 设置标题
     *
     * @param title 标题
     */
    public PermissionWindow setTitle(String title) {
        tvTitle.setText(title);
        return this;
    }

    /**
     * 设置内容
     *
     * @param message 内容
     */
    public PermissionWindow setMessage(String message) {
        tvMessage.setText(message);
        return this;
    }

    /**
     * 水平居中显示
     */
    public void showHorizontalCenter() {
        // 窗口顶部与屏幕顶部的间距默认设置为屏幕高度的 WINDOW_RATIO_MARGIN_TOP 倍
        showHorizontalCenter((int) (DisplayUtils.getScreenHeight(activity) * WINDOW_RATIO_MARGIN_TOP));
    }

    /**
     * 水平居中显示
     *
     * @param marginTop 窗口顶部与屏幕顶部的间距
     */
    public void showHorizontalCenter(int marginTop) {
        try {
            if (activity != null
                    && activity.getWindow() != null
                    && activity.getWindow().getDecorView() != null) {

                showAsDropDown(activity.getWindow().getDecorView(),
                        (DisplayUtils.getScreenWidth(activity) - getWidth()) / 2,
                        marginTop - activity.getWindow().getDecorView().getHeight());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
