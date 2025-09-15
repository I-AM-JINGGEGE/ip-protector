package com.sdk.ssmod

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.RemoteException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.shadowsocks.Core
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.imsvc.ImsvcService
import com.github.shadowsocks.utils.Action
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.sdk.ssmod.IMSDK.VpnState
import com.sdk.ssmod.IMSDK.WithResponseBuilder.ConnectedTo
import com.sdk.ssmod.api.http.beans.FetchResponse
import com.sdk.ssmod.api.http.ping.ServerPing
import com.sdk.ssmod.beans.TrafficStats
import com.sdk.ssmod.imsvcipc.ConnectedServerServiceConnection
import com.sdk.ssmod.imsvcipc.UptimeLimit
import com.sdk.ssmod.util.pingOrNull
import com.sdk.ssmod.util.tryIgnoreException
import com.vpn.android.BuildConfig
import com.vpn.android.MainApplication.Companion.instance
import com.vpn.android.region.RegionConstants.KEY_PROFILE_VPN_IP
import com.vpn.base.vstore.VstoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import com.github.shadowsocks.aidl.TrafficStats as TrafficStatsSs
import com.github.shadowsocks.imsvc.connection.ConnectedTo as ImsvcConnectedTo

object IMSDK {
    internal lateinit var app: IIMSDKApplication
    internal val ssServiceConnection = ShadowsocksConnection(true)
    private val connectedServerServiceConnection =
        ConnectedServerServiceConnection(this::connectedServerServiceConnection_onBinderDied)

    val trafficStats: LiveData<TrafficStats> get() = trafficStatsLiveDataPrivate
    internal val trafficStatsLiveDataPrivate = MutableLiveData<TrafficStats>()
    val vpnState: LiveData<VpnState> get() = vpnStateLiveDataPrivate
    internal val vpnStateLiveDataPrivate = MutableLiveData<VpnState>()
    val connectedServer: LiveData<ConnectedTo?> get() = connectedServerLiveDataPrivate
    private val connectedServerLiveDataPrivate = MutableLiveData<ConnectedTo?>()

