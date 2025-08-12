package com.vpn.android.ads

import android.content.Context
import com.vpn.base.vstore.VstoreManager
import com.vpn.android.constants.KvStoreConstants
import com.vpn.android.vlog.VlogManager

object AdEventLogger {
    @Deprecated("no longer use")
    private fun logInterstitialAdShow(context: Context) {
        val appContext = context.applicationContext
        val times = getInterstitialAdShowTimes(context)
        VstoreManager.getInstance(appContext)
            .encode(true, KvStoreConstants.KEY_INTERSTITIAL_AD_SHOW_TIMES, times + 1)
        if (times in 3..9) {
            VlogManager.getInstance(context).logEvent(generateAdShowEventName(times + 1), null)
        }
    }

    private fun getInterstitialAdShowTimes(context: Context): Int {
        val appContext = context.applicationContext
        return VstoreManager.getInstance(appContext)
            .decode(true, KvStoreConstants.KEY_INTERSTITIAL_AD_SHOW_TIMES, 0)
    }

    @Deprecated("no longer use")
    private fun logInterstitialAdClick(context: Context) {
        val appContext = context.applicationContext
        val times = getInterstitialAdClickTimes(context)
        VstoreManager.getInstance(appContext)
            .encode(true, KvStoreConstants.KEY_INTERSTITIAL_AD_CLICK_TIMES, times + 1)
        if (times in 1..9) {
            VlogManager.getInstance(context).logEvent(generateAdClickEventName(times + 1), null)
        }
    }

    private fun getInterstitialAdClickTimes(context: Context): Int {
        val appContext = context.applicationContext
        return VstoreManager.getInstance(appContext)
            .decode(true, KvStoreConstants.KEY_INTERSTITIAL_AD_CLICK_TIMES, 0)
    }

    private fun generateAdShowEventName(showTimes: Int): String {
        return "interstitial_show_${showTimes}_times"
    }

    private fun generateAdClickEventName(clickTimes: Int): String {
        return "interstitial_click_${clickTimes}_times"
    }
}