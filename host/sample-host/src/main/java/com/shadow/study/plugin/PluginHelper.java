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

package com.shadow.study.plugin;

import static com.tencent.shadow.sample.constant.Constant.PLUGIN_MANAGER_APK_NAME;
import static com.tencent.shadow.sample.constant.Constant.SD_DIRECTORY_NAME;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.shadow.study.BuildConfig;
import com.tencent.shadow.core.common.LoggerFactory;
import com.tencent.shadow.sample.constant.Constant;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PluginHelper {

    private static PluginHelper sInstance = new PluginHelper();
    private Context mContext;
    public ExecutorService singlePool = Executors.newSingleThreadExecutor();

    /**
     * 插件管理apk复制到APK包中之后的文件，绝对路径示例：/data/user/0/com.tencent.shadow.sample.host/files/pluginmanager.apk
     */
    public File pluginManagerDestinationFile;

    /**
     * 动态加载的插件包名，里面包含：插件apk、插件框架apk（loader apk和runtime apk）、apk信息配置关系json文件。
     * 完整名字示例：plugin1-debug.zip、plugin2-debug.zip
     */
    public String pluginZipName;

    /**
     * SD卡中的插件的路径，只用于从SD卡中复制插件管理apk和插件zip包的情况
     */
    private String pluginSDSourcePath;

    /**
     * 插件包复制到APK包中之后的文件，绝对路径示例：/data/user/0/com.tencent.shadow.sample.host/files/plugin-debug.zip
     */
    public File pluginZipDestinationFile;

    public static PluginHelper getInstance() {
        return sInstance;
    }

    private PluginHelper() {
    }

    /**
     * 初始化
     */
    public void init(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * 把插件管理apk复制到 /data/user/0/APP包名/files/ 目录中
     */
    public void installPluginManager() {

        singlePool.execute(() -> {
            // 创建插件管理apk包被复制后的文件
            pluginManagerDestinationFile = new File(mContext.getFilesDir(), PLUGIN_MANAGER_APK_NAME);

            // 从 build的assets 中把 pluginmanager.apk 复制到 /data/user/0/APP包名/files/ 目录中
            copyPluginManagerFromAssets();

            // 从SD卡中把 pluginmanager.apk 复制到 /data/user/0/APP包名/files/ 目录中
//            copyPluginManagerFromSD();
        });
    }

    /**
     * 把插件Zip包复制到  /data/user/0/APP包名/files/  目录中
     *
     * @param number 插件序号
     */
    public void installPlugin(String number) {

        // 注意：同时安装多个插件时，必须开启多个线程，否则任务会被覆盖掉，导致只安装最后的那个插件。
        singlePool.execute(() -> {
            // 复制插件前的准备工作
            copyPluginZipPrepare(number);

            // 从 assets 中复制插件
            copyPluginZipFromAssets();

            // 从SD卡中复制插件
//            copyPluginZipFromSD();
        });
    }

    /**
     * 复制插件前的准备工作：创建插件zip包被复制后的文件
     *
     * @param number 插件序号
     */
    private void copyPluginZipPrepare(String number) {
        if (mContext == null) {
            return;
        }

        // 拼接要复制的插件zip文件名，完整名字示例：plugin1-debug.zip、plugin2-debug.zip
        pluginZipName = Constant.PLUGIN_ZIP_PREFIX + number;
        if (BuildConfig.DEBUG) {
            pluginZipName += Constant.PLUGIN_ZIP_DEBUG_SUFFIX;
        } else {
            pluginZipName += Constant.PLUGIN_ZIP_RELEASE_SUFFIX;
        }

        // 创建插件zip包被复制后的文件
        pluginZipDestinationFile = new File(mContext.getFilesDir(), pluginZipName);
    }

    /**
     * 从 build的assets 中把 pluginmanager.apk 复制到 /data/user/0/APP包名/files/ 目录中
     */
    private void copyPluginManagerFromAssets() {
        try {
            LoggerFactory.getLogger(PluginHelper.class).info("copyPluginManagerFromAssets() ==> 插件路径：" + mContext.getAssets()
                    + "\npluginManagerDestinationFile.getAbsolutePath()=" + pluginManagerDestinationFile.getAbsolutePath());

            InputStream is = mContext.getAssets().open(PLUGIN_MANAGER_APK_NAME);
            FileUtils.copyInputStreamToFile(is, pluginManagerDestinationFile);
        } catch (IOException e) {
            throw new RuntimeException("从assets中复制apk出错", e);
        }
    }

    /**
     * 从 SD卡的 ShadowStudy 目录中把 pluginmanager.apk 复制到 /data/user/0/APP包名/files/ 目录中。
     * 热更新时，可以把 pluginmanager.apk 下载到SD卡中，然后从SD卡中复制到对应目录，再加载。
     */
    private void copyPluginManagerFromSD() {
        try {
            if (TextUtils.isEmpty(pluginSDSourcePath)) {
                String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                pluginSDSourcePath = sdPath + SD_DIRECTORY_NAME;
            }

            // 创建路径
            File pluginSourceFile = new File(pluginSDSourcePath);
            if (!pluginSourceFile.exists()) {
                pluginSourceFile.mkdirs();
            }

            // 复制 pluginmanager.apk
            File pluginManagerSource = new File(pluginSDSourcePath, PLUGIN_MANAGER_APK_NAME);
            if (pluginManagerSource.exists()) {
                InputStream pluginManagerIS = new FileInputStream(pluginManagerSource);
                FileUtils.copyInputStreamToFile(pluginManagerIS, pluginManagerDestinationFile);
            }

            LoggerFactory.getLogger(PluginHelper.class).info("copyPluginManagerFromSD() ==> 插件路径：pluginManagerSource.getAbsolutePath()=" + pluginManagerSource.getAbsolutePath()
                    + "\npluginManagerSource.exists()=" + pluginManagerSource.exists()
                    + "\npluginManagerDestinationFile.getAbsolutePath()=" + pluginManagerDestinationFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("从SD卡中复制apk出错", e);
        }
    }

    /**
     * 把 build的assets 中的plugin-debug.zip 复制到 /data/user/0/APP包名/files/ 目录中
     */
    private void copyPluginZipFromAssets() {
        try {
            LoggerFactory.getLogger(PluginHelper.class).info("copyPluginZipFromAssets() ==> 插件路径：" + mContext.getAssets()
                    + "\npluginZipDestinationFile.getAbsolutePath()=" + pluginZipDestinationFile.getAbsolutePath());

            InputStream zip = mContext.getAssets().open(pluginZipName);
            FileUtils.copyInputStreamToFile(zip, pluginZipDestinationFile);
        } catch (IOException e) {
            throw new RuntimeException("从assets中复制apk出错", e);
        }
    }

    /**
     * 把 SD卡中 ShadowStudy 目录中的 plugin-debug.zip 复制到 /data/user/0/APP包名/files/ 目录中
     * 热更新时，可以把插件下载到SD卡中，然后从SD卡中复制到对应目录，再加载。
     */
    private void copyPluginZipFromSD() {
        try {
            if (TextUtils.isEmpty(pluginSDSourcePath)) {
                String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                pluginSDSourcePath = sdPath + SD_DIRECTORY_NAME;
            }

            // 创建路径
            File pluginSourceFile = new File(pluginSDSourcePath);
            if (!pluginSourceFile.exists()) {
                pluginSourceFile.mkdirs();
            }

            // 复制插件zip包
            File pluginZipSource = new File(pluginSDSourcePath, pluginZipName);
            if (pluginZipSource.exists()) {
                InputStream pluginZipIS = new FileInputStream(pluginZipSource);
                FileUtils.copyInputStreamToFile(pluginZipIS, pluginZipDestinationFile);
            }

            LoggerFactory.getLogger(PluginHelper.class).info("copyPluginZipFromSD() ==> 插件路径：pluginZipSource.getAbsolutePath()=" + pluginZipSource.getAbsolutePath()
                    + "\npluginZipSource.exists()=" + pluginZipSource.exists()
                    + "\npluginZipDestinationFile.getAbsolutePath()=" + pluginZipDestinationFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("从SD卡中复制apk出错", e);
        }
    }

}
