package com.vpn.android.network

import android.content.Context
import android.util.Log
import com.vpn.android.base.utils.BuildConfigUtils
import com.vpn.android.base.utils.DeviceUtils
import com.vpn.android.base.utils.LogUtils
import com.vpn.android.comboads.network.HttpClientRetryInterceptor
import com.vpn.tahiti.TahitiCoreServiceAppsBypassUtils
import com.vpn.tahiti.TahitiCoreServiceUserUtils
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.Proxy
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * IP 查询 Retrofit 客户端
 * 参考 UserProfileRetrofit 实现
 */
class IpRetrofit private constructor() {
    private val ipService: IpService

    companion object {
        @JvmStatic
        val instance: IpRetrofit by lazy { IpRetrofit() }
        
        private const val TAG = "IpRetrofit"
    }

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://${TahitiCoreServiceAppsBypassUtils.getDomainBypass()}/") // 使用 ironmeta API
            .addConverterFactory(GsonConverterFactory.create())
            .client(retryNetWorkHttpClient())
            .build()
        ipService = retrofit.create(IpService::class.java)
    }

    private fun retryNetWorkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        return builder
            .connectTimeout((30 * 1000).toLong(), TimeUnit.MILLISECONDS)
            .readTimeout((30 * 1000).toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout((30 * 1000).toLong(), TimeUnit.MILLISECONDS)
            .addInterceptor(HttpClientRetryInterceptor())
            // 关键：为访问 bypassDomains 中的域名禁用代理
            .proxy(Proxy.NO_PROXY)
            .build()
    }

    /**
     * 获取 IP 信息，使用与 UserProfileRetrofit.getUserProfile 相同的请求参数
     */
    fun getIpInfo(context: Context) {
        // 构建与 ad_config 相同的请求参数
        val pkg = BuildConfigUtils.getPackageName(context)
        val cv = BuildConfigUtils.getVersionCode(context)
        val cnl = BuildConfigUtils.getCnl(context)
        val did = TahitiCoreServiceUserUtils.getUid(context)
        val mcc = DeviceUtils.getMcc(context)
        val mnc = DeviceUtils.getMnc(context)
        val lang = DeviceUtils.getOSLang(context)
        val rgn = DeviceUtils.getOSCountry(context)
        val random = Random().nextInt()

        val map: MutableMap<String?, Any?> = HashMap()
        map["cv"] = cv
        map["cnl"] = cnl
        map["pkg"] = pkg
        map["did"] = did
        map["mcc"] = mcc
        map["mnc"] = mnc
        map["lang"] = lang
        map["rgn"] = rgn
        map["_random"] = random

        Log.d(TAG, "Request parameters:")
        map.forEach { (key, value) ->
            Log.d(TAG, "  $key: $value")
        }

        val call = ipService.getIp(map)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body()?.string()
                        LogUtils.i(TAG, "✅ IP API Response Success:")
                        LogUtils.i(TAG, "Response Body: $responseBody")
                        
                        // 根据您提供的示例，响应应该是: {"ip":"54.208.119.170"}
                        responseBody?.let { body ->
                            LogUtils.i(TAG, "📍 Current IP: $body")
                        }
                    } catch (e: Exception) {
                        LogUtils.e(TAG, "❌ Error reading response body", e)
                    }
                } else {
                    LogUtils.e(TAG, "❌ IP API Response Failed:")
                    LogUtils.e(TAG, "Response Code: ${response.code()}")
                    LogUtils.e(TAG, "Response Message: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                LogUtils.e(TAG, "❌ IP API Request Failed:", t)
                LogUtils.e(TAG, "Error Message: ${t.message}")
            }
        })
    }
}

