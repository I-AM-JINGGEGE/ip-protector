package com.vpn.android.server;

import com.vpn.android.server.interceptor.NetExceptionHandlerInterceptor;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;

public class ServerManager {
    private static final String TAG = ServerManager.class.getSimpleName();

    private static ServerManager sServerManager = null;

    private OkHttpClient mOkHttpClient;

    public static synchronized ServerManager getInstance() {
        if (sServerManager == null) {
            sServerManager = new ServerManager();
        }
        return sServerManager;
    }

    private ServerManager() {
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30L * 1000L, TimeUnit.MILLISECONDS)
                .readTimeout(30L * 1000L, TimeUnit.MILLISECONDS)
                .writeTimeout(30L * 1000L, TimeUnit.MILLISECONDS)
                .addNetworkInterceptor(new NetExceptionHandlerInterceptor())
                .build();
    }

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }
}
