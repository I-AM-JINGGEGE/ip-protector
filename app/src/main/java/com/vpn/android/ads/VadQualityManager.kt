package com.vpn.android.ads

import ai.datatower.ad.AdPlatform
import ai.datatower.analytics.DTAnalytics
import android.content.Context
import android.os.SystemClock
import com.vpn.android.ads.constant.AdFormat
import com.vpn.android.base.utils.LogUtils
import org.json.JSONObject

class VadQualityManager private constructor(appContext: Context) {
    private var mLeaveApplicationTS: Long = 0
    private var mAppForegroundedTS: Long = 0

    private var mAdObjectHashCode: Int = 0
        get() = field

    private var mLoadedTs: Long = 0
    private var mShowTs: Long = 0
    private var mClickTs: Long = 0
    private var mCloseTs: Long = 0

    private var placement: String? = null
    private var mAdTypeName: String? = null
    private var mAdPlatform: AdPlatform? = null
    private var mAdId: String? = null

    fun adLoaded(hashCode: Int, adFormat: AdFormat, adPlatform: AdPlatform, adId: String) {
        mAdObjectHashCode = hashCode
        mLoadedTs = SystemClock.elapsedRealtime()
        mAdTypeName = adFormat.name
        mAdPlatform = adPlatform
        mAdId = adId
        reportLoaded()
    }

    fun show(hashCode: Int, placementId: String) {
        placement = placementId
        mShowTs = SystemClock.elapsedRealtime()
        reportShow(hashCode)
    }

    fun showFail(hashCode: Int, placementId: String, errorCode: Int, errorMsg: String) {
        placement = placementId
        mShowTs = SystemClock.elapsedRealtime()
        reportShowFail(hashCode, errorCode, errorMsg)
    }

    fun click(hashCode: Int) {
        mClickTs = SystemClock.elapsedRealtime()
        reportClick(hashCode)
    }

    fun close(hashCode: Int) {
        mCloseTs = SystemClock.elapsedRealtime()
        reportClose(hashCode)
    }

    fun leaveApplication() {
        mLeaveApplicationTS = SystemClock.elapsedRealtime()
        reportLeaveApp()
    }

    fun appForegrounded() {
        mAppForegroundedTS = SystemClock.elapsedRealtime()
        reportBackApp()

        if (mClickTs == 0L || mLeaveApplicationTS == 0L || mAppForegroundedTS <= mLeaveApplicationTS) {
            reset()
            return
        }
    }

    fun reset() {
        mClickTs = 0L
        mLeaveApplicationTS = 0L
        mAppForegroundedTS = 0L

        mAdObjectHashCode = 0

        mLoadedTs = 0
        mShowTs = 0
        mCloseTs = 0

        placement = null
        mAdTypeName = null
        mAdPlatform = null
        mAdId = null
    }

    companion object {
        private var sVadQualityManager: VadQualityManager? = null

        const val EVENT_LOADED = "ad_behavior_loaded"
        const val EVENT_SHOW = "ad_behavior_show"
        const val EVENT_SHOW_FAIL = "ad_behavior_show_fail"
        const val EVENT_CLOSE = "ad_behavior_close"
        const val EVENT_CLICK = "ad_behavior_click"
        const val EVENT_LEAVE_APP = "ad_behavior_leave_app"
        const val EVENT_BACK_APP = "ad_behavior_back_app"

        @Synchronized
        fun getInstance(context: Context): VadQualityManager {
            if (sVadQualityManager == null) {
                sVadQualityManager = VadQualityManager(context.applicationContext)
            }
            return sVadQualityManager!!
        }
    }

    fun reportLoaded() {
        DTAnalytics.track(EVENT_LOADED, JSONObject().apply {
            put("ad_type", "$mAdTypeName")
            put("ad_platform", mAdPlatform?.name ?: "")
            put("ad_id", "$mAdId")
            put("seq", "$mAdObjectHashCode")
            put("loaded_ts", mLoadedTs)
            LogUtils.e("AdQualityReporter", "$EVENT_LOADED [${this}]")
        })
    }

    fun reportShow(adObjectHashCode: Int) {
        DTAnalytics.track(EVENT_SHOW, JSONObject().apply {
            put("placement", "$placement")
            put("ad_type", "$mAdTypeName")
            put("ad_platform", mAdPlatform?.name ?: "")
            put("ad_id", "$mAdId")
            put("seq", "$mAdObjectHashCode")
            put("loaded_ts", mLoadedTs)
            put("show_ts", mShowTs)
            put("show_gap", (mShowTs - mLoadedTs))
            if (mAdObjectHashCode != adObjectHashCode) {
                put("diff_seq", "$adObjectHashCode")
            }
            put("loaded_time_valid", mLoadedTs != 0L)
            LogUtils.e("AdQualityReporter", "$EVENT_SHOW [${this}]")
        })
    }

