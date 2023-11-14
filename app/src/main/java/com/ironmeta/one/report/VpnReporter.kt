package com.ironmeta.one.report

import com.ironmeta.one.base.utils.LogUtils
import com.roiquery.analytics.DTAnalytics
import com.sdk.ssmod.IMSDK
import com.sdk.ssmod.api.http.beans.FetchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object VpnReporter {
    private const val EVENT_TO_START_CONNECT = "vpn_to_start_connect"
    private const val EVENT_START_CONNECT = "vpn_start_connect"
    private const val EVENT_STOP_VPN_WHEN_TO_CONNECT = "stop_vpn_when_to_connect"
    private const val EVENT_VPN_TO_CONNECT_BEST = "vpn_to_connect_best"
    private const val EVENT_VPN_TO_CONNECT_SERVER_ZONE = "vpn_to_connect_server_zone"
    private const val EVENT_VPN_CONNECT_EXCEPTION = "vpn_connect_exception"
    private const val EVENT_CONNECTED = "vpn_connected"
    private const val EVENT_START_TEST = "vpn_start_test"
    private const val EVENT_TEST_SUCCESS = "vpn_test_success"
    private const val EVENT_TEST_FAIL = "vpn_test_fail"
    private const val EVENT_START_PING = "vpn_start_ping"
    private const val EVENT_PING_SUCCESS = "vpn_ping_success"
    private const val EVENT_PING_FAIL = "vpn_ping_fail"
    private const val EVENT_START_CONNECT_SS = "vpn_start_connect_ss"
    private const val EVENT_CONNECT_SS_SUCCESS = "vpn_connect_ss_success"
    private const val EVENT_CONNECT_SS_FAIL = "vpn_connect_ss_fail"
    private const val EVENT_FETCH_SERVER_START = "fetch_server_start"
    private const val EVENT_FETCH_SERVER_SUCCESS = "fetch_server_success"
    private const val EVENT_FETCH_SERVER_FAIL = "fetch_server_fail"
    private const val EVENT_FETCH_SERVER_LOG = "fetch_server_log"

    private const val PARAM_KEY_FROM = "from"
    private const val PARAM_KEY_FETCH_FINISHED = "fetch_finished"
    const val PARAM_VALUE_FROM_NOTIFICATION = "notification"
    const val PARAM_VALUE_FROM_BUTTON = "button"
    const val PARAM_VALUE_FROM_SLEEP_ICON = "sleep_icon"
    const val PARAM_VALUE_FROM_SPEED_TEST = "speed_test"
    const val PARAM_VALUE_FROM_FIX_NETWORK = "fix_network"
    const val PARAM_VALUE_FROM_SERVER_LIST = "server_list"

    private var from: String? = null
    private var fetchFinished: Boolean = false

    init {
        GlobalScope.launch(Dispatchers.Main) {
            observeVpnState()
        }
    }

    fun reportToStartConnect(from: String) {
        DTAnalytics.track(EVENT_TO_START_CONNECT, mutableMapOf<String, Any>().apply {
            put(PARAM_KEY_FROM, from)
        })
        LogUtils.i("VpnReporter", "report To Start Connect, from $from")
    }

    fun reportStartConnect(from: String) {
        DTAnalytics.track(EVENT_START_CONNECT, mutableMapOf<String, Any>().apply {
            put(PARAM_KEY_FROM, from)
            put(PARAM_KEY_FETCH_FINISHED, fetchFinished)
        })
        this.from = from
        LogUtils.i("VpnReporter", "report Start Connect, from $from, fetch finished $fetchFinished")
    }

    fun reportStopWhenToConnect() {
        DTAnalytics.track(EVENT_STOP_VPN_WHEN_TO_CONNECT, mutableMapOf<String, Any>().apply {
            put(PARAM_KEY_FROM, "$from")
        })
        LogUtils.i("VpnReporter", "report Stop When To Connect, from $from")
    }

    fun reportToConnectBest() {
        DTAnalytics.track(EVENT_VPN_TO_CONNECT_BEST, mutableMapOf<String, Any>().apply {
            put(PARAM_KEY_FROM, "$from")
        })
        LogUtils.i("VpnReporter", "report To Connect Best, from $from")
    }

    fun reportToConnectServerZone(serverZone: FetchResponse.ServerZone) {
        DTAnalytics.track(EVENT_VPN_TO_CONNECT_SERVER_ZONE, mutableMapOf<String, Any>().apply {
            put(PARAM_KEY_FROM, "$from")
            put("country", "${serverZone.country}")
            put("city", "${serverZone.city}")
            put("id", "${serverZone.id}")
        })
        LogUtils.i("VpnReporter", "report To Connect Server Zone, from $from")
    }

    fun reportConnectException(e: Exception) {
        DTAnalytics.track(EVENT_VPN_CONNECT_EXCEPTION, mutableMapOf<String, Any>().apply {
            put(PARAM_KEY_FROM, "$from")
            put("exception", "${e.message}")
        })
        LogUtils.e("VpnReporter", "report Connect Exception, from $from, message: ${e.message}")
    }

    private fun reportConnected(from: String) {
        DTAnalytics.track(EVENT_CONNECTED, mutableMapOf<String, Any>().apply {
            put(PARAM_KEY_FROM, from)
        })
        LogUtils.i("VpnReporter", "report Connected, from $from")
    }

    private fun observeVpnState() {
        IMSDK.vpnState.observeForever {
            when(it) {
                IMSDK.VpnState.Connected -> {
                    from?.apply {
                        reportConnected(this)
                    }
                }
            }
        }
    }

    fun reportStartTest() {
        DTAnalytics.track(EVENT_START_TEST)
        LogUtils.i("VpnReporter", "report Start Test")
    }

    fun reportTestSuccess(host: FetchResponse.Host) {
        DTAnalytics.track(EVENT_TEST_SUCCESS, mutableMapOf<String, Any>().apply {
            put("host", "${host.host}")
            put("port", "${host.port}")
        })
        LogUtils.e("VpnReporter", "report Test Success [host=${host.host},port=${host.port}]")
    }

    fun reportTestFail() {
        DTAnalytics.track(EVENT_TEST_FAIL)
        LogUtils.e("VpnReporter", "report Test Fail")
    }

    fun reportStartPing(host: FetchResponse.Host) {
        DTAnalytics.track(EVENT_START_PING, mutableMapOf<String, Any>().apply {
            put("host", "${host.host}")
            put("port", "${host.port}")
        })
        LogUtils.i("VpnReporter", "report Start Ping [host=${host.host},port=${host.port}]")
    }

    fun reportPingSuccess(host: FetchResponse.Host, cost: Long) {
        DTAnalytics.track(EVENT_PING_SUCCESS, mutableMapOf<String, Any>().apply {
            put("host", "${host.host}")
            put("port", "${host.port}")
            put("cost", "$cost")
        })
        LogUtils.e("VpnReporter", "report Ping Success [host=${host.host},port=${host.port}], cost $cost")
    }

    fun reportPingFail(host: FetchResponse.Host?, e: Exception?) {
        DTAnalytics.track(EVENT_PING_FAIL, mutableMapOf<String, Any>().apply {
            put("host", "${host?.host}")
            put("port", "${host?.port}")
            e?.let {
                put("exception", "${e.message}")
            }
        })
        LogUtils.e("VpnReporter", "report Ping Fail [host=${host?.host},port=${host?.port}], exception: ${e?.message}")
    }

    fun reportStartConnectSs(host: FetchResponse.Host) {
        DTAnalytics.track(EVENT_START_CONNECT_SS, mutableMapOf<String, Any>().apply {
            put("host", "${host.host}")
            put("port", "${host.port}")
        })
        LogUtils.i("VpnReporter", "report Start Connect Shadow Socks [host=${host.host},port=${host.port}]")
    }

    fun reportConnectSsSuccess(host: FetchResponse.Host) {
        DTAnalytics.track(EVENT_CONNECT_SS_SUCCESS, mutableMapOf<String, Any>().apply {
            put("host", "${host.host}")
            put("port", "${host.port}")
        })
        LogUtils.e("VpnReporter", "report Connect Shadow Socks Success [host=${host.host},port=${host.port}]")
    }

    fun reportConnectSsFail(host: FetchResponse.Host) {
        DTAnalytics.track(EVENT_CONNECT_SS_FAIL, mutableMapOf<String, Any>().apply {
            put("host", "${host.host}")
            put("port", "${host.port}")
        })
        LogUtils.e("VpnReporter", "report Connect Shadow Socks Fail [host=${host.host},port=${host.port}]")
    }

    fun reportFetchServersStart(from: String, vpnAvailable: Boolean) {
        DTAnalytics.track(EVENT_FETCH_SERVER_START, mutableMapOf<String, Any>().apply {
            put("from", "$from")
            put("vpn_available", "$vpnAvailable")
        })
        LogUtils.i("VpnReporter", "report fetch server start. [from=$from]")
    }

    fun reportFetchServersSuccess(from: String) {
        DTAnalytics.track(EVENT_FETCH_SERVER_SUCCESS, mutableMapOf<String, Any>().apply {
            put("from", "$from")
        })
        fetchFinished = true
        LogUtils.e("VpnReporter", "report fetch server success. [from=$from]")
    }

    fun reportFetchServersFail(from: String, error: String) {
        DTAnalytics.track(EVENT_FETCH_SERVER_FAIL, mutableMapOf<String, Any>().apply {
            put("from", "$from")
            put("error", "$error")
        })
        LogUtils.e("VpnReporter", "report fetch server fail. [from=$from, error=$error]")
    }

    fun reportFetchServersLog(from: String, message: String) {
        DTAnalytics.track(EVENT_FETCH_SERVER_LOG, mutableMapOf<String, Any>().apply {
            put("from", "$from")
            put("message", "$message")
        })
        LogUtils.e("VpnReporter", "report fetch server log. [from=$from, message=$message]")
    }
}