    val servers: IServers by lazy { IServersImpl() }
    val device: IDevice by lazy { IDeviceImpl() }
    val uptimeLimit = UptimeLimit
    private val geoRestrictionPolicy = GeoRestrictionPolicyImpl(
        blockChina = true,
        blockCountriesSanctionedByUS = true
    )
    val isVpnAvailable: Boolean
        get() = !geoRestrictionPolicy.isGeoRestricted() || BuildConfig.DEBUG
    private val trafficStatsReceiver = TrafficStatsReceiver()
    private val trafficStatsReceiverRegistered = AtomicBoolean()
    private val currentProcessName: String
        @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Application.getProcessName()
        } else {
            val clazz = Class.forName("android.app.ActivityThread")
            val method = clazz.getDeclaredMethod("currentProcessName")
            method.invoke(null) as String
        }
    private val isOnMainProcess: Boolean get() = currentProcessName == app.applicationId
    private val callbackImpl = ShadowsocksConnectionCallbackImpl2()

    fun init(app: IIMSDKApplication) {
        this.app = app
        if (!isVpnAvailable) return
        try {
            // `Core.imApp = app` doesn't compile, so we use reflection to do it.
            Core::class.java
                .getDeclaredMethod("setImApp", IIMSDKApplication::class.java)
                .invoke(Core, app)
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
        }
        Core.init(app.app, app.configureClass)
        Core.updateNotificationChannels()
        uptimeLimit.init()
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                ImsvcService.startService(IMSDK.app.app)
            }
        } catch (ignore: IllegalStateException) {
            /**
             * Starting from Android 12, starting services in the background may not allowed.
             * This exception can be safely ignored because this function is designed to invoke at
             * `Application.onCreate()` while no visible UI is present.
             */
        }
        connectedServerServiceConnection.bindService(app.app)
        ssServiceConnection.connect(app.app, callbackImpl)
        // 设置带宽监听超时，使 trafficUpdated 回调生效
        ssServiceConnection.bandwidthTimeout = 1000L // 1秒更新一次
        if (trafficStatsReceiverRegistered.compareAndSet(false, true)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                app.app.registerReceiver(trafficStatsReceiver, TrafficStatsReceiver.filter, Context.RECEIVER_EXPORTED)
            } else {
                app.app.registerReceiver(trafficStatsReceiver, TrafficStatsReceiver.filter)
            }
        }

        updateConnectedServer()
    }

    /**
     * Invoke after the UI is visible or the Application is at foreground.
     */
    fun onForeground() {
        ImsvcService.startService(app.app)
    }

    private fun updateConnectedServer() = with(app as CoroutineScope) {
        launch(Dispatchers.IO) {
            val svcConn = connectedServerServiceConnection
            if (!svcConn.waitServiceConnection()) return@launch
            val connectedTo = svcConn.pingOrNull()?.getConnectedTo() ?: return@launch
            connectedServerLiveDataPrivate.postValue(connectedTo)
        }
    }

    /**
     * Connect to Shadowsocks server.
     *
     * @param name To display in system notification.
     * @param host Server IP address or host/domain name.
     * @param port The port that destination server listening on.
     * @param password Hmm...I wonder how it could be used.
     * @param packageNames To exclude from being proxied.
     * @param enableIPv6Support Enable IPv6 support.
     */
    @Throws(IMSDKRuntimeException::class)
    private suspend fun connect(
        name: String, host: String, port: Int,
        password: String, packageNames: List<String>,
        bypassDomain: String?,
        enableIPv6Support: Boolean = false
    ) {
        if (!isVpnAvailable) return
        VstoreManager.getInstance(instance).encode(false, KEY_PROFILE_VPN_IP, host)
        ShadowsocksServiceStarter(name, host, port, password, packageNames, enableIPv6Support, bypassDomain).run()
    }

    private val DEFAULT_ID = "Default"

    @Throws(IMSDKRuntimeException::class)
    internal suspend fun connectToBest(
        response: FetchResponse,
        packageNames: List<String>,
        bypassDomain: String?,
        enableIPv6Support: Boolean = false
    ): ConnectedTo = coroutineScope {
        val serverList = mutableListOf<FetchResponse.Host>()
        response.serverZones?.forEach { serverZone ->
            serverZone.hosts?.let { serverList.addAll(it) }
        }
        val bestHost = ServerPing(serverList, DEFAULT_ID).startTest(null) ?: throw ServerUnreachableException()
        connectToHost(this, bestHost, DEFAULT_ID, DEFAULT_ID, DEFAULT_ID, packageNames, bypassDomain, enableIPv6Support)
    }

    @Throws(IMSDKRuntimeException::class)
    internal suspend fun connectToServerZone(
        response: FetchResponse,
        id: String,
        packageNames: List<String>,
        bypassDomain: String?,
        enableIPv6Support: Boolean = false
    ): ConnectedTo = coroutineScope {
        val serverZone = response.serverZones?.firstOrNull { it.id == id }
            ?: throw ServerZoneNotFoundException(id)
        val bestHost = ServerPing(serverZone.hosts, DEFAULT_ID).startTest(null) ?: throw ServerUnreachableException()
        connectToHost(this, bestHost, DEFAULT_ID, DEFAULT_ID, DEFAULT_ID, packageNames, bypassDomain, enableIPv6Support)
    }

    @Throws(IMSDKRuntimeException::class)
    private suspend fun connectToHost(
        scope: CoroutineScope,
        host: FetchResponse.Host,
        zoneId: String,
        city: String,
        country: String,
        packageNames: List<String>,
        bypassDomain: String?,
        enableIPv6Support: Boolean = false
    ): ConnectedTo = with(scope) {
        val city = city.let { if (it.isNullOrEmpty()) DEFAULT_ID else it }
        val name = "$city, $country"
        connect(name, host.host!!, host.port!!, host.password!!, packageNames, bypassDomain, enableIPv6Support)

        ImsvcService.startService(app.app, true)
        val connectedTo = ConnectedTo(zoneId, country, host)
        tryIgnoreException { connectedServerServiceConnection.setConnectedTo(connectedTo) }
        tryIgnoreException { connectedServerLiveDataPrivate.postValue(connectedTo) }

        connectedTo
    }

    private fun connectedServerServiceConnection_onBinderDied() {
        with(app as CoroutineScope) {
            launch {
                delay(3000) // Retry binding service after 3 seconds.
                tryIgnoreException { connectedServerServiceConnection.bindService(app.app) }
            }
        }
    }

    interface WithResponseBuilder {
        fun bypassPackageNames(packages: List<String>): WithResponseBuilder
        fun bypassDomain(domain: String): WithResponseBuilder
        fun toBest(): WithResponseBuilder
        fun toServerZone(id: String): WithResponseBuilder
        fun enableIPv6Support(enable: Boolean = true): WithResponseBuilder

        /**
         * @throws IMSDKRuntimeException Indicates any failure when connecting to a VPN server.
         */
        @Throws(IMSDKRuntimeException::class)
        suspend fun connect(): ConnectedTo

        class ConnectedTo(
            val zoneId: String? = null,
            val country: String? = null,
            val host: FetchResponse.Host? = null
        ) {
            fun toImsvcOne(): ImsvcConnectedTo = ImsvcConnectedTo(zoneId, country)

            companion object {
                fun fromImsvcOne(connectedTo: ImsvcConnectedTo): ConnectedTo =
                    ConnectedTo(connectedTo.zoneId, connectedTo.country)
            }
        }
    }

    fun withResponse(response: FetchResponse): WithResponseBuilder =
        WithResponseBuilderImpl(response)

    /**
     * Disconnect from Shadowsocks server
     */
    suspend fun disconnect() {
        Core.stopService()
        ImsvcService.stopForeground(app.app)
        // TODO: Block until service really stopped instead of delaying.
        delay(1000)
        tryIgnoreException { connectedServerServiceConnection.setConnectedTo(null) }
        tryIgnoreException { connectedServerLiveDataPrivate.postValue(null) }
    }

    enum class VpnState(val canStop: Boolean = false) {
        /**
         * Idle state is only used by UI and will never be returned by BaseService.
         */
        Idle,
        Connecting(true),
        Connected(true),
        Stopping,
        Stopped;

        companion object {
            internal fun fromTheSsOne(state: BaseService.State): VpnState =
                values().first { it.ordinal == state.ordinal }
        }
    }
}

