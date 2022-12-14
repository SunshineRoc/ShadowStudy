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

package com.tencent.shadow.dynamic.host;

import static com.tencent.shadow.dynamic.host.FailedException.ERROR_CODE_FILE_NOT_FOUND_EXCEPTION;
import static com.tencent.shadow.dynamic.host.FailedException.ERROR_CODE_RELOAD_LOADER_EXCEPTION;
import static com.tencent.shadow.dynamic.host.FailedException.ERROR_CODE_RELOAD_RUNTIME_EXCEPTION;
import static com.tencent.shadow.dynamic.host.FailedException.ERROR_CODE_RESET_UUID_EXCEPTION;
import static com.tencent.shadow.dynamic.host.FailedException.ERROR_CODE_RUNTIME_EXCEPTION;
import static com.tencent.shadow.dynamic.host.FailedException.ERROR_CODE_UUID_MANAGER_DEAD_EXCEPTION;
import static com.tencent.shadow.dynamic.host.FailedException.ERROR_CODE_UUID_MANAGER_NULL_EXCEPTION;

import android.app.Application;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.tencent.shadow.core.common.InstalledApk;
import com.tencent.shadow.core.common.LoggerFactory;

import java.io.File;


public class PluginProcessService extends BasePluginProcessService {

    private final PpsBinder mPpsControllerBinder = new PpsBinder(this);

    static final ActivityHolder sActivityHolder = new ActivityHolder();

    public static Application.ActivityLifecycleCallbacks getActivityHolder() {
        return sActivityHolder;
    }

    public static PpsController wrapBinder(IBinder ppsBinder) {
        return new PpsController(ppsBinder);
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (mLogger.isInfoEnabled()) {
            mLogger.info("onBind:" + this);
        }
        return mPpsControllerBinder;
    }

    private UuidManager mUuidManager;

    private PluginLoaderImpl mPluginLoader;

    private boolean mRuntimeLoaded = false;

    /**
     * ?????????Uuid??????????????????????????????
     */
    private String mUuid = "";

    private void setUuid(String uuid) throws FailedException {
        if (mUuid.isEmpty()) {
            mUuid = uuid;
        } else if (!mUuid.equals(uuid)) {
            throw new FailedException(ERROR_CODE_RESET_UUID_EXCEPTION, "????????????uuid==" + mUuid + "????????????uuid==" + uuid);
        }
    }

    private void checkUuidManagerNotNull() throws FailedException {
        if (mUuidManager == null) {
            throw new FailedException(ERROR_CODE_UUID_MANAGER_NULL_EXCEPTION, "mUuidManager == null");
        }
    }

    void loadRuntime(String uuid) throws FailedException {

        checkUuidManagerNotNull();
        setUuid(uuid);
        if (mRuntimeLoaded) {
            throw new FailedException(ERROR_CODE_RELOAD_RUNTIME_EXCEPTION
                    , "????????????loadRuntime");
        }
        try {
            if (mLogger.isInfoEnabled()) {
                mLogger.info("loadRuntime uuid:" + uuid);
            }
            InstalledApk installedApk;
            try {
                // ?????????????????? runtime ???apk?????????
                installedApk = mUuidManager.getRuntime(uuid);
            } catch (RemoteException e) {
                throw new FailedException(ERROR_CODE_UUID_MANAGER_DEAD_EXCEPTION, e.getMessage());
            } catch (NotFoundException e) {
                throw new FailedException(ERROR_CODE_FILE_NOT_FOUND_EXCEPTION, "uuid==" + uuid + "???Runtime???????????????cause:" + e.getMessage());
            }

            LoggerFactory.getLogger(PluginProcessService.class).info("loadRuntime() ==> ??????runtime???apkFilePath=" + installedApk.apkFilePath
                    + "???oDexPath=" + installedApk.oDexPath + "???libraryPath=" + installedApk.libraryPath);

            // apkFilePath=/data/user/0/com.tencent.shadow.sample.host/files/ShadowPluginManager/UnpackedPlugin/test-dynamic-manager/b8dc3a6268ed0537653504bcc71152c7/plugin1-debug.zip/sample-runtime-debug.apk???
            // oDexPath=/data/user/0/com.tencent.shadow.sample.host/files/ShadowPluginManager/UnpackedPlugin/test-dynamic-manager/oDex/AC104F02-AF26-4530-AE52-D0D3F97DF1A8_odex???
            // libraryPath=null
            InstalledApk installedRuntimeApk = new InstalledApk(installedApk.apkFilePath, installedApk.oDexPath, installedApk.libraryPath);

            // ?????????2????????????????????? runtime ???apk??????????????????runtime
            boolean loaded = DynamicRuntime.loadRuntime(installedRuntimeApk);
            if (loaded) {
                // ?????????3?????????????????????runtime?????????SharedPreferences???
                DynamicRuntime.saveLastRuntimeInfo(this, installedRuntimeApk);
            }
            mRuntimeLoaded = true;
        } catch (RuntimeException e) {
            if (mLogger.isErrorEnabled()) {
                mLogger.error("loadRuntime??????RuntimeException", e);
            }
            throw new FailedException(e);
        }
    }

