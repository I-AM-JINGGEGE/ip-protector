package com.vpn.android.server

import com.vpn.android.utils.ChannelUtils

interface ServerPathConstants {
    companion object {
        const val DEBUG_HOST = "https://test.ironmeta.com"

        const val HOST_MAIN_1 = "https://api.ip-protector.net"

        val BASE_URL_PROD = if (ChannelUtils.isDebugFlavor()) {
            DEBUG_HOST
        } else {
            HOST_MAIN_1
        }
    }
}
