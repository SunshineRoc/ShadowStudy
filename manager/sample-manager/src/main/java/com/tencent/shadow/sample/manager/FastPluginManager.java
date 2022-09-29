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
import com.tencent.shadow.sample.constant.Constant;

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
     * <p>
     * 1、从 /data/user/0/APP包名/files/ 目录中获取config.json文件
     * 2、oDex优化 runtime 和 loader
     * 3、oDex优化 插件
     * 4、插件安装完成时，把runtime、loader、插件信息保存到数据中
     *
     * @param sourceDirectory 插件安装前所在的目录
     * @param oDex            是否oDex
     */
    public void installPlugin(File sourceDirectory, boolean oDex) throws IOException, JSONException, InterruptedException, ExecutionException {
        /*
          从 /data/user/0/APP包名/files/ 目录中获取config.json文件
          */
        PluginConfig pluginConfig = getPluginConfig(sourceDirectory, Constant.FILE_NAME_CONFIG);
        LoggerFactory.getLogger(FastPluginManager.class).info("installPlugin() ==> 安装插件：sourceDirectory=" + sourceDirectory + "，pluginConfig=" + pluginConfig);
        if (pluginConfig != null) {
            LoggerFactory.getLogger(FastPluginManager.class).info("installPlugin() ==> 安装插件：" +
                    "\nUUID=" + pluginConfig.UUID
                    + "，UUID_NickName=" + pluginConfig.UUID_NickName
                    + "，version=" + pluginConfig.version);
        }

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
          oDex优化插件
          */
        for (Map.Entry<String, PluginConfig.PluginFileInfo> plugin : pluginConfig.plugins.entrySet()) {
            final String partKey = plugin.getKey();
            final File apkFile = plugin.getValue().file;
            // 解压插件apk中的so
            Future<Pair<String, String>> extractSo = mFixedPool.submit(() -> extractSo(uuid, partKey, apkFile));
            futures.add(extractSo);
            extractSoFutures.add(extractSo);
            if (oDex) {
                // oDex优化插件
                Future oDexPlugin = mFixedPool.submit(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        oDexPlugin(uuid, partKey, apkFile);
                        return null;
                    }
                });
                futures.add(oDexPlugin);
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

        // 插件安装完成时，把插件信息保存到数据库中
        onInstallCompleted(pluginConfig, soDirMap);
    }

    /**
     * 获取已安装的runtime、loader和插件的信息
     */
    public InstalledPlugin getInstallPluginInfo() {
        return getInstalledPlugins(1).get(0);
    }

    /**
     * 跨进程调用远程程PluginLoaderBinder中的callApplicationOnCreate()方法，传递宿主的Application，实例化ContentProvider实例并初始化。
     */
    protected void callApplicationOnCreate(String partKey) throws RemoteException {
        LoggerFactory.getLogger(FastPluginManager.class).info("callApplicationOnCreate() ==> 调用插件");

        Map map = mPluginLoader.getLoadedPlugin();
        Boolean isCall = (Boolean) map.get(partKey);
        if (isCall == null || !isCall) {
            mPluginLoader.callApplicationOnCreate(partKey);
        }
    }

    /**
     * 加载 loader 和 runtime
     * 1、创建并启动插件进程
     * 2、在插件进程中加载runtime，最终会把runtime挂到pathclassLoader之上，即把DynamicRuntime对应的ClassLoader的parent修改为runtime apk 对应的BaseDexClassLoader，形成如下结构的classLoader树：
     * ---BootClassLoader
     * ----RuntimeClassLoader
     * ------PathClassLoader
     * 3、在插件进程中加载loader，最终获取PluginLoaderBinder对象，加载插件时会用到该对象。PluginLoaderBinder继承自Binder，具有跨进程通信能力。
     */
    protected void loadPluginLoaderAndRuntime(String uuid, String partKey) throws RemoteException, TimeoutException, FailedException {
        LoggerFactory.getLogger(FastPluginManager.class).info("loadPluginLoaderAndRuntime() ==> 加载 loader 和 runtime：" +
                "\nuuid=" + uuid
                + "，partKey=" + partKey);

        /*
          第一步：创建、启动并绑定插件进程。getPluginProcessServiceName()获取插件进程的service的全类名，bindPluginProcessService()启动并绑定插件service。bindPluginProcessService()在sdk/dynamic目录的dynamic-manager模块中，也就是说，启动并绑定插件进程是在dynamic-manager模块中进行的。
        */
        if (mPpsController == null) {
            bindPluginProcessService(getPluginProcessServiceName(partKey));
            waitServiceConnected(10, TimeUnit.SECONDS);
        }

        /*
         第二步：在插件进程中跨进程加载runtime。该方法在sdk/dynamic目录的dynamic-manager模块中实现的，该方法会调用到PpsController类的loadRuntime()方法，然后跨进程在PluginProcessService类中加载runtime。PluginProcessService类中加载runtime的步骤如下：
         1、获取已安装的 runtime 的apk的信息。
         2、根据已安装的 runtime 的apk的信息，加载runtime，最终会把runtime挂到pathclassLoader之上，即把DynamicRuntime对应的ClassLoader的parent修改为runtime apk 对应的BaseDexClassLoader，形成如下结构的classLoader树：
          ---BootClassLoader
          ----RuntimeClassLoader
          ------PathClassLoader
         3、把最新加载的 runtime 信息保存到SharedPreferences中。
       */
        loadRunTime(uuid);

       /*
        第三步：在插件进程中加载loader。该方法在sdk/dynamic的dynamic-manager模块中实现。该方法会调用到PpsController类的loadPluginLoader()方法，然后跨进程在PluginProcessService类中加载loader。PluginProcessService类中加载loader的步骤如下：
        1、根据uuid获取已安装的 loader 的apk的信息。
        2、根据已安装的loader的apk的信息，加载loader，最终获取PluginLoaderBinder对象（加载插件时就会用到该对象），并传入宿主的Application，供加载启动插件时使用。PluginLoaderBinder继承自Binder，具有跨进程通信能力。
        */
        loadPluginLoader(uuid);
    }

    /**
     * 加载插件
     *
     * @param partKey 插件的partKey
     */
    protected void loadPlugin(String partKey) throws RemoteException {
        LoggerFactory.getLogger(FastPluginManager.class).info("loadPlugin() ==> 加载Plugin");

        Map map = mPluginLoader.getLoadedPlugin();
        if (!map.containsKey(partKey)) {
            mPluginLoader.loadPlugin(partKey);
        }
    }


    protected abstract String getPluginProcessServiceName(String partKey);

}
