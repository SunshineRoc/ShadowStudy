package com.tencent.shadow.core.runtime;

import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.Intent;
import android.util.Log;

/**
 * 通过反射创建插件的 Application 和四大组件的实例对象
 */
public class ShadowAppComponentFactory {

    private final String TAG = ShadowAppComponentFactory.class.getSimpleName();

    public ShadowApplication instantiateApplication(ClassLoader cl,
                                                    String className)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {

        Log.v(TAG, "instantiateApplication() ==> 调用插件，className=" + className + "，cl=" + cl);

        return (ShadowApplication) cl.loadClass(className).newInstance();
    }

    public ShadowActivity instantiateActivity(ClassLoader cl, String className,
                                              Intent intent)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {

        Log.v(TAG, "instantiateActivity() ==> 调用插件，className=" + className + "，cl=" + cl);

        return (ShadowActivity) cl.loadClass(className).newInstance();
    }

    public BroadcastReceiver instantiateReceiver(ClassLoader cl,
                                                 String className, Intent intent)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {

        Log.v(TAG, "instantiateReceiver() ==> 调用插件，className=" + className + "，cl=" + cl);

        return (BroadcastReceiver) cl.loadClass(className).newInstance();
    }

    public ShadowService instantiateService(ClassLoader cl,
                                            String className, Intent intent)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {

        Log.v(TAG, "instantiateService() ==> 调用插件，className=" + className + "，cl=" + cl);

        return (ShadowService) cl.loadClass(className).newInstance();
    }

    public ContentProvider instantiateProvider(ClassLoader cl,
                                               String className)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {

        Log.v(TAG, "instantiateProvider() ==> 调用插件，className=" + className + "，cl=" + cl);

        return (ContentProvider) cl.loadClass(className).newInstance();
    }
}
