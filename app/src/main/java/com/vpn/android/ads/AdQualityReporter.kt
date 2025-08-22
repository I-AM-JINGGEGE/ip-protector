package com.vpn.android.ads

import ai.datatower.analytics.DTAnalytics
import android.os.SystemClock
import com.vpn.android.MainApplication
import com.vpn.android.ads.constant.AdFormat
import com.vpn.android.base.utils.LogUtils
import org.json.JSONObject

class AdQualityReporter() {
    private companion object {
        const val EVENT_LOADED = "ad_behavior_loaded"
        const val EVENT_SHOW = "ad_behavior_show"
        const val EVENT_CLOSE = "ad_behavior_close"
        const val EVENT_CLICK = "ad_behavior_click"
        const val EVENT_BACK_APP = "ad_behavior_back_app"
    }
    private val context = MainApplication.instance.applicationContext
    private var mAdObjectHashCode: Int? = null
        get() = field

    private var valid: Boolean = true

    private var mLoadedTs: Long? = null
    private var mShowTs: Long? = null
    private var mCloseTs: Long? = null
    private var mClickTs: Long? = null
    private var mBackTs: Long? = null

    private var placement: String? = null
    private var mAdTypeName: String? = null
    private var mAdPlatformCode: Int? = null
    private var mAdId: String? = null

    fun reportLoaded(adObjectHashCode: Int, adType: AdFormat, adPlatform: Int, adId: String) {
        mAdObjectHashCode = adObjectHashCode
        mLoadedTs = SystemClock.elapsedRealtime()
        mAdTypeName = adType.name
        mAdPlatformCode = adPlatform
        mAdId = adId
        DTAnalytics.track(EVENT_LOADED, JSONObject().apply {
            put("ad_type", "$mAdTypeName")
            put("ad_platform_code", mAdPlatformCode)
            put("ad_id", "$mAdId")
            put("seq", "$mAdObjectHashCode")
            put("loaded_ts", mLoadedTs)
            LogUtils.e("AdQualityReporter", "$EVENT_LOADED [${this}]")
        })
    }

    fun reportShow(adObjectHashCode: Int, location: String) {
        if (!valid || mAdObjectHashCode != adObjectHashCode || mLoadedTs == null) {
            return
        }
        mShowTs = SystemClock.elapsedRealtime()
        placement = location
        DTAnalytics.track(EVENT_SHOW, JSONObject().apply {
            put("placement", "$placement")
            put("ad_type", "$mAdTypeName")
            put("ad_platform_code", mAdPlatformCode)
            put("ad_id", "$mAdId")
            put("seq", "$mAdObjectHashCode")
            put("loaded_ts", mLoadedTs)
            put("show_ts", mShowTs)
            put("show_gap", (mShowTs!! - mLoadedTs!!))
            LogUtils.e("AdQualityReporter", "$EVENT_SHOW [${this}]")
        })
    }

    fun reportClose(adObjectHashCode: Int) {
        if (!valid || mAdObjectHashCode != adObjectHashCode || mLoadedTs == null || mShowTs == null) {
            return
        }
        mCloseTs = SystemClock.elapsedRealtime()
        DTAnalytics.track(EVENT_CLOSE, JSONObject().apply {
            put("placement", "$placement")
            put("ad_type", "$mAdTypeName")
            put("ad_platform_code", mAdPlatformCode)
            put("ad_id", "$mAdId")
            put("seq", "$mAdObjectHashCode")
            put("loaded_ts", mLoadedTs)
            put("show_ts", mShowTs)
            put("show_gap", (mShowTs!! - mLoadedTs!!))
            put("close_ts", mCloseTs)
            put("close_gap", (mCloseTs!! - mShowTs!!))
            LogUtils.e("AdQualityReporter", "$EVENT_CLOSE [${this}]")
        })
    }

    fun reportClick(adObjectHashCode: Int) {
        if (!valid || mAdObjectHashCode != adObjectHashCode || mLoadedTs == null || mShowTs == null) {
            return
        }
        mClickTs = SystemClock.elapsedRealtime()
        DTAnalytics.track(EVENT_CLICK, JSONObject().apply {
            put("placement", "$placement")
            put("ad_type", "$mAdTypeName")
            put("ad_platform_code", mAdPlatformCode)
            put("ad_id", "$mAdId")
            put("seq", "$mAdObjectHashCode")
            put("loaded_ts", mLoadedTs)
            put("show_ts", mShowTs)
            put("show_gap", (mShowTs!! - mLoadedTs!!))
            put("click_ts", mClickTs)
            put("click_gap", (mClickTs!! - mShowTs!!))
            LogUtils.e("AdQualityReporter", "$EVENT_CLICK [${this}]")
        })

        VadQualityManager.getInstance(context).clickAd(this@AdQualityReporter)
    }

    fun reportBackApp() {
        if (!valid || mLoadedTs == null || mShowTs == null || mClickTs == null) {
            return
        }
        mBackTs = SystemClock.elapsedRealtime()
        DTAnalytics.track(EVENT_BACK_APP, JSONObject().apply {
            put("placement", "$placement")
            put("ad_type", "$mAdTypeName")
            put("ad_platform_code", mAdPlatformCode)
            put("ad_id", "$mAdId")
            put("seq", "$mAdObjectHashCode")
            put("loaded_ts", mLoadedTs)
            put("show_ts", mShowTs)
            put("show_gap", (mShowTs!! - mLoadedTs!!))
            put("click_ts", mClickTs)
            put("click_gap", (mClickTs!! - mShowTs!!))
            put("back_ts", mBackTs)
            put("back_gap", (mBackTs!! - mClickTs!!))
            LogUtils.e("AdQualityReporter", "$EVENT_BACK_APP [${this}]")
        })
        if (mAdTypeName == AdFormat.NATIVE.name) {
            AdPresenterWrapper.getInstance().apply {
                markNativeAdShown()
                loadNativeAd(null, "ad clicked")
            }
        }
    }

    fun reset() {
        mAdObjectHashCode = null

        valid = true

        mLoadedTs = null
        mShowTs = null
        mCloseTs = null
        mClickTs = null
        mBackTs = null

        placement = null
        mAdTypeName = null
        mAdPlatformCode = null
        mAdId = null
    }
}