package com.shadow.study.utils;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * 资源管理类
 */
public class ResourceUtils {

    public static int getLayoutId(Context paramContext, String resName) {
        return getRid(paramContext, "layout", resName);
    }

    public static int getStringId(Context paramContext, String resName) {

        return getRid(paramContext, "string", resName);
    }

    public static int getDrawableId(Context paramContext, String resName) {
        return getRid(paramContext, "drawable", resName);
    }


    public static int getStyleId(Context paramContext, String resName) {
        return getRid(paramContext, "style", resName);
    }

    public static int getResourceId(Context paramContext, String resName) {
        return getRid(paramContext, "id", resName);
    }

    public static int getColorId(Context paramContext, String resName) {

        return getRid(paramContext, "color", resName);
    }

    public static int getArrayId(Context paramContext, String resName) {

        return getRid(paramContext, "array", resName);
    }

    public static int getAnimId(Context paramContext, String resName) {

        return getRid(paramContext, "anim", resName);
    }

    public static int getDimenId(Context paramContext, String resName) {
        return getRid(paramContext, "dimen", resName);
    }

    public static int[] getStyleableArray(Context paramContext, String resName) {
        return (int[]) getResourceId(paramContext, resName, "styleable");
    }

    public static int getStyleableId(Context context, String name) {
        return ((Integer) getResourceId(context, name, "styleable")).intValue();
    }

    /**
     * 对于 context.getResources().getIdentifier 无法获取的数据 , 或者数组
     * 资源反射值
     *
     * @param name
     * @param type
     * @return
     * @paramcontext
     */
    private static Object getResourceId(Context context, String name, String type) {
        String className = context.getPackageName() + ".R";
        try {
            Class cls = Class.forName(className);
            for (Class childClass : cls.getClasses()) {
                String simple = childClass.getSimpleName();
                if (simple.equals(type)) {
                    for (Field field : childClass.getFields()) {
                        String fieldName = field.getName();
                        if (fieldName.equals(name)) {
                            return field.get(null);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static int getRid(Context context, String resType, String resName) {

        int rid = 0;
        try {
            rid = context.getResources().getIdentifier(resName, resType, context.getPackageName());
        } catch (Exception e) {
            Log.e("SDK", "[" + resType + "] resName:" + resName + " NOT found!");
        }
        return rid;
    }


}
