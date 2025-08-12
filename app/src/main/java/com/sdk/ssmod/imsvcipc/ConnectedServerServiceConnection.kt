package com.sdk.ssmod.imsvcipc

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.IInterface
import android.os.RemoteException
import android.os.SystemClock
import com.github.shadowsocks.aidl.imsvc.IConnectedServerService
import com.github.shadowsocks.imsvc.ImsvcService
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.sdk.ssmod.IMSDK.WithResponseBuilder.ConnectedTo
import com.sdk.ssmod.util.pingOrNull
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

class ConnectedServerServiceConnection(
    private val onBinderDied: (() -> Unit)? = null
) : ServiceConnection, IBinder.DeathRecipient, IInterface {
    @Volatile
    private var service: IConnectedServerService? = null

    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
        val service = IConnectedServerService.Stub.asInterface(binder)
        try {
            service.asBinder().linkToDeath(this, 0)
            this.service = service
        } catch (e: RemoteException) {
            binderDied()
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        this.service = null
        onBinderDied?.invoke()
    }

    override fun binderDied() {
        service = null
        onBinderDied?.invoke()
    }

    override fun asBinder(): IBinder? = service?.asBinder()

    fun bindService(context: Context): Boolean {
        val intent = Intent(context, ImsvcService::class.java)
        intent.action = IConnectedServerService.CONNECTED_SERVER_SERVICE
        return context.bindService(intent, this, Service.BIND_AUTO_CREATE)
    }

    suspend fun bindServiceDeferred(
        context: Context
    ): Deferred<ConnectedServerServiceConnection?> = coroutineScope {
        async {
            val intent = Intent(context, ImsvcService::class.java)
            intent.action = IConnectedServerService.CONNECTED_SERVER_SERVICE
            val isBindServiceSuccess = context.bindService(
                intent,
                this@ConnectedServerServiceConnection,
                Service.BIND_AUTO_CREATE
            )
            if (!isBindServiceSuccess) return@async null
            if (!waitServiceConnection()) return@async null
            this@ConnectedServerServiceConnection.pingOrNull()
        }
    }

    fun unbindService(context: Context) {
        val service = service ?: return
        service.asBinder().unlinkToDeath(this, 0)
        context.unbindService(this)
    }

    fun getConnectedTo(): ConnectedTo? {
        val theImsvcOne = try {
            service?.pingOrNull()?.connectedTo
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            null
        } ?: return null
        return ConnectedTo(theImsvcOne.zoneId, theImsvcOne.country)
    }

    fun setConnectedTo(connectedTo: ConnectedTo?) {
        service?.connectedTo = connectedTo?.toImsvcOne()
    }

    suspend fun waitServiceConnection(timeout: Long = 5000): Boolean {
        val deadline = SystemClock.elapsedRealtime() + timeout
        while (SystemClock.elapsedRealtime() < deadline && service == null) {
            delay(250)
        }
        return service != null
    }
}
