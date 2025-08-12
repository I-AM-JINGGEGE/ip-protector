package com.github.shadowsocks.imsvc.uptime

import com.github.shadowsocks.aidl.imsvc.IUptimeLimitService
import java.time.Duration

class UptimeLimitBinder(private val uptimeLimit: UptimeLimit) : IUptimeLimitService.Stub() {
    override fun startNew(uptime: Uptime) = uptimeLimit.startNew(uptime)
    override fun extend(duration: Long) = uptimeLimit.extend(Duration.ofMillis(duration))
    override fun cancel() = uptimeLimit.cancel()
    override fun getOngoing(): Uptime = uptimeLimit.ongoing

    interface UptimeLimit {
        val ongoing: Uptime

        fun startNew(uptime: Uptime)
        fun extend(duration: Duration)
        fun cancel()
    }
}
