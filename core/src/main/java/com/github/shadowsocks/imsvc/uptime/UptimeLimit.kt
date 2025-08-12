package com.github.shadowsocks.imsvc.uptime

import android.os.Parcelable
import android.os.SystemClock
import com.github.shadowsocks.Core
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.time.Duration
import java.util.*
import kotlin.concurrent.timer

/**
 * Thread-safe: yes, immutable.
 */
@Parcelize
class Uptime(
    val duration: Duration = Duration.ZERO,
    val startedAt: Long = now()
) : Parcelable {
    val isInvalid: Boolean
        get() = now() !in LongRange(startedAt, expectedEndsAt)

    @IgnoredOnParcel
    val expectedEndsAt: Long = startedAt + duration.toMillis()
    val remaining: Long
        get() = expectedEndsAt - SystemClock.elapsedRealtime()
    val isExpired: Boolean get() = now() >= expectedEndsAt

    infix operator fun plus(duration: Duration): Uptime =
        Uptime(this.duration + duration, this.startedAt)

    companion object {
        fun now() = SystemClock.elapsedRealtime()
    }
}

class UptimeTimer(uptime: Uptime) {
    var ongoing: Uptime = uptime
        private set
    private var timer: Timer? = null

    private val remaining: Long get() = ongoing.remaining

    @Synchronized
    fun extend(duration: Duration): Uptime {
        ongoing += duration
        return ongoing
    }

    init {
        if (uptime.duration != Duration.ZERO) {
            start()
        }
    }

    private fun start() {
        timer = timer(period = 1, action = timerTaskCheckExpiryEverySecond())
    }

    private fun timerTaskCheckExpiryEvery90Seconds(): TimerTask.() -> Unit = {
        onTick()
        when {
            remaining <= 90 -> {
                timer = timer(period = 1_000, action = timerTaskCheckExpiryEverySecond())
                cancel()
            }
            remaining <= 0 -> {
                cancel()
                onExpire()
            }
        }
    }

    private fun timerTaskCheckExpiryEverySecond(): TimerTask.() -> Unit = {
        onTick()
        when {
            remaining >= 90 -> {
                timer = timer(period = 90_000, action = timerTaskCheckExpiryEvery90Seconds())
                cancel()
            }
            remaining <= 0 -> {
                cancel()
                onExpire()
            }
        }
    }

    private fun onTick() = Unit

    private fun onExpire() {
        Core.stopService()
    }

    @Synchronized
    fun cancel() {
        timer?.purge()
        timer?.cancel()
        timer = null
        ongoing = Uptime()
    }
}
