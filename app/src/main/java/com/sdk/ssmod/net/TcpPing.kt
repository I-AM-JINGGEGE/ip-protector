package com.sdk.ssmod.net

import com.sdk.ssmod.util.closeQuietly
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.time.Duration
import java.time.Instant

class TcpPing(private val address: InetSocketAddress, private val timeoutMs: Int) {
    suspend fun ping(): Deferred<Int> = coroutineScope {
        async(Dispatchers.IO) {
            val socket = Socket()
            try {
                val before = Instant.now()
                socket.connect(address, timeoutMs)
                val after = Instant.now()
                Duration.between(before, after).abs().toMillis().toInt()
            } catch (e: IOException) {
                Int.MAX_VALUE
            } catch (e: SocketTimeoutException) {
                Int.MAX_VALUE
            } finally {
                socket.closeQuietly()
            }
        }
    }
}
