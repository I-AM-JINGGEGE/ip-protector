package com.sdk.ssmod.api.http

import com.sdk.ssmod.util.IMAesUtil
import com.vpn.android.base.utils.LogUtils
import com.vpn.android.server.ServerPathConstants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

fun newOkHttpClient(): OkHttpClient =
    OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(RequestLoggingInterceptor())
        .addInterceptor(DecryptResponseInterceptor())
        .build()

fun newRetrofit(client: OkHttpClient = newOkHttpClient()): Retrofit =
    Retrofit.Builder()
        .baseUrl(ServerPathConstants.BASE_URL_PROD)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

private class RequestLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // 记录请求信息
        LogUtils.i("OkHttp", "=== 请求开始 ===")
        LogUtils.i("OkHttp", "Method: ${request.method}")
        LogUtils.i("OkHttp", "URL: ${request.url}")
        LogUtils.i("OkHttp", "Headers: ${request.headers}")
        
        // 记录查询参数
        val queryNames = request.url.queryParameterNames
        if (queryNames.isNotEmpty()) {
            LogUtils.i("OkHttp", "Query Parameters:")
            queryNames.forEach { paramName ->
                val paramValue = request.url.queryParameter(paramName)
                LogUtils.i("OkHttp", "  $paramName: $paramValue")
            }
        }
        
        LogUtils.i("OkHttp", "=== 请求结束 ===")
        
        val response = chain.proceed(request)
        
        // 记录响应信息
        LogUtils.i("OkHttp", "=== 响应开始 ===")
        LogUtils.i("OkHttp", "Response Code: ${response.code}")
        LogUtils.i("OkHttp", "Response Message: ${response.message}")
        LogUtils.i("OkHttp", "Response Headers: ${response.headers}")
        LogUtils.i("OkHttp", "=== 响应结束 ===")
        
        return response
    }
}

private class DecryptResponseInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (!response.isSuccessful) return response
        val responseBody = response.body ?: return response
        if (responseBody.contentLength() == 0L) return response
        val iv = response.header("EncIV")?.toByteArray() ?: return response
        val outStream = ByteArrayOutputStream()
        IMAesUtil.decrypt(iv, responseBody.byteStream(), outStream)
        val decryptedBody = outStream.toByteArray().toResponseBody(responseBody.contentType())
        return response.newBuilder().body(decryptedBody).build()
    }
}
