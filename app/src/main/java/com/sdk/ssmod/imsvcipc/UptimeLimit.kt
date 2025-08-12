package com.sdk.ssmod.imsvcipc

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.DeadObjectException
import android.os.IBinder
import android.os.SystemClock
import com.github.shadowsocks.aidl.imsvc.IUptimeLimitService
import com.github.shadowsocks.imsvc.ImsvcService
import com.github.shadowsocks.imsvc.uptime.Uptime
import com.sdk.ssmod.IMSDK
import com.sdk.ssmod.util.tryIgnoreException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

object UptimeLimit : ServiceConnection, IBinder.DeathRecipient, CoroutineScope {
    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Main.immediate

    @Volatile
    var ongoing: IUptimeTimer = IUptimeTimerEmptyImpl()
        private set

    private var service: IUptimeLimitService? = null
    private val isConnected = AtomicBoolean()

    fun init() {
        connect(IMSDK.app.app)
    }

    /**
     * @throws IllegalStateException if a timer is still running or the `duration` parameter
     *                               is less than 1 second.
     */
    @Synchronized
    fun startNew(duration: Duration) {
        val deadline = SystemClock.elapsedRealtime() + 1000 // Wait for 1 sec at least.
        while (SystemClock.elapsedRealtime() < deadline && service == null) {
            Thread.yield()
        }
        val service = service ?: return
        service.startNew(Uptime(duration))
        service.asBinder().linkToDeath(this, 0)
        ongoing = FailSafeIUptimeTimerImpl(service)
    }

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        val service = IUptimeLimitService.Stub.asInterface(binder)
        this.service = service
        ongoing = FailSafeIUptimeTimerImpl(service)
    }

    override fun onServiceDisconnected(name: ComponentName) {
        disconnect(IMSDK.app.app)
        connect(IMSDK.app.app)
    }

    internal fun connect(context: Context) {
        val intent = Intent(context, ImsvcService::class.java)
            .setAction(IUptimeLimitService.UPTIME_LIMIT_SERVICE)
        val result = context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        isConnected.set(result)
    }

    internal fun disconnect(context: Context) {
        isConnected.set(false)
        service = null
        tryIgnoreException { context.unbindService(this) }
        binderDied()
    }

    override fun binderDied() {
        launch { connect(IMSDK.app.app) }
    }
}

internal fun IUptimeLimitService.hasAnOngoingTimer() = !this.ongoing.isExpired

interface IUptimeTimer {
    val isRunning: Boolean
    val startedAt: Long
    val duration: Duration
    val expectedEndsAt: Long
    val remaining: Long
    val isExpired: Boolean

    fun extend(duration: Duration)
    fun cancel()
}

internal class IUptimeTimerEmptyImpl : IUptimeTimer {
    override val isRunning: Boolean = false
    override val startedAt: Long = -1
    override val duration: Duration = Duration.ZERO
    override val expectedEndsAt: Long = -1
    override val remaining: Long = -1
    override val isExpired: Boolean = true
    override fun extend(duration: Duration) = Unit
    override fun cancel() = Unit
}

internal class FailSafeIUptimeTimerImpl(private val service: IUptimeLimitService) : IUptimeTimer {
    private val ongoing: Uptime?
        get() = try {
            service.ongoing
        } catch (e: DeadObjectException) {
            UptimeLimit.disconnect(IMSDK.app.app)
            null
        }

    override val isRunning: Boolean get() = ongoing?.isExpired?.not() ?: false
    override val startedAt: Long get() = ongoing?.startedAt ?: DEFAULT_UPTIME.startedAt
    override val duration: Duration get() = ongoing?.duration ?: DEFAULT_UPTIME.duration
    override val expectedEndsAt: Long
        get() = ongoing?.expectedEndsAt ?: DEFAULT_UPTIME.expectedEndsAt
    override val remaining: Long get() = ongoing?.remaining ?: DEFAULT_UPTIME.remaining
    override val isExpired: Boolean get() = ongoing?.isExpired ?: DEFAULT_UPTIME.isExpired

    override fun extend(duration: Duration) {
        tryIgnoreException { service.extend(duration.toMillis()) }
    }

    override fun cancel() {
        tryIgnoreException { service.cancel() }
    }

    companion object {
        private val DEFAULT_UPTIME = Uptime()
    }
}
