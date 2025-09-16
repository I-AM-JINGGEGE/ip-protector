package com.vpn.android.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.vpn.android.base.utils.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Google Ad ID 工具类
 * 用于获取设备的广告标识符
 */
object GoogleAdIdUtils {
    
    private const val TAG = "VpnReporter"
    
    /**
     * 获取 Google Ad ID
     * @param context 上下文
     * @return GoogleAdIdInfo 包含 ID 和限制广告跟踪状态，失败时返回 null
     */
    suspend fun getGoogleAdId(context: Context): GoogleAdIdInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
                GoogleAdIdInfo(
                    id = adInfo.id,
                    isLimitAdTrackingEnabled = adInfo.isLimitAdTrackingEnabled
                )
            } catch (e: GooglePlayServicesNotAvailableException) {
                LogUtils.e(TAG, "Google Play Services 不可用", e)
                null
            } catch (e: GooglePlayServicesRepairableException) {
                LogUtils.e(TAG, "Google Play Services 需要修复", e)
                null
            } catch (e: IOException) {
                LogUtils.e(TAG, "获取 Ad ID 时发生 IO 异常", e)
                null
            } catch (e: Exception) {
                LogUtils.e(TAG, "获取 Ad ID 时发生未知异常", e)
                null
            }
        }
    }
    
    /**
     * 获取 Google Ad ID（同步版本）
     * 注意：此方法会阻塞主线程，建议使用异步版本
     * @param context 上下文
     * @return GoogleAdIdInfo 包含 ID 和限制广告跟踪状态，失败时返回 null
     */
    fun getGoogleAdIdSync(context: Context): GoogleAdIdInfo? {
        return try {
            val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
            GoogleAdIdInfo(
                id = adInfo.id,
                isLimitAdTrackingEnabled = adInfo.isLimitAdTrackingEnabled
            )
        } catch (e: GooglePlayServicesNotAvailableException) {
            LogUtils.e(TAG, "Google Play Services 不可用", e)
            null
        } catch (e: GooglePlayServicesRepairableException) {
            LogUtils.e(TAG, "Google Play Services 需要修复", e)
            null
        } catch (e: IOException) {
            LogUtils.e(TAG, "获取 Ad ID 时发生 IO 异常", e)
            null
        } catch (e: Exception) {
            LogUtils.e(TAG, "获取 Ad ID 时发生未知异常", e)
            null
        }
    }
    
    /**
     * 检查是否支持广告 ID
     * @param context 上下文
     * @return true 如果支持，false 否则
     */
    fun isAdIdSupported(context: Context): Boolean {
        return try {
            AdvertisingIdClient.getAdvertisingIdInfo(context)
            true
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Google Ad ID 信息数据类
 */
data class GoogleAdIdInfo(
    val id: String?,
    val isLimitAdTrackingEnabled: Boolean
) {
    /**
     * 获取格式化的 ID 信息
     */
    fun getFormattedInfo(): String {
        return "Ad ID: $id, Limit Ad Tracking: $isLimitAdTrackingEnabled"
    }
    
    /**
     * 检查 ID 是否有效
     */
    fun isValid(): Boolean {
        return id?.isNotBlank() == true && id != "00000000-0000-0000-0000-000000000000"
    }
}
