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

package com.tencent.shadow.core.loader.delegates

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import com.tencent.shadow.coding.java_build_config.BuildConfig
import com.tencent.shadow.core.common.LoggerFactory
import com.tencent.shadow.core.loader.managers.ComponentManager.Companion.CM_ACTIVITY_INFO_KEY
import com.tencent.shadow.core.loader.managers.ComponentManager.Companion.CM_BUSINESS_NAME_KEY
import com.tencent.shadow.core.loader.managers.ComponentManager.Companion.CM_CALLING_ACTIVITY_KEY
import com.tencent.shadow.core.loader.managers.ComponentManager.Companion.CM_CLASS_NAME_KEY
import com.tencent.shadow.core.loader.managers.ComponentManager.Companion.CM_EXTRAS_BUNDLE_KEY
import com.tencent.shadow.core.loader.managers.ComponentManager.Companion.CM_LOADER_BUNDLE_KEY
import com.tencent.shadow.core.loader.managers.ComponentManager.Companion.CM_PART_KEY
import com.tencent.shadow.core.runtime.PluginActivity
import com.tencent.shadow.core.runtime.PluginManifest
import com.tencent.shadow.core.runtime.ShadowActivity
import com.tencent.shadow.core.runtime.container.HostActivityDelegate
import com.tencent.shadow.core.runtime.container.HostActivityDelegator

/**
 * ??????Activity?????????Activity????????????????????????
 * ??????????????????????????????????????????????????????.?????????????????????????????????????????????.
 *
 * @author cubershi
 */
