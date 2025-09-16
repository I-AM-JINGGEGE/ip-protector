package com.vpn.android.network

import android.content.Context
import com.google.gson.Gson
import com.vpn.android.base.utils.BuildConfigUtils
import com.vpn.android.base.utils.LogUtils
import com.vpn.android.comboads.network.HttpClientRetryInterceptor
import com.vpn.android.region.RegionConstants.KEY_DT_ID
import com.vpn.android.utils.GoogleAdIdUtils
import com.vpn.base.vstore.VstoreManager
import com.vpn.tahiti.TahitiCoreServiceAppsBypassUtils
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.Proxy
import java.security.SecureRandom
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
        
        private const val TAG = "VpnReporter"
    }

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://${TahitiCoreServiceAppsBypassUtils.getDomainBypass()}/")
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
    fun getIpInfo(context: Context, result: Boolean) {
        val pkg = "com.free.ip.protector"
        val cv = BuildConfigUtils.getVersionCode(context)
        val nv = BuildConfigUtils.getVersionName(context)

        // 获取 Google Ad ID
        val googleAdId = GoogleAdIdUtils.getGoogleAdIdSync(context)
        val gaid = googleAdId?.id ?: ""

        val json = JSONObject()
        json.put("#dt_id", VstoreManager.getInstance(context).decode(true, KEY_DT_ID, ""))
        json.put("#app_id", "dt_526bf4b6e996cac1")
        json.put("#event_type", "track")
        json.put("#event_time", System.currentTimeMillis())
        json.put("#event_name", "connectivity")
        json.put("#bundle_id", pkg)
        json.put("#event_syn", getUUID())
        json.put("#gaid", gaid)
        json.put("properties", JSONObject().apply {
            put("result", result)
            put("#app_version_code", cv)
            put("#app_version_name", nv)
        })

        val jsonString = json.toString()

        LogUtils.i(TAG, "📄 转换后的 JSON:")
        LogUtils.i(TAG, jsonString)

        // 创建 RequestBody
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonString.toRequestBody(mediaType)

        val call = ipService.postRelay(requestBody)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body()?.string()
                        LogUtils.i(TAG, "✅ 上报连通性 Response Success, Body: $responseBody")
                    } catch (e: Exception) {
                        LogUtils.e(TAG, "❌ 上报连通性 Error, response body: ", e)
                    }
                } else {
                    LogUtils.e(TAG, "❌ 上报连通性 Response Failed:")
                    LogUtils.e(TAG, "Response Code: ${response.code()}")
                    LogUtils.e(TAG, "Response Message: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                LogUtils.e(TAG, "❌ 上报连通性 Request Failed:", t)
                LogUtils.e(TAG, "Error Message: ${t.message}")
            }
        })
    }

    private fun getUUID(): String {
        val random = SecureRandom()
        val uuid = random.nextLong().toString()
        return uuid.replace("-", "")
    }
}

