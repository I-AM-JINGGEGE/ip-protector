package com.vpn.android.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IpTestHelper {
    
    companion object {
        private const val TAG = "IpRetrofit"
        
        /**
         * 异步方式获取 IP
         */
        @JvmStatic
        fun testGetIpAsync(context: Context) {
            Log.i(TAG, "🚀 开始异步获取 IP 信息...")
            IpRetrofit.instance.getIpInfo(context)
        }
    }
}
