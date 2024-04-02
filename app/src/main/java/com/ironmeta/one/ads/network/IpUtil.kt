package com.ironmeta.one.ads.network

import androidx.annotation.MainThread
import com.ironmeta.base.vstore.VstoreManager
import com.ironmeta.one.MainApplication
import com.ironmeta.one.region.RegionConstants.KEY_CONNECTED_VPN_IP

object IpUtil {
    @MainThread
    fun getConnectedIdAddress(): String {
        return VstoreManager.getInstance(MainApplication.instance).decode(false, KEY_CONNECTED_VPN_IP, "")
    }
}