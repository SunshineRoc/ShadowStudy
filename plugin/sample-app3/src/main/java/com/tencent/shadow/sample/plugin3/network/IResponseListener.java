package com.tencent.shadow.sample.plugin3.network;

/**
 * 网络请求的响应结果
 */
public interface IResponseListener {

    /**
     * 请求成功时的回调
     *
     * @param response 接口响应结果
     */
    void onSuccess(String response);

    /**
     * 请求失败时的回调
     *
     * @param message 接口响应结果
     */
    void onFailed(String message);
}
