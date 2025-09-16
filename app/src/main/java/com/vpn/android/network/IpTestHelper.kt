package com.vpn.android.network

import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.view.View
import com.vpn.android.R
import com.vpn.android.base.utils.LogUtils
import com.vpn.android.base.utils.ThreadUtils
import java.net.HttpURLConnection
import java.net.URL

class IpTestHelper {
    
    companion object {
        private const val TAG = "VpnReporter"
        
        /**
         * 异步方式获取 IP
         */
        @JvmStatic
        fun testGetIpAsync(context: Context) {
            LogUtils.i(TAG, "🚀 开始测试 vpn 连通性")
            Thread {
                var result = false
                var conn: HttpURLConnection? = null
                try {
                    val url = URL("https", "www.google.com", "/generate_204")
                    conn = url.openConnection() as HttpURLConnection
                    conn.setRequestProperty("Connection", "close")
                    conn.connectTimeout = 5000
                    conn.readTimeout = 5000
                    conn.instanceFollowRedirects = false
                    conn.useCaches = false
                    val start = SystemClock.elapsedRealtime()
                    val code = conn.responseCode
                    val elapsed = SystemClock.elapsedRealtime() - start
                    val length = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        conn.contentLengthLong
                    } else {
                        conn.contentLength.toLong()
                    }
                    if (code == 204 || code == 200 && length == 0L) {
                        result = true
                    } else {
                        result = false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    result = false
                } finally {
                    conn?.disconnect()
                }
                LogUtils.i(TAG, "🚀 开始上报 vpn 连通性结果：$result")
                IpRetrofit.instance.getIpInfo(context, result)
            }.start()
        }
    }
}