    /**
     * ??????loader???????????????
     * 1?????????uuid?????????????????? loader ???apk?????????
     * 2?????????????????????loader???apk??????????????????loader???????????????PluginLoaderBinder?????????PluginLoaderBinder?????????Binder?????????????????????????????????
     */
    void loadPluginLoader(String uuid) throws FailedException {
        if (mLogger.isInfoEnabled()) {
            mLogger.info("loadPluginLoader uuid:" + uuid + " mPluginLoader:" + mPluginLoader);
        }
        checkUuidManagerNotNull();
        setUuid(uuid);
        if (mPluginLoader != null) {
            throw new FailedException(ERROR_CODE_RELOAD_LOADER_EXCEPTION
                    , "????????????loadPluginLoader");
        }
        try {
            InstalledApk installedApk;
            try {
                // ?????????1????????????????????? loader ???apk?????????
                installedApk = mUuidManager.getPluginLoader(uuid);
                if (mLogger.isInfoEnabled()) {
                    mLogger.info("??????uuid==" + uuid + "???Loader apk:" + installedApk.apkFilePath);
                }
            } catch (RemoteException e) {
                if (mLogger.isErrorEnabled()) {
                    mLogger.error("??????Loader Apk??????", e);
                }
                throw new FailedException(ERROR_CODE_UUID_MANAGER_DEAD_EXCEPTION, e.getMessage());
            } catch (NotFoundException e) {
                throw new FailedException(ERROR_CODE_FILE_NOT_FOUND_EXCEPTION, "uuid==" + uuid + "???PluginLoader???????????????cause:" + e.getMessage());
            }
            File file = new File(installedApk.apkFilePath);
            if (!file.exists()) {
                throw new FailedException(ERROR_CODE_FILE_NOT_FOUND_EXCEPTION, file.getAbsolutePath() + "???????????????");
            }

            LoggerFactory.getLogger(PluginProcessService.class).info("loadPluginLoader() ==> ???????????????uuid=" + uuid + "???getApplicationContext()=" + getApplicationContext());

            // ?????????2?????????????????????loader???apk??????????????????loader???????????????PluginLoaderBinder??????
            PluginLoaderImpl pluginLoader = new LoaderImplLoader().load(installedApk, uuid, getApplicationContext());
            pluginLoader.setUuidManager(mUuidManager);
            mPluginLoader = pluginLoader;
        } catch (RuntimeException e) {
            if (mLogger.isErrorEnabled()) {
                mLogger.error("loadPluginLoader??????RuntimeException", e);
            }
            throw new FailedException(e);
        } catch (FailedException e) {
            throw e;
        } catch (Exception e) {
            if (mLogger.isErrorEnabled()) {
                mLogger.error("loadPluginLoader??????Exception", e);
            }
            String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            throw new FailedException(ERROR_CODE_RUNTIME_EXCEPTION, "???????????????????????? cause???" + msg);
        }
    }

    void setUuidManager(UuidManager uuidManager) {
        if (mLogger.isInfoEnabled()) {
            mLogger.info("setUuidManager uuidManager==" + uuidManager);
        }
        mUuidManager = uuidManager;
        if (mPluginLoader != null) {
            if (mLogger.isInfoEnabled()) {
                mLogger.info("??????mPluginLoader???uuidManager");
            }
            mPluginLoader.setUuidManager(uuidManager);
        }
    }

    void exit() {
        if (mLogger.isInfoEnabled()) {
            mLogger.info("exit ");
        }
        PluginProcessService.sActivityHolder.finishAll();
        System.exit(0);
        try {
            wait();
        } catch (InterruptedException ignored) {
        }
    }

    PpsStatus getPpsStatus() {
        return new PpsStatus(mUuid, mRuntimeLoaded, mPluginLoader != null, mUuidManager != null);
    }

    IBinder getPluginLoader() {
        return mPluginLoader;
    }
}
