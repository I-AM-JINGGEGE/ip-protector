package com.ironmeta.one.comboads.network;

import com.ironmeta.one.ads.bean.UserAdConfig;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * author: xiaosailing
 * date: 2022-03-23
 * description:
 * version：1.0
 */
public interface UserProfileService {
    @GET("/ad_config")
    Call<UserAdConfig> adUserProfile(@QueryMap Map<String,Object> map);
    @GET("/hbz")
    Call<ResponseBody> reportBeat(@QueryMap Map<String,Object> map);
}
