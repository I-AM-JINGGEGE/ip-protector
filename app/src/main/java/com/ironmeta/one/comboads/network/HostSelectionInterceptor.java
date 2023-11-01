package com.ironmeta.one.comboads.network;

import java.io.IOException;
import java.net.URISyntaxException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HostSelectionInterceptor implements Interceptor {
    private HttpUrl host;

    public HostSelectionInterceptor(String hostName){
        host = HttpUrl.parse(hostName);
    }

    @Override
    public okhttp3.Response intercept(Chain chain)  throws IOException {
        Request request = chain.request();
        Response response = null;
        try {
            response = chain.proceed(request);
        } catch (IOException e) {
            if (response != null) {
                response.close();
            }
            try {
                HttpUrl newUrl = request.url().newBuilder()
                        .scheme(host.scheme())
                        .host(host.url().toURI().getHost())
                        .build();
                request = request.newBuilder()
                        .url(newUrl)
                        .build();
                response = chain.proceed(request);
            } catch (URISyntaxException uriSyntaxException) {
                uriSyntaxException.printStackTrace();
                response = chain.proceed(request);
            }
        }

        return response;
    }
}