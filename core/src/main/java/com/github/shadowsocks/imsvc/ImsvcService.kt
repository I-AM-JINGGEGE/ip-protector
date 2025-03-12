package com.github.shadowsocks.imsvc

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.github.shadowsocks.Core
import com.github.shadowsocks.aidl.imsvc.IConnectedServerService.CONNECTED_SERVER_SERVICE
import com.github.shadowsocks.aidl.imsvc.IUptimeLimitService.UPTIME_LIMIT_SERVICE
import com.github.shadowsocks.core.R
import com.github.shadowsocks.imsvc.connection.ConnectedServerBinder
import com.github.shadowsocks.imsvc.connection.ConnectedTo
import com.github.shadowsocks.imsvc.uptime.Uptime
import com.github.shadowsocks.imsvc.uptime.UptimeLimitBinder
import com.github.shadowsocks.imsvc.uptime.UptimeTimer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.time.Duration
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext

class ImsvcService : Service(), CoroutineScope {
    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Main.immediate
    internal val connectedTo: AtomicReference<ConnectedTo> = AtomicReference()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_FOREGROUND) {
            stopForeground(true)
            return START_NOT_STICKY
        }
        if (intent?.getBooleanExtra(IS_FOREGROUND_EXTRA, false) == true) {
            val notificationId = Core.imApp?.notification?.notificationId ?: 1
            val notification = Core.imApp?.notification?.builder?.build() ?: NotificationCompat
                .Builder(this, "service-vpn")
                .setSmallIcon(Core.imApp?.notification?.iconId ?: R.drawable.ic_service_active)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(notificationId, notification,
                    FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED)
            } else {
                startForeground(notificationId, notification)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return when (intent?.action) {
            UPTIME_LIMIT_SERVICE -> uptimeLimitBinder
            CONNECTED_SERVER_SERVICE -> ConnectedServerBinder(this)
            else -> null
        }
    }

    override fun onDestroy() {
        cancel("onDestroy")
        super.onDestroy()
    }

    private val uptimeLimitImpl = object : UptimeLimitBinder.UptimeLimit {
        private var uptimeTimer: UptimeTimer = UptimeTimer(Uptime())
        override val ongoing: Uptime get() = uptimeTimer.ongoing

        override fun startNew(uptime: Uptime) {
            synchronized(this) {
                val isRunning = uptimeTimer.ongoing.run { !isExpired }
                if (!isRunning) {
                    uptimeTimer = UptimeTimer(uptime)
                } else {
                    throw IllegalStateException()
                }
            }
        }

        override fun extend(duration: Duration) {
            uptimeTimer.extend(duration)
        }

        override fun cancel() {
            uptimeTimer.cancel()
        }
    }
    private val uptimeLimitBinder = UptimeLimitBinder(uptimeLimitImpl)

    companion object {
        private const val IS_FOREGROUND_EXTRA = "is_foreground"
        private const val ACTION_STOP_FOREGROUND = "stop_foreground"

        fun startService(context: Context, foreground: Boolean = false) {
            val intent = Intent(context, ImsvcService::class.java)
            if (foreground) {
                intent.putExtra(IS_FOREGROUND_EXTRA, true)
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopForeground(context: Context) {
            val intent = Intent(context, ImsvcService::class.java)
            intent.action = ACTION_STOP_FOREGROUND
            context.startService(intent)
        }
    }
}
