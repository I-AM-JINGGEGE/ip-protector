package com.sdk.ssmod.api.http.typed

import com.sdk.ssmod.IMSDK
import com.sdk.ssmod.api.http.beans.FetchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url
import kotlin.random.Random

interface HttpApiService {
    @GET
    fun fetchServers(
        @Url url: String,
        @Query("cv") appVersionName: String = IMSDK.app.versionName,
        @Query("ver_code") appVersionCode: Int = IMSDK.app.versionCode,
        @Query("cnl") channel: String = CHANNEL_GOOGLE_PLAY,
        @Query("pkg") packageName: String = "com.free.ip.protector",
        @Query("did") deviceId: String,
        @Query("mcc") mcc: String,
        @Query("mnc") mnc: String,
        @Query("lang") language: String,
        @Query("rgn") region: String,
        @Query("_random") random: Int = Random.nextInt()
    ): Call<FetchResponse>

    companion object {
        private const val CHANNEL_GOOGLE_PLAY = "gp"
        const val DEFAULT_FETCH_SERVERS_PATH = "reload/2"
    }
}
