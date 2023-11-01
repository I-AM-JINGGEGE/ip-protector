package com.ironmeta.one.ads.network

import androidx.annotation.MainThread
import com.ironmeta.one.MainApplication
import com.ironmeta.tahiti.TahitiCoreServiceStateInfoManager
import com.sdk.ssmod.IMSDK

object IpUtil {
    @MainThread
    fun getConnectedIdAddress(): String {
        return IMSDK.connectedServer.value?.host?.host?:""
    }
}