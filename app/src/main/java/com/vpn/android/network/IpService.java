package com.vpn.android.network;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IpService {
    @POST("/relay")
    Call<ResponseBody> postRelay(@Body RequestBody jsonBody);
}

