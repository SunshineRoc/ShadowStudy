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

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;

import com.shadow.study.HostApplication;
import com.shadow.study.R;
import com.tencent.shadow.dynamic.host.EnterCallback;
import com.tencent.shadow.sample.constant.Constant;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 加载插件的容器，或者说跳板，通过该页面，打开插件
 */
public class PluginLoadActivity extends Activity {

    private ViewGroup mViewGroup;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);

        mViewGroup = findViewById(R.id.container);

        startPlugin();
    }

    /**
     * 启动插件
     */
    public void startPlugin() {

        PluginHelper.getInstance().singlePool.execute(() -> {

            // 根据插件apk包，创建PluginManager
            HostApplication.getApp().loadPluginManager(PluginHelper.getInstance().pluginManagerFile);

            Bundle bundle = new Bundle();
            // 插件路径
            bundle.putString(Constant.KEY_PLUGIN_ZIP_PATH, PluginHelper.getInstance().pluginZipFile.getAbsolutePath());
            // 插件Key
            bundle.putString(Constant.KEY_PLUGIN_PART_KEY, getIntent().getStringExtra(Constant.KEY_PLUGIN_PART_KEY));
            // 宿主加载插件时显示的启动页，该启动页显示在插件进程中
            bundle.putString(Constant.KEY_ACTIVITY_CLASSNAME, getIntent().getStringExtra(Constant.KEY_ACTIVITY_CLASSNAME));

            Logger.getLogger(PluginLoadActivity.class.getSimpleName()).log(Level.INFO, "准备打开插件");

            // 进入插件
            HostApplication.getApp().getPluginManager()
                    .enter(PluginLoadActivity.this, Constant.FROM_ID_START_ACTIVITY, bundle, new EnterCallback() {
                        @Override
                        public void onShowLoadingView(final View view) {
                            // 宿主加载插件过程中的过渡UI
//                            mHandler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    mViewGroup.addView(view);
//                                }
//                            });
                        }

                        @Override
                        public void onCloseLoadingView() {
                            finish();
                        }

                        @Override
                        public void onEnterComplete() {

                        }
                    });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HostApplication.getApp().getPluginManager().enter(this, Constant.FROM_ID_CLOSE, null, null);
        mViewGroup.removeAllViews();
    }
}
