package com.tencent.shadow.sample.plugin3.network;

import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpManager {

    public static final String NETWORK_URL = "https://www.baidu.com/";
    private static final String TAG = OkHttpManager.class.getSimpleName();

    private Call call;
    private static OkHttpManager okHttpManager;

    public static OkHttpManager getInstance() {
        if (okHttpManager == null) {
            synchronized (OkHttpManager.class) {
                if (okHttpManager == null) {
                    okHttpManager = new OkHttpManager();
                }
            }
        }

        return okHttpManager;
    }

    private OkHttpManager() {
    }

    /**
     * OkHttp请求接口
     *
     * @param url      接口链接
     * @param listener 接口请求结果回调
     */
    public void requestGet(String url, IResponseListener listener) {
        if (call == null) {
            OkHttpClient okHttpClient = new OkHttpClient();
            Request.Builder builder = new Request.Builder();
            Request request = builder
                    .get()
                    .url(url)
                    .build();
            call = okHttpClient.newCall(request);
        }

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure()，网络请求失败，e=" + e.getMessage());
                e.printStackTrace();

                if (listener != null) {
                    listener.onFailed(e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                Log.i(TAG, "onResponse()，网络请求成功，response=" + response);

                if (listener != null) {
                    listener.onSuccess(response.toString());
                }
            }
        });
    }
}
