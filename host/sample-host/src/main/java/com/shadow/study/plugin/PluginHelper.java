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

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.shadow.study.BuildConfig;
import com.tencent.shadow.core.common.LoggerFactory;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PluginHelper {

    /**
     * 动态加载的插件管理apk
     */
    public final static String sPluginManagerName = "pluginmanager.apk";

    /**
     * 动态加载的插件包，里面包含以下几个部分，插件apk，插件框架apk（loader apk和runtime apk）, apk信息配置关系json文件
     */
    public final static String sPluginZip = BuildConfig.DEBUG ? "plugin-debug.zip" : "plugin-release.zip";

    private String pluginSourcePath; // 插件的路径

    public File pluginManagerFile;

    public File pluginZipFile;

    public ExecutorService singlePool = Executors.newSingleThreadExecutor();

    private Context mContext;

    private static PluginHelper sInstance = new PluginHelper();

    public static PluginHelper getInstance() {
        return sInstance;
    }

    private PluginHelper() {
    }

    public void init(Context context) {
        pluginManagerFile = new File(context.getFilesDir(), sPluginManagerName);
        pluginZipFile = new File(context.getFilesDir(), sPluginZip);

        mContext = context.getApplicationContext();

        singlePool.execute(() -> {

            // 从 assets 中复制插件
            preparePlugin();

            // 从SD卡中复制插件
//            copyPlugin();
        });
    }

    /**
     * 把 assets 中的 pluginmanager.apk 和 plugin-debug.zip 复制到 sPluginManagerName 和 pluginZipFile 中
     */
    private void preparePlugin() {
        try {
            InputStream is = mContext.getAssets().open(sPluginManagerName);
            FileUtils.copyInputStreamToFile(is, pluginManagerFile);

            InputStream zip = mContext.getAssets().open(sPluginZip);
            FileUtils.copyInputStreamToFile(zip, pluginZipFile);

            LoggerFactory.getLogger(PluginHelper.class).info("preparePlugin() ==> 插件路径：" + mContext.getAssets()
                    + "\npluginManagerFile.getAbsolutePath()=" + pluginManagerFile.getAbsolutePath()
                    + "\npluginZipFile.getAbsolutePath()=" + pluginZipFile.getAbsolutePath());

        } catch (IOException e) {
            throw new RuntimeException("从assets中复制apk出错", e);
        }
    }

    /**
     * 把 SD卡中 ShadowStudy 目录中的 pluginmanager.apk 和 plugin-debug.zip 复制到 sPluginManagerName 和 pluginZipFile 中。
     * 热更新时，可以把插件下载到SD卡中，然后从SD卡中复制到对应目录，再加载。
     */
    private void copyPlugin() {
        try {
            if (TextUtils.isEmpty(pluginSourcePath)) {
                String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                pluginSourcePath = sdPath + "/ShadowStudy";
            }

            // 创建路径
            File pluginSourceFile = new File(pluginSourcePath);
            if (!pluginSourceFile.exists()) {
                pluginSourceFile.mkdirs();
            }

            // 复制 pluginmanager.apk
            File pluginManagerSource = new File(pluginSourcePath, sPluginManagerName);
            if (pluginManagerSource.exists()) {
                InputStream pluginManagerIS = new FileInputStream(pluginManagerSource);
                FileUtils.copyInputStreamToFile(pluginManagerIS, pluginManagerFile);
            }

            LoggerFactory.getLogger(PluginHelper.class).info("copyPlugin() ==> 插件路径：pluginManagerSource.getAbsolutePath()=" + pluginManagerSource.getAbsolutePath()
                    + "\npluginManagerSource.exists()=" + pluginManagerSource.exists()
                    + "\npluginManagerFile.getAbsolutePath()=" + pluginManagerFile.getAbsolutePath());

            // 复制 plugin-debug.zip 或 plugin-release.zip
            File pluginZipSource = new File(pluginSourcePath, sPluginZip);
            if (pluginZipSource.exists()) {
                InputStream pluginZipIS = new FileInputStream(pluginZipSource);
                FileUtils.copyInputStreamToFile(pluginZipIS, pluginZipFile);
            }

            LoggerFactory.getLogger(PluginHelper.class).info("copyPlugin() ==> 插件路径：pluginZipSource.getAbsolutePath()=" + pluginZipSource.getAbsolutePath()
                    + "\npluginZipSource.exists()=" + pluginZipSource.exists()
                    + "\npluginZipFile.getAbsolutePath()=" + pluginZipFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("从SD卡中复制apk出错", e);
        }
    }

}
