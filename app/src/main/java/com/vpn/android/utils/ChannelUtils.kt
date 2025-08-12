package com.vpn.android.utils

import com.vpn.android.BuildConfig

object ChannelUtils {
    fun isDebugFlavor(): Boolean {
        return BuildConfig.APPLICATION_ID == "com.vpn.android.debug"
    }
}