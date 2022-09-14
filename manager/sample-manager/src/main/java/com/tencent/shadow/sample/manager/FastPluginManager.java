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

import android.content.Context;
import android.os.RemoteException;
import android.util.Pair;

import com.tencent.shadow.core.common.Logger;
import com.tencent.shadow.core.common.LoggerFactory;
import com.tencent.shadow.core.manager.installplugin.InstalledPlugin;
import com.tencent.shadow.core.manager.installplugin.InstalledType;
import com.tencent.shadow.core.manager.installplugin.PluginConfig;
import com.tencent.shadow.dynamic.host.FailedException;
import com.tencent.shadow.dynamic.manager.PluginManagerThatUseDynamicLoader;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class FastPluginManager extends PluginManagerThatUseDynamicLoader {

    private static final Logger mLogger = LoggerFactory.getLogger(FastPluginManager.class);

    private ExecutorService mFixedPool = Executors.newFixedThreadPool(4);

    public FastPluginManager(Context context) {
        super(context);
    }


    /**
     * 安装插件
     * 1、从zip包中解压插件
     * 2、oDex优化 runtime 和 loader
     * 3、oDex优化 插件
     * 4、触发插件安装完成时的回调
     * 5、获取已安装的插件
     */
    public InstalledPlugin installPlugin(String zip, String hash, boolean odex) throws IOException, JSONException, InterruptedException, ExecutionException {
        /*
          从zip包中解压插件
          */
        final PluginConfig pluginConfig = installPluginFromZip(new File(zip), hash);
        final String uuid = pluginConfig.UUID;
        List<Future> futures = new LinkedList<>();
        List<Future<Pair<String, String>>> extractSoFutures = new LinkedList<>();

        /*
          oDex优化 runtime 和 loader
        */
        if (pluginConfig.runTime != null && pluginConfig.pluginLoader != null) {
            Future odexRuntime = mFixedPool.submit(new Callable() {
                @Override
                public Object call() throws Exception {
                    oDexPluginLoaderOrRunTime(uuid, InstalledType.TYPE_PLUGIN_RUNTIME,
                            pluginConfig.runTime.file);
                    return null;
                }
            });
            futures.add(odexRuntime);
            Future odexLoader = mFixedPool.submit(new Callable() {
                @Override
                public Object call() throws Exception {
                    oDexPluginLoaderOrRunTime(uuid, InstalledType.TYPE_PLUGIN_LOADER,
                            pluginConfig.pluginLoader.file);
                    return null;
                }
            });
            futures.add(odexLoader);
        }

        /*
          oDex优化 插件
          */
        for (Map.Entry<String, PluginConfig.PluginFileInfo> plugin : pluginConfig.plugins.entrySet()) {
            final String partKey = plugin.getKey();
            final File apkFile = plugin.getValue().file;
            // 解压插件apk中的so
            Future<Pair<String, String>> extractSo = mFixedPool.submit(() -> extractSo(uuid, partKey, apkFile));
            futures.add(extractSo);
            extractSoFutures.add(extractSo);
            if (odex) {
                // oDex优化插件
                Future odexPlugin = mFixedPool.submit(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        oDexPlugin(uuid, partKey, apkFile);
                        return null;
                    }
                });
                futures.add(odexPlugin);
            }
        }

        for (Future future : futures) {
            future.get();
        }
        Map<String, String> soDirMap = new HashMap<>();
        for (Future<Pair<String, String>> future : extractSoFutures) {
            Pair<String, String> pair = future.get();
            soDirMap.put(pair.first, pair.second);
        }
        // 插件安装完成时的回调
        onInstallCompleted(pluginConfig, soDirMap);

        // 获取已安装的插件
        return getInstalledPlugins(1).get(0);
    }


    protected void callApplicationOnCreate(String partKey) throws RemoteException {
        //
        Map map = mPluginLoader.getLoadedPlugin();
        Boolean isCall = (Boolean) map.get(partKey);
        if (isCall == null || !isCall) {
            mPluginLoader.callApplicationOnCreate(partKey);
        }
    }

    /**
     * 加载 loader 和 runtime
     * 1、创建并启动插件进程
     * 2、在插件进程中加载runtime
     * 3、在插件进程中加载loader
     */
    private void loadPluginLoaderAndRuntime(String uuid, String partKey) throws RemoteException, TimeoutException, FailedException {
        // 绑定插件进程
        if (mPpsController == null) {
            bindPluginProcessService(getPluginProcessServiceName(partKey));
            waitServiceConnected(10, TimeUnit.SECONDS);
        }

        // 在插件进程中加载runtime
        loadRunTime(uuid);

        // 在插件进程中加载loader
        loadPluginLoader(uuid);
    }

    /**
     * 加载插件
     *
     * @param uuid    插件的uuid
     * @param partKey 插件的partKey
     */
    protected void loadPlugin(String uuid, String partKey) throws RemoteException, TimeoutException, FailedException {
        // 加载 loader 和 runtime
        loadPluginLoaderAndRuntime(uuid, partKey);
        Map map = mPluginLoader.getLoadedPlugin();
        if (!map.containsKey(partKey)) {
            mPluginLoader.loadPlugin(partKey);
        }
    }


    protected abstract String getPluginProcessServiceName(String partKey);

}
