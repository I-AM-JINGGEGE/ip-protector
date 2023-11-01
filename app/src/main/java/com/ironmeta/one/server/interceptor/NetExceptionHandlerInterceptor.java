package com.ironmeta.one.server.interceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class NetExceptionHandlerInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        try {
            return chain.proceed(chain.request());
        } catch (Exception e) {
            //See: https://github.com/square/okhttp/issues/3301
            throw new IOException(e.getMessage());
        }
    }
}
