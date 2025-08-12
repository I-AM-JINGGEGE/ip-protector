package com.vpn.android.comboads.network;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author: xiaosailing
 * date: 2022-03-24
 * description: 网络加载失败重试拦截器
 * version：1.0
 */
public class HttpClientRetryInterceptor implements Interceptor {
    private int MAX_RETRY_TIMES = 3;
    private int retryNum = 0;

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Response response = null;
        try {
            Request request = chain.request();
            response = chain.proceed(request);
            while (!response.isSuccessful() && retryNum < MAX_RETRY_TIMES) {
                retryNum++;
                response.close();
                response = chain.proceed(request);
            }
            return response;
        } catch (IOException e) {
            throw e;
        }
    }
}
