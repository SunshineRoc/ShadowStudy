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

package com.tencent.shadow.sample.constant;

final public class Constant {
    public static final String KEY_PLUGIN_ZIP_PATH = "pluginZipPath";
    public static final String KEY_ACTIVITY_CLASSNAME = "KEY_ACTIVITY_CLASSNAME";
    public static final String KEY_EXTRAS = "KEY_EXTRAS";
    public static final String KEY_PLUGIN_PART_KEY = "KEY_PLUGIN_PART_KEY";
    public static final String PART_KEY_PLUGIN_MAIN_APP = "sample-plugin-app";

    public static final String PART_KEY_PLUGIN_APP_ONE = "sample-plugin-app1"; // 插件1的partKey，必须与build.gradle中插件1的partKey一致
    public static final String PART_KEY_PLUGIN_APP_TWO = "sample-plugin-app2"; // 插件2的partKey，必须与build.gradle中插件2的partKey一致
    public static final String PART_KEY_PLUGIN_APP_THREE = "sample-plugin-app3"; // 插件3的partKey，必须与build.gradle中插件3的partKey一致

    public static final String SD_DIRECTORY_NAME = "/ShadowStudy"; // 当前APP在SD卡中保存插件的目录名
    public static final String PLUGIN_MANAGER_APK_NAME = "plugin-manager.apk"; // 动态加载的插件管理apk文件名

    public static final String PLUGIN_ZIP_PREFIX = "plugin"; // 插件包的前缀
    public static final String PLUGIN_ZIP_DEBUG_SUFFIX = "-debug.zip"; // 插件包后缀：debug模式
    public static final String PLUGIN_ZIP_RELEASE_SUFFIX = "-release.zip"; // 插件包后缀：release模式

    public static final int FROM_ID_NOOP = 1000;
    public static final int FROM_ID_START_ACTIVITY = 1002;
    public static final int FROM_ID_CLOSE = 1003;
    public static final int FROM_ID_LOAD_VIEW_TO_HOST = 1004;
}
