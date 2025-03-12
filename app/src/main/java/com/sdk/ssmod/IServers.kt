package com.sdk.ssmod

import android.annotation.SuppressLint
import android.provider.Settings
import com.ironmeta.one.BuildConfig
import com.sdk.ssmod.IServers.GeoRestrictedException
import com.sdk.ssmod.api.http.beans.FetchResponse
import com.sdk.ssmod.api.http.newRetrofit
import com.sdk.ssmod.api.http.typed.HttpApiService
import com.sdk.ssmod.util.tryIgnoreException
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.awaitResponse
import java.util.*
import kotlin.jvm.Throws

interface IServers {
    @Throws(GeoRestrictedException::class)
    suspend fun refresh(deviceId: String, path: String? = null): FetchResponse?
    fun generateDeviceId(): String

    class NullResponseException(val isSuccessful: Boolean, val errorCode: Int, val errorMessage: String) : RuntimeException()
    /**
     * Server list unavailable because you are accessing our service from geo-restricted location.
     */
    class GeoRestrictedException(e: HttpException) : RuntimeException(e)
}

internal class IServersImpl : IServers {
    private val apiService = newRetrofit().create(HttpApiService::class.java)

    @Throws(GeoRestrictedException::class, IllegalStateException::class, IServers.NullResponseException::class)
    override suspend fun refresh(deviceId: String, path: String?): FetchResponse? {
        if (!IMSDK.isVpnAvailable) {
            val simMcc = IMSDK.device.simMcc
            val netMcc = IMSDK.device.netMcc
            val countryCode = IMSDK.device.os.country
            throw IllegalStateException("sMcc:${simMcc}, nMcc:${netMcc}, cCode:${countryCode}, debug:${BuildConfig.DEBUG}, ")
        }

        val device = IMSDK.device

        val response = apiService.fetchServers(
            url = path ?: HttpApiService.DEFAULT_FETCH_SERVERS_PATH,
            deviceId = deviceId,
            mcc = device.simMcc.toString(),
            mnc = device.simMnc.toString(),
            language = device.os.language,
            region = device.os.country
        ).awaitResponse()
        if (response.isGeoRestricted()) throw GeoRestrictedException(HttpException(response))
        if (response.body() == null) {
            throw IServers.NullResponseException(response.isSuccessful, response.code(), response.message())
        }
        return response.body()
    }

    @SuppressLint("HardwareIds")
    private fun getAndroidId() = tryIgnoreException {
        val cr = IMSDK.app.app.contentResolver
        Settings.Secure.getString(cr, Settings.Secure.ANDROID_ID)
    }

    override fun generateDeviceId(): String =
        UUID.randomUUID().toString().replace("-", "")
}

private fun <T> Response<T>.isGeoRestricted(): Boolean =
    this.code() == 404 && this.headers()["X-API-Service-Unavailable"] == "geo-restricted"
