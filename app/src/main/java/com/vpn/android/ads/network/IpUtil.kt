package com.vpn.android.ads.network

import androidx.annotation.MainThread
import com.vpn.base.vstore.VstoreManager
import com.vpn.android.MainApplication
import com.vpn.android.region.RegionConstants.KEY_PROFILE_VPN_IP

object IpUtil {
    @MainThread
    fun getConnectedIdAddress(): String {
        return VstoreManager.getInstance(MainApplication.instance).decode(false, KEY_PROFILE_VPN_IP, "")
    }
}