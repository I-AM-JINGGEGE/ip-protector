package com.vpn.android.comboads.network

import android.content.Context
import android.text.TextUtils
import com.vpn.android.ads.bean.UserAdConfig
import com.vpn.android.ads.network.IpUtil.getConnectedIdAddress
import com.vpn.android.base.utils.BuildConfigUtils
import com.vpn.android.base.utils.DeviceUtils
import com.vpn.android.server.ServerPathConstants
import com.vpn.tahiti.TahitiCoreServiceUserUtils
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Random
import java.util.concurrent.TimeUnit

/**
 * author: xiaosailing
 * date: 2022-03-24
 * description:
 * version：1.0
 */
class UserProfileRetrofit private constructor() {
    private val userRangeService: UserProfileService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(ServerPathConstants.BASE_URL_PROD)
            .addConverterFactory(GsonConverterFactory.create())
            .client(retryNetWorkHttpClient())
            .build()
        userRangeService = retrofit.create<UserProfileService>(UserProfileService::class.java)
    }

    private fun retryNetWorkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        val client = builder
            .connectTimeout((30 * 1000).toLong(), TimeUnit.MILLISECONDS)
            .readTimeout((30 * 1000).toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout((30 * 1000).toLong(), TimeUnit.MILLISECONDS)
            .addInterceptor(HttpClientRetryInterceptor())
            .build()
        return client
    }

    fun getUserProfile(context: Context, callback: Callback<UserAdConfig>) {
        val pkg = "com.free.ip.protector"
        val cv = BuildConfigUtils.getVersionCode(context)
        val cnl = BuildConfigUtils.getCnl(context)
        val did = TahitiCoreServiceUserUtils.getUid(context)
        val mcc = DeviceUtils.getMcc(context)
        val mnc = DeviceUtils.getMnc(context)
        val lang = DeviceUtils.getOSLang(context)
        val rgn = DeviceUtils.getOSCountry(context)
        val random = Random().nextInt()

        val map: MutableMap<String?, Any?> = HashMap<String?, Any?>()
        map.put("cv", cv)
        map.put("cnl", cnl)
        map.put("pkg", pkg)
        map.put("did", did)
        map.put("mcc", mcc)
        map.put("mnc", mnc)
        map.put("lang", lang)
        map.put("rgn", rgn)
        map.put("_random", random)
        val rqCol = userRangeService.adUserProfile(map)
        rqCol.enqueue(callback)
    }

    fun reportBeat(context: Context, callback: Callback<ResponseBody>) {
        val pkg = "com.free.ip.protector"
        val cv = BuildConfigUtils.getVersionCode(context)
        val cnl = BuildConfigUtils.getCnl(context)
        val did = TahitiCoreServiceUserUtils.getUid(context)
        val mcc = DeviceUtils.getMcc(context)
        val mnc = DeviceUtils.getMnc(context)
        val lang = DeviceUtils.getOSLang(context)
        val rgn = DeviceUtils.getOSCountry(context)
        val random = Random().nextInt()

        val map: MutableMap<String?, Any?> = HashMap<String?, Any?>()
        map.put("cv", cv)
        map.put("cnl", cnl)
        map.put("pkg", pkg)
        map.put("did", did)
        map.put("mcc", mcc)
        map.put("mnc", mnc)
        map.put("lang", lang)
        map.put("rgn", rgn)
        val ipAddress = getConnectedIdAddress()
        if (!TextUtils.isEmpty(ipAddress)) {
            map.put("ip", ipAddress)
        }
        map.put("_random", random)
        val rqCol = userRangeService.reportBeat(map)
        rqCol.enqueue(callback)
    }

    companion object {
        private var userProfileRetrofit: UserProfileRetrofit? = null

        @JvmStatic
        val instance: UserProfileRetrofit
            get() {
                if (userProfileRetrofit == null) {
                    userProfileRetrofit = UserProfileRetrofit()
                }
                return userProfileRetrofit!!
            }
    }
}