private class ShadowsocksConnectionCallbackImpl2 : ShadowsocksConnection.Callback {
    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        val state2 = VpnState.fromTheSsOne(state)
        IMSDK.vpnStateLiveDataPrivate.postValue(state2)
    }

    override fun trafficUpdated(profileId: Long, stats: TrafficStatsSs) {
        // 将 TrafficStatsSs 转换为 TrafficStats 并更新 LiveData
        val trafficStats = TrafficStats.fromTheSsOne(stats)
        IMSDK.trafficStatsLiveDataPrivate.postValue(trafficStats)
    }

    override fun onServiceConnected(service: IShadowsocksService) {
        val state = try {
            BaseService.State.values()[service.state]
        } catch (_: RemoteException) {
            BaseService.State.Idle
        }
        val state2 = VpnState.fromTheSsOne(state)
        IMSDK.vpnStateLiveDataPrivate.postValue(state2)
    }

    override fun onServiceDisconnected() {
        IMSDK.app.onServiceDisconnected()
        IMSDK.vpnStateLiveDataPrivate.postValue(VpnState.Stopping)
        IMSDK.vpnStateLiveDataPrivate.postValue(VpnState.Stopped)
    }

    override fun onBinderDied() {
        IMSDK.app.onBinderDied()
        tryIgnoreException { IMSDK.ssServiceConnection.disconnect(IMSDK.app.app) }
        IMSDK.ssServiceConnection.connect(IMSDK.app.app, this)
    }
}

private class TrafficStatsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val stats: TrafficStatsSs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_STATS, TrafficStatsSs::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_STATS)
        } ?: return
        IMSDK.trafficStatsLiveDataPrivate.postValue(TrafficStats.fromTheSsOne(stats))
    }

    companion object {
        val filter = IntentFilter(Action.IM_TRAFFIC_STATS)
        private const val EXTRA_STATS = "stats"
    }
}

internal class WithResponseBuilderImpl(
    private val response: FetchResponse
) : IMSDK.WithResponseBuilder {

    private var packages: MutableList<String> = arrayListOf()
    private var bypassDomain: String? = null
    private var whereToConnect = PreferredGeolocation.BestOfOverall
    private var preferredZoneId: String? = null
    private var isEnableIPv6Support: Boolean = false

    override fun bypassPackageNames(packages: List<String>): IMSDK.WithResponseBuilder {
        this.packages.addAll(packages)
        return this
    }

    override fun bypassDomain(domain: String): IMSDK.WithResponseBuilder {
        this.bypassDomain = domain
        return this
    }

    override fun toBest(): IMSDK.WithResponseBuilder {
        whereToConnect = PreferredGeolocation.BestOfOverall
        return this
    }

    override fun toServerZone(id: String): IMSDK.WithResponseBuilder {
        whereToConnect = PreferredGeolocation.BestInServerZone
        preferredZoneId = id
        return this
    }

    override fun enableIPv6Support(enable: Boolean): IMSDK.WithResponseBuilder {
        isEnableIPv6Support = enable
        return this
    }

    override suspend fun connect(): ConnectedTo = when (whereToConnect) {
        PreferredGeolocation.BestOfOverall -> {
            IMSDK.connectToBest(response, packages, bypassDomain, isEnableIPv6Support)
        }

        PreferredGeolocation.BestInServerZone -> {
            IMSDK.connectToServerZone(response, preferredZoneId!!, packages, bypassDomain, isEnableIPv6Support)
        }
    }

    companion object {
        private enum class PreferredGeolocation {
            BestOfOverall, BestInServerZone
        }
    }
}
