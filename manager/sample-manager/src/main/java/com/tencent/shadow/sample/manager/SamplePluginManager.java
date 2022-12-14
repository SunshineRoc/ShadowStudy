/*
 * Tencent is pleased to support the open source community by making Tencent Shadow available.
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.tencent.shadow.sample.manager;

import static com.tencent.shadow.sample.constant.Constant.PART_KEY_PLUGIN_APP_ONE;
import static com.tencent.shadow.sample.constant.Constant.PART_KEY_PLUGIN_APP_THREE;
import static com.tencent.shadow.sample.constant.Constant.PART_KEY_PLUGIN_APP_TWO;
import static com.tencent.shadow.sample.constant.Constant.PART_KEY_PLUGIN_MAIN_APP;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;

import com.tencent.shadow.core.common.LoggerFactory;
import com.tencent.shadow.core.manager.installplugin.InstalledPlugin;
import com.tencent.shadow.dynamic.host.EnterCallback;
import com.tencent.shadow.sample.constant.Constant;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SamplePluginManager extends FastPluginManager {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private Context mCurrentContext;

    public SamplePluginManager(Context context) {
        super(context);
        mCurrentContext = context;
    }

    /**
     * @return PluginManager实现的别名，用于区分不同PluginManager实现的数据存储路径
     */
    @Override
    protected String getName() {
        return "test-dynamic-manager";
    }

    /**
     * @return 宿主中注册的PluginProcessService实现的类名
     */
    @Override
    protected String getPluginProcessServiceName(String partKey) {
        if (PART_KEY_PLUGIN_MAIN_APP.equals(partKey)) {
            return "com.shadow.study.plugin.PluginProcessPPS";
        } else if (PART_KEY_PLUGIN_APP_ONE.equals(partKey)) {
            return "com.shadow.study.plugin.PluginProcessPPS";
        } else if (PART_KEY_PLUGIN_APP_TWO.equals(partKey)) {
            return "com.shadow.study.plugin.PluginProcessPPS";
        } else if (PART_KEY_PLUGIN_APP_THREE.equals(partKey)) {
            return "com.shadow.study.plugin.PluginProcessPPS";
        } else {
//            //如果有默认PPS，可用return代替throw
//            throw new IllegalArgumentException("unexpected plugin load request: " + partKey);

            return "com.shadow.study.plugin.Plugin2ProcessPPS";//在这里支持多个插件
        }
    }

    @Override
    public void enter(final Context context, long fromId, Bundle bundle, final EnterCallback callback) {
        if (fromId == Constant.FROM_ID_NOOP) {
            //do nothing.
        } else if (fromId == Constant.FROM_ID_START_ACTIVITY) {
            onStartActivity(context, bundle, callback);
        } else if (fromId == Constant.FROM_ID_CLOSE) {
            close();
        } else if (fromId == Constant.FROM_ID_LOAD_VIEW_TO_HOST) {
            loadViewToHost(context, bundle);
        } else {
            throw new IllegalArgumentException("不认识的fromId==" + fromId);
        }
    }

    private void loadViewToHost(final Context context, Bundle bundle) {
        Intent pluginIntent = new Intent();
        pluginIntent.setClassName(
                context.getPackageName(),
                "com.tencent.shadow.sample.plugin.app.lib.usecases.service.HostAddPluginViewService"
        );
        pluginIntent.putExtras(bundle);
        try {
            mPluginLoader.startPluginService(pluginIntent);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private void onStartActivity(final Context context, Bundle bundle, final EnterCallback callback) {
        final String pluginZipPath = bundle.getString(Constant.KEY_PLUGIN_ZIP_PATH);
        final String partKey = bundle.getString(Constant.KEY_PLUGIN_PART_KEY);
        final String className = bundle.getString(Constant.KEY_ACTIVITY_CLASSNAME);

        if (className == null) {
            throw new NullPointerException("className == null");
        }
        final Bundle extras = bundle.getBundle(Constant.KEY_EXTRAS);

        if (callback != null) {
            final View view = LayoutInflater.from(mCurrentContext).inflate(R.layout.activity_load_plugin, null);
            callback.onShowLoadingView(view);
        }

        executorService.execute(() -> {
            try {
                // 安装插件
                InstalledPlugin installedPlugin = installPlugin(pluginZipPath, null, true);

                LoggerFactory.getLogger(SamplePluginManager.class).info("onStartActivity() ==> 打开插件启动页：" +
                        "\npartKey=" + partKey
                        + "，installedPlugin.UUID=" + installedPlugin.UUID
                        + "，pluginZipPath=" + pluginZipPath
                        + "\nclassName=" + className);

                // 加载插件
                loadPlugin(installedPlugin.UUID, partKey);

                // 调用插件APP
                callApplicationOnCreate(partKey);

//                callApplicationOnCreate(PART_KEY_PLUGIN_APP_ONE);
//                callApplicationOnCreate(PART_KEY_PLUGIN_APP_TWO);
//                callApplicationOnCreate(PART_KEY_PLUGIN_APP_THREE);

                Intent pluginIntent = new Intent();
                pluginIntent.setClassName(
                        context.getPackageName(),
                        className
                );
                if (extras != null) {
                    pluginIntent.replaceExtras(extras);
                }
                // 把插件pluginIntent转换为代理intent。pluginIntent=Intent { cmp=com.tencent.shadow.sample.host/com.tencent.shadow.sample.plugin1.PluginOneMainActivity }，intent=Intent { flg=0x10000000 cmp=com.tencent.shadow.sample.host/com.tencent.shadow.sample.plugin.runtime.PluginDefaultProxyActivity (has extras) }
                Intent intent = mPluginLoader.convertActivityIntent(pluginIntent);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                LoggerFactory.getLogger(SamplePluginManager.class).info("onStartActivity() ==> 打开插件启动页：context=" + context + "，getApplicationContext()=" + context.getApplicationContext()
                        + "，pluginIntent=" + pluginIntent + "，intent=" + intent);

                // 在插件进程中打开插件启动页
                mPluginLoader.startActivityInPluginProcess(intent);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (callback != null) {
                callback.onCloseLoadingView();
            }
        });
    }
}
