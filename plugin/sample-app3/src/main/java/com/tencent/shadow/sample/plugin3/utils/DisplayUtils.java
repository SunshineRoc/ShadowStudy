package com.tencent.shadow.sample.plugin3.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class DisplayUtils {

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        return getScreenRealWidth(context);
    }

    public static int getScreenRealWidth(Context context) {
        WindowManager manager = ((Activity) context).getWindowManager();
        int width4 = getRealScreenSize(manager)[0];
        return width4;
    }

    public static int getScreenVisibleWidth(Context context) {
        WindowManager manager = ((Activity) context).getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int width2 = outMetrics.widthPixels;
        int height2 = outMetrics.heightPixels;
        return width2;
    }

    /**
     * 可见屏幕尺寸和真实物理尺寸是否一致,返回差异size
     * >0 表示真实屏幕比可见屏幕大
     * =0 表示一样大
     * <0 一般不会<0
     *
     * @param context
     * @return 差值
     */
    public static int screenWidthDiffSize(Context context) {
        int width2 = getScreenVisibleWidth(context);
        int width4 = getScreenRealWidth(context);

        int window_width = getWindowVisibleWidth((Activity) context);
        int window_height = getWindowVisibleHeight((Activity) context);

        return width4 - width2;
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        return ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getHeight();
    }

    /**
     * 获得状态栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusHeight(Context context) {

        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height").get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    /**
     * 获取屏幕真实尺寸（包括虚拟按键）
     *
     * @param windowManager
     * @return
     */
    public static int[] getRealScreenSize(WindowManager windowManager) {

        android.view.Display localDisplay = windowManager.getDefaultDisplay();
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        localDisplay.getMetrics(localDisplayMetrics);

        int width = 0, height = 0;
        if ((Build.VERSION.SDK_INT >= 14) && (Build.VERSION.SDK_INT < 17))
            try {
                width = ((Integer) android.view.Display.class.getMethod("getRawWidth",
                        new Class[0]).invoke(localDisplay, new Object[0]))
                        .intValue();
                height = ((Integer) android.view.Display.class.getMethod("getRawHeight",
                        new Class[0]).invoke(localDisplay, new Object[0]))
                        .intValue();
            } catch (Exception localException2) {
                localException2.printStackTrace();
            }
        if (Build.VERSION.SDK_INT >= 17)
            try {
                Point localPoint = new Point();
                android.view.Display.class.getMethod("getRealSize",
                        new Class[]{Point.class}).invoke(localDisplay,
                        new Object[]{localPoint});
                width = localPoint.x;
                height = localPoint.y;
            } catch (Exception localException1) {
                localException1.printStackTrace();
            }

        int[] sizes = new int[2];
        sizes[0] = width;
        sizes[1] = height;
        return sizes;
    }

    public static int getWindowVisibleWidth(Activity activity) {

        /**
         * 获取应用区域宽度
         */
        Rect outRect1 = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect1);
        int window_width = outRect1.width();
        return window_width;
    }

    public static int getWindowVisibleHeight(Activity activity) {

        /**
         * 获取应用区域高度
         */
        Rect outRect1 = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect1);
        int window_height = outRect1.height();
        return window_height;
    }
}
