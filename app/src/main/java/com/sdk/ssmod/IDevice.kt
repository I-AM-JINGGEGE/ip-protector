package com.sdk.ssmod

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.os.LocaleListCompat
import com.sdk.ssmod.util.tryIgnoreException
import java.util.*

interface IDevice {
    val simMcc: Int
    val simMnc: Int
    val netMcc: Int
    val netMnc: Int
    val model: String?
    val brand: String?
    val os: OperatingSystem

    interface OperatingSystem {
        val version: String
        val country: String
        val language: String
    }
}

internal class IDeviceImpl : IDevice {
    override val simMcc: Int
        get() = tryIgnoreException { simOperator!!.slice(0..2).toInt() } ?: -1
    override val simMnc: Int
        get() = tryIgnoreException { simOperator!!.substring(3).toInt() } ?: -1
    override val netMcc: Int
        get() = tryIgnoreException { netOperator!!.slice(0..2).toInt() } ?: -1
    override val netMnc: Int
        get() = tryIgnoreException { netOperator!!.substring(3).toInt() } ?: -1
    override val model: String? get() = Build.MODEL
    override val brand: String? get() = Build.BRAND
    override val os: IDevice.OperatingSystem = OperatingSystemImpl()
    private val simOperator: String? get() = telephonyManager.simOperator
    private val netOperator: String? get() = telephonyManager.networkOperator

    private val telephonyManager: TelephonyManager by lazy {
        IMSDK.app.app.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }
}

internal class OperatingSystemImpl : IDevice.OperatingSystem {
    override val version: String get() = Build.VERSION.RELEASE
    override val country: String get() = locale.country
    override val language: String get() = locale.language

    private val locale: Locale get() =
        LocaleListCompat.getAdjustedDefault()[0] ?: Locale.getDefault()
}