    fun reportShowFail(adObjectHashCode: Int, errorCode: Int, errorMsg: String) {
        DTAnalytics.track(EVENT_SHOW_FAIL, JSONObject().apply {
            put("placement", "$placement")
            put("error_code", errorCode)
            put("error_message", errorMsg)
            put("ad_type", "$mAdTypeName")
            put("ad_platform", mAdPlatform?.name ?: "")
            put("ad_id", "$mAdId")
            put("seq", "$mAdObjectHashCode")
            put("loaded_ts", mLoadedTs)
            put("show_ts", mShowTs)
            put("show_gap", (mShowTs - mLoadedTs))
            if (mAdObjectHashCode != adObjectHashCode) {
                put("diff_seq", "$adObjectHashCode")
            }
            put("loaded_time_valid", mLoadedTs != 0L)
            LogUtils.e("AdQualityReporter", "$EVENT_SHOW_FAIL [${this}]")
        })
    }

    fun reportClose(adObjectHashCode: Int) {
        DTAnalytics.track(EVENT_CLOSE, JSONObject().apply {
            put("placement", "$placement")
            put("ad_type", "$mAdTypeName")
            put("ad_platform", mAdPlatform?.name ?: "")
            put("ad_id", "$mAdId")
            put("seq", "$mAdObjectHashCode")
            put("loaded_ts", mLoadedTs)
            put("show_ts", mShowTs)
            put("show_gap", (mShowTs - mLoadedTs))
            put("close_ts", mCloseTs)
            put("close_gap", (mCloseTs - mShowTs))
            if (mAdObjectHashCode != adObjectHashCode) {
                put("diff_seq", "$adObjectHashCode")
            }
            put("loaded_time_valid", mLoadedTs != 0L)
            put("show_time_valid", mShowTs != 0L)
            LogUtils.e("AdQualityReporter", "$EVENT_CLOSE [${this}]")
        })
    }

    fun reportClick(adObjectHashCode: Int) {
        mClickTs = SystemClock.elapsedRealtime()
        DTAnalytics.track(EVENT_CLICK, JSONObject().apply {
            put("placement", "$placement")
            put("ad_type", "$mAdTypeName")
            put("ad_platform", mAdPlatform?.name ?: "")
            put("ad_id", "$mAdId")
            put("seq", "$mAdObjectHashCode")
            put("loaded_ts", mLoadedTs)
            put("show_ts", mShowTs)
            put("show_gap", (mShowTs - mLoadedTs))
            put("click_ts", mClickTs)
            put("click_gap", (mClickTs - mShowTs))
            if (mAdObjectHashCode != adObjectHashCode) {
                put("diff_seq", "$adObjectHashCode")
            }
            put("loaded_time_valid", mLoadedTs != 0L)
            put("show_time_valid", mShowTs != 0L)
            LogUtils.e("AdQualityReporter", "$EVENT_CLICK [${this}]")
        })
    }

    fun reportBackApp() {
        if (mLoadedTs == null || mShowTs == null || mClickTs == null) {
            return
        }
        DTAnalytics.track(EVENT_BACK_APP, JSONObject().apply {
            put("placement", "$placement")
            put("ad_type", "$mAdTypeName")
            put("ad_platform", mAdPlatform?.name ?: "")
            put("ad_id", "$mAdId")
            put("seq", "$mAdObjectHashCode")
            put("loaded_ts", mLoadedTs)
            put("show_ts", mShowTs)
            put("show_gap", (mShowTs - mLoadedTs))
            put("click_ts", mClickTs)
            put("click_gap", (mClickTs - mShowTs))
            put("back_ts", mAppForegroundedTS)
            put("back_gap", (mAppForegroundedTS - mClickTs))
            LogUtils.e("AdQualityReporter", "$EVENT_BACK_APP [${this}]")
        })
    }

    fun reportLeaveApp() {
        if (mLoadedTs == null || mShowTs == null || mClickTs == null) {
            return
        }
        DTAnalytics.track(EVENT_LEAVE_APP, JSONObject().apply {
            put("placement", "$placement")
            put("ad_type", "$mAdTypeName")
            put("ad_platform", mAdPlatform?.name ?: "")
            put("ad_id", "$mAdId")
            put("seq", "$mAdObjectHashCode")
            put("loaded_ts", mLoadedTs)
            put("show_ts", mShowTs)
            put("show_gap", (mShowTs - mLoadedTs))
            put("click_ts", mClickTs)
            put("click_gap", (mClickTs - mShowTs))
            put("leave_ts", mLeaveApplicationTS)
            put("leave_gap", (mLeaveApplicationTS - mClickTs))
            LogUtils.e("AdQualityReporter", "$EVENT_LEAVE_APP [${this}]")
        })
    }
}