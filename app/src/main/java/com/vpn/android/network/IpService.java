package com.vpn.android.network;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * IP 查询服务接口
 */
public interface IpService {
    @GET("/get_ip")
    Call<ResponseBody> getIp(@QueryMap Map<String, Object> map);
}

