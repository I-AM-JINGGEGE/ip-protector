package com.vpn.android.network

import android.content.Context
import com.vpn.android.base.utils.LogUtils

class IpTestHelper {
    
    companion object {
        private const val TAG = "IpRetrofit"
        
        /**
         * 异步方式获取 IP
         */
        @JvmStatic
        fun testGetIpAsync(context: Context) {
            LogUtils.i(TAG, "🚀 开始异步获取 IP 信息...")
            IpRetrofit.instance.getIpInfo(context)
        }
    }
}