open class ShadowActivityDelegate(private val mDI: DI) : GeneratedShadowActivityDelegate(),
        HostActivityDelegate {
    companion object {
        const val PLUGIN_OUT_STATE_KEY = "PLUGIN_OUT_STATE_KEY"
        val mLogger = LoggerFactory.getLogger(ShadowActivityDelegate::class.java)
    }

    protected lateinit var mHostActivityDelegator: HostActivityDelegator
    private val mPluginActivity get() = super.pluginActivity
    private lateinit var mBusinessName: String
    private lateinit var mPartKey: String
    private lateinit var mBundleForPluginLoader: Bundle
    private var mRawIntentExtraBundle: Bundle? = null
    private var mPluginActivityCreated = false
    private var mDependenciesInjected = false
    private var mRecreateCalled = false

    /**
     * ?????????????????????OnWindowAttributesChanged????????????????????????????????????onCreate????????????
     */
    private var mCallOnWindowAttributesChanged = false
    private var mBeforeOnCreateOnWindowAttributesChangedCalledParams: WindowManager.LayoutParams? =
            null

    override fun setDelegator(hostActivityDelegator: HostActivityDelegator) {
        mHostActivityDelegator = hostActivityDelegator
    }

    override fun getPluginActivity(): Any = mPluginActivity

    private lateinit var mCurrentConfiguration: Configuration
    private var mPluginHandleConfigurationChange: Int = 0
    private var mCallingActivity: ComponentName? = null
    protected lateinit var mPluginActivityInfo: PluginManifest.ActivityInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        val pluginInitBundle = savedInstanceState ?: mHostActivityDelegator.intent.extras!!

        mCallingActivity = pluginInitBundle.getParcelable(CM_CALLING_ACTIVITY_KEY)
        mBusinessName = pluginInitBundle.getString(CM_BUSINESS_NAME_KEY, "")
        val partKey = pluginInitBundle.getString(CM_PART_KEY)!!
        mPartKey = partKey
        mDI.inject(this, partKey)
        mDependenciesInjected = true

        val bundleForPluginLoader = pluginInitBundle.getBundle(CM_LOADER_BUNDLE_KEY)!!
        mBundleForPluginLoader = bundleForPluginLoader
        bundleForPluginLoader.classLoader = this.javaClass.classLoader
        val pluginActivityClassName = bundleForPluginLoader.getString(CM_CLASS_NAME_KEY)!!
        val pluginActivityInfo: PluginManifest.ActivityInfo =
                bundleForPluginLoader.getParcelable(CM_ACTIVITY_INFO_KEY)!!
        mPluginActivityInfo = pluginActivityInfo

        mCurrentConfiguration = Configuration(resources.configuration)
        mPluginHandleConfigurationChange =
                (pluginActivityInfo.configChanges
                        or ActivityInfo.CONFIG_SCREEN_SIZE//?????????????????????????????????????????????????????????????????????Activity???
                        or ActivityInfo.CONFIG_SMALLEST_SCREEN_SIZE//?????????????????????????????????????????????????????????????????????Activity???
                        or 0x20000000 //???ActivityInfo.CONFIG_WINDOW_CONFIGURATION ??????????????????
                        )
        if (savedInstanceState == null) {
            mRawIntentExtraBundle = pluginInitBundle.getBundle(CM_EXTRAS_BUNDLE_KEY)
            mHostActivityDelegator.intent.replaceExtras(mRawIntentExtraBundle)
        }
        mHostActivityDelegator.intent.setExtrasClassLoader(mPluginClassLoader)

        try {
            val pluginActivity = mAppComponentFactory.instantiateActivity(
                    mPluginClassLoader,
                    pluginActivityClassName,
                    mHostActivityDelegator.intent
            )
            initPluginActivity(pluginActivity, pluginActivityInfo)
            super.pluginActivity = pluginActivity

            if (mLogger.isDebugEnabled) {
                mLogger.debug(
                        "{} mPluginHandleConfigurationChange=={}",
                        mPluginActivity.javaClass.canonicalName,
                        mPluginHandleConfigurationChange
                )
            }

            //???PluginActivity??????ContainerActivity??????Window???Callback
            mHostActivityDelegator.window.callback = pluginActivity

            //????????????AndroidManifest.xml ????????????WindowSoftInputMode
            mHostActivityDelegator.window.setSoftInputMode(pluginActivityInfo.softInputMode)

            //Activity.onCreate???????????????????????????onWindowAttributesChanged???
            if (mCallOnWindowAttributesChanged) {
                pluginActivity.onWindowAttributesChanged(
                        mBeforeOnCreateOnWindowAttributesChangedCalledParams
                )
                mBeforeOnCreateOnWindowAttributesChangedCalledParams = null
            }

            val pluginSavedInstanceState: Bundle? =
                    savedInstanceState?.getBundle(PLUGIN_OUT_STATE_KEY)
            pluginSavedInstanceState?.classLoader = mPluginClassLoader
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                notifyPluginActivityPreCreated(pluginActivity, pluginSavedInstanceState)
            }
            pluginActivity.onCreate(pluginSavedInstanceState)
            mPluginActivityCreated = true
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun initPluginActivity(
            pluginActivity: PluginActivity,
            pluginActivityInfo: PluginManifest.ActivityInfo
    ) {
        pluginActivity.setHostActivityDelegator(mHostActivityDelegator)
        pluginActivity.setPluginResources(mPluginResources)
        pluginActivity.setPluginClassLoader(mPluginClassLoader)
        pluginActivity.setPluginComponentLauncher(mComponentManager)
        pluginActivity.setPluginApplication(mPluginApplication)
        pluginActivity.setShadowApplication(mPluginApplication)
        pluginActivity.applicationInfo = mPluginApplication.applicationInfo
        pluginActivity.setBusinessName(mBusinessName)
        pluginActivity.callingActivity = mCallingActivity
        pluginActivity.setPluginPartKey(mPartKey)

        //???????????????set????????????PluginActivity??????????????????
        //?????????Activity???????????????????????????????????????????????????????????????Activity??????????????????
        //????????????setHostContextAsBase???????????????Activity???attachBaseContext?????????
        //????????????????????????Activity??????????????????
        //???????????????????????????????????????
        pluginActivity.setHostContextAsBase(mHostActivityDelegator.hostActivity as Context)

        val activityTheme = if (pluginActivityInfo.theme != 0) {
            pluginActivityInfo.theme
        } else {
            pluginActivity.applicationInfo.theme
        }
        pluginActivity.setTheme(activityTheme)
    }

    override fun getLoaderVersion() = BuildConfig.VERSION_NAME

    override fun onNewIntent(intent: Intent) {
        val pluginExtras: Bundle? = intent.getBundleExtra(CM_EXTRAS_BUNDLE_KEY)
        intent.replaceExtras(pluginExtras)
        mPluginActivity.onNewIntent(intent)
    }

    override fun onNavigateUpFromChild(arg0: Activity?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val pluginOutState = Bundle(mPluginClassLoader)
        mPluginActivity.onSaveInstanceState(pluginOutState)
        outState.putBundle(PLUGIN_OUT_STATE_KEY, pluginOutState)
        outState.putString(CM_PART_KEY, mPartKey)
        outState.putBundle(CM_LOADER_BUNDLE_KEY, mBundleForPluginLoader)
        if (mRecreateCalled) {
            outState.putBundle(CM_EXTRAS_BUNDLE_KEY, mHostActivityDelegator.intent.extras)
        } else {
            outState.putBundle(CM_EXTRAS_BUNDLE_KEY, mRawIntentExtraBundle)
        }
    }

    override fun onChildTitleChanged(arg0: Activity?, arg1: CharSequence?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        val diff = newConfig.diff(mCurrentConfiguration)
        if (mLogger.isDebugEnabled) {
            mLogger.debug(
                    "{} onConfigurationChanged diff=={}",
                    mPluginActivity.javaClass.canonicalName,
                    diff
            )
        }
        if (diff == (diff and mPluginHandleConfigurationChange)) {
            mPluginActivity.onConfigurationChanged(newConfig)
            mCurrentConfiguration = Configuration(newConfig)
        } else {
            mHostActivityDelegator.superOnConfigurationChanged(newConfig)
            mHostActivityDelegator.recreate()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        val pluginSavedInstanceState: Bundle? = savedInstanceState?.getBundle(PLUGIN_OUT_STATE_KEY)
        mPluginActivity.onRestoreInstanceState(pluginSavedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        val pluginSavedInstanceState: Bundle? = savedInstanceState?.getBundle(PLUGIN_OUT_STATE_KEY)
        mPluginActivity.onPostCreate(pluginSavedInstanceState)
    }

    override fun onWindowAttributesChanged(params: WindowManager.LayoutParams) {
        if (mPluginActivityCreated) {
            mPluginActivity.onWindowAttributesChanged(params)
        } else {
            mBeforeOnCreateOnWindowAttributesChangedCalledParams = params
        }
        mCallOnWindowAttributesChanged = true
    }

    override fun onApplyThemeResource(theme: Resources.Theme, resid: Int, first: Boolean) {
        mHostActivityDelegator.superOnApplyThemeResource(theme, resid, first)
        if (mPluginActivityCreated) {
            mPluginActivity.onApplyThemeResource(theme, resid, first)
        }
    }

    override fun getClassLoader(): ClassLoader {
        return mPluginClassLoader
    }

    override fun getLayoutInflater(): LayoutInflater = LayoutInflater.from(mPluginActivity)

    override fun getResources(): Resources {
        if (mDependenciesInjected) {
            return mPluginResources;
        } else {
            //????????????android.view.Window.getDefaultFeatures??????????????????????????????????????????????????????????????????
            //???getDefaultFeatures???????????????????????????
            return Resources.getSystem()
        }
    }

    override fun recreate() {
        mRecreateCalled = true
        mHostActivityDelegator.superRecreate()
    }

    private fun notifyPluginActivityPreCreated(
            pluginActivity: ShadowActivity,
            pluginSavedInstanceState: Bundle?
    ) {
        mPluginApplication.mActivityLifecycleCallbacksHolder.notifyPluginActivityPreCreated(
                pluginActivity,
                pluginSavedInstanceState
        )
    }
}
