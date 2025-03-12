package com.sdk.ssmod

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.pm.PackageInfoCompat
import kotlinx.coroutines.CoroutineScope
import kotlin.reflect.KClass

interface IIMSDKApplication : CoroutineScope {
    val app: Application
    val applicationId: String
    val configureClass: KClass<out Any>
    val notification: CustomNotification? get() = null

    private val packageManager get() = app.packageManager!!
    val appInfo get() = packageManager.getApplicationInfo(applicationId, 0)
    val packageInfo: PackageInfo
        @SuppressLint("PackageManagerGetSignatures")
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val flags = PackageManager.GET_SIGNING_CERTIFICATES
            packageManager.getPackageInfo(appInfo.packageName, flags)
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(appInfo.packageName, PackageManager.GET_SIGNATURES)
        }
    val versionName get() = packageInfo.versionName!!
    val versionCode get() = PackageInfoCompat.getLongVersionCode(packageInfo).toInt()
//    val signature: Signature get() = packageInfo.signaturesCompat.first()

//    val certBytes: ByteArray

    fun onServiceDisconnected()
    fun onBinderDied()

    interface CustomNotification {
        val iconId: Int? get() = null
        val notificationId: Int? get() = null
        val builder: NotificationCompat.Builder? get() = null
        val hasToDismissAfterDisconnection: Boolean get() = true
        val isLiveSpeedEnabled: Boolean get() = true

        fun onProfileChange(name: String? = null) = Unit
        fun onTrafficStatsUpdate(profileId: Long, liveSpeed: String, totalTransfer: String) = Unit
        fun onTrafficStatsUpdate(profileId: Long, stats: com.sdk.ssmod.beans.TrafficStats) = Unit
    }
}