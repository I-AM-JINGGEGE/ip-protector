package com.vpn.android.report

import ai.datatower.ad.AdPlatform
import ai.datatower.ad.AdType
import ai.datatower.analytics.DTAnalytics
import com.vpn.android.ads.bean.UserAdConfig
import com.vpn.android.ads.network.IpUtil
import com.vpn.android.base.utils.LogUtils
import com.vpn.android.report.ReportConstants.Param.IP_ADDRESS
import org.json.JSONObject

object VpnReporter {

    private const val PARAM_KEY_FROM = "from"
    const val PARAM_VALUE_FROM_NOTIFICATION = "notification"
    const val PARAM_VALUE_FROM_BUTTON = "button"
    const val PARAM_VALUE_FROM_TAP_TEXT = "tap_text"
    const val PARAM_VALUE_FROM_FIX_NETWORK = "fix_network"
    const val PARAM_VALUE_FROM_SERVER_LIST = "server_list"
    const val PARAM_VALUE_FROM_SPEED_ICON = "speed_icon"
    const val PARAM_VALUE_FROM_REPORT_ICON = "report_icon"

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

    fun reportAdConfigRequestStart() {
        DTAnalytics.track("ad_config_request_start", JSONObject().apply {

            LogUtils.i("VpnReporter", "ad_config_request_start [${this}]")
        })
    }

    fun reportAdConfigRequestEnd(
        errorCode: Int,
        errorMsg: String?,
        result: Boolean,
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

            put("result", result)
            put("duration", duration)

            LogUtils.i("VpnReporter", "ad_config_request_end [${this}]")
        })
    }

    fun reportAdComboBehavior(id: String,
                              type: AdType,
                              platform: AdPlatform,
                              location: String,) {
        DTAnalytics.track("ad_combo_behavior", JSONObject().apply {
            put("ad_id", id)
            put("ad_type", type.name)
            put("ad_platform", platform)
            put("ad_location", location)
            put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
            LogUtils.i("VpnReporter", "ad_combo_behavior [${this}]")
        })
    }

    fun reportAdClickExceededLimit(id: String,
                              type: AdType,
                              platform: AdPlatform,
                              location: String,) {
        DTAnalytics.track("ad_click_exceeded_limit", JSONObject().apply {
            put("ad_id", id)
            put("ad_type", type.name)
            put("ad_platform", platform)
            put("ad_location", location)
            put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
            LogUtils.i("VpnReporter", "ad_click_exceeded_limit [${this}]")
        })
    }
}