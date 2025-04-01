package com.ironmeta.one.report

import com.ironmeta.one.ads.bean.UserAdConfig
import com.ironmeta.one.ads.constant.AdFormat
import com.ironmeta.one.ads.network.IpUtil
import com.ironmeta.one.base.utils.LogUtils
import ai.datatower.analytics.DTAnalytics
import com.ironmeta.one.report.ReportConstants.Param.IP_ADDRESS
import org.json.JSONObject

object VpnReporter {

    private const val PARAM_KEY_FROM = "from"
    const val PARAM_VALUE_FROM_NOTIFICATION = "notification"
    const val PARAM_VALUE_FROM_BUTTON = "button"
    const val PARAM_VALUE_FROM_TAP_TEXT = "tap_text"
    const val PARAM_VALUE_FROM_FIX_NETWORK = "fix_network"
    const val PARAM_VALUE_FROM_SERVER_LIST = "server_list"

    fun reportToStartConnect(from: String) {
        DTAnalytics.track("vpn_to_start_connect", mutableMapOf<String, Any>().apply {
            put(PARAM_KEY_FROM, from)

            LogUtils.i("VpnReporter", "vpn_to_start_connect [${this}]")
        })
    }

    fun reportStartConnect(from: String) {
        DTAnalytics.track("vpn_start_connect", mutableMapOf<String, Any>().apply {
            put(PARAM_KEY_FROM, from)

            LogUtils.i("VpnReporter", "vpn_start_connect [${this}]")
        })
    }

    fun reportServerRequestStart(from: String) {
        DTAnalytics.track("servers_refresh_start", mutableMapOf<String, Any>().apply {
            put(PARAM_KEY_FROM, from)

            LogUtils.i("VpnReporter", "servers_refresh_start [${this}]")
        })
    }

    fun reportServersRefreshFinish(from:String, result: Boolean, errorCode: Int, errorMessage:String, cost: Long, areaCount: Int) {
        DTAnalytics.track("servers_refresh_finish", mutableMapOf<String, Any>().apply {
            put(PARAM_KEY_FROM, from)
            put("result", result)
            put("errorCode", errorCode)
            put("errorMessage", errorMessage)
            put("cost", cost)
            put("areaCount", areaCount)

            LogUtils.i("VpnReporter", "servers_refresh_finish [${this}]")
        })
    }

    fun reportAdLoadStart(adType: AdFormat, source: String?) {
        DTAnalytics.track("ad_load_start", JSONObject().apply {
            put("ad_type", adType.name)
            put("from", source ?: "default")
            put(IP_ADDRESS, IpUtil.getConnectedIdAddress())

            LogUtils.i("VpnReporter", "ad_load_start [${this}]")
        })
    }

    fun reportAdLoadEnd(adType: AdFormat, errorCode: Int, errorMsg: String?, success: Boolean, from: String, cost: Long) {
        DTAnalytics.track("ad_load_end", JSONObject().apply {
            put("ad_type", adType.name)
            put("error_code", errorCode)
            errorMsg?.apply {
                put("error_msg", this)
            }
            put(IP_ADDRESS, IpUtil.getConnectedIdAddress())

            put("success", success)
            put("from", from)
            put("cost", cost)

            LogUtils.i("VpnReporter", "ad_load_end [${this}]")
        })
    }

    fun reportNativeAdFailShow(errorCode: Int, errorMsg: String) {
        DTAnalytics.track("native_ad_fail_show", JSONObject().apply {
            put("error_code", errorCode)
            put("error_msg", errorMsg)

            LogUtils.i("VpnReporter", "native_ad_fail_show [${this}]")
        })
    }

    fun reportNativeAdBeAdd(location: String) {
        DTAnalytics.track("native_ad_will_add", JSONObject().apply {
            put("placement", location)

            LogUtils.i("VpnReporter", "native_ad_will_add [${this}]")
        })
    }

    fun reportInitNativeAdObserver(location: String) {
        DTAnalytics.track("native_init_observer", JSONObject().apply {
            put("placement", location)

            LogUtils.i("VpnReporter", "native_init_observer [${this}]")
        })
    }

    fun reportNativeAdObserverChange(location: String, result: Boolean, loaded: Boolean) {
        DTAnalytics.track("native_observer_change", JSONObject().apply {
            put("placement", location)
            put("result", result)
            put("loaded", loaded)

            LogUtils.i("VpnReporter", "native_observer_change [${this}]")
        })
    }

    fun reportAdConfigRequestStart() {
        DTAnalytics.track("ad_config_request_start", JSONObject().apply {

            LogUtils.i("VpnReporter", "ad_config_request_start [${this}]")
        })
    }

    fun reportAdConfigRequestEnd(
        errorCode: Int,
        errorMsg: String?,
        success: Boolean,
        adConfigBean: UserAdConfig?,
        duration: Long
    ) {
        DTAnalytics.track("ad_config_request_end", JSONObject().apply {
            errorMsg?.apply {
                put("error_msg", this)
            }
            adConfigBean?.apply {
                put(
                    "interstitial_count",
                    this.adConfig?.adUnitSetAfterConnect?.interstitial?.size ?: 0
                )
                put("native_count", this.adConfig?.adUnitSetAfterConnect?.native?.size ?: 0)
            }
            put("error_code", errorCode)

            put("success", success)
            put("duration", duration)

            LogUtils.i("VpnReporter", "ad_config_request_end [${this}]")
        })
    }
}