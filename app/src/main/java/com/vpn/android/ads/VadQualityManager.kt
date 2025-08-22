package com.vpn.android.ads

import android.content.Context
import android.os.SystemClock

class VadQualityManager private constructor(appContext: Context) {
    private var mClickTS: Long = 0
    private var mLeftApplicationTS: Long = 0
    private var mAppForegroundedTS: Long = 0

    private var adQualityReporter: AdQualityReporter? = null

    fun clickAd(adQualityReporter: AdQualityReporter?) {
        mClickTS = SystemClock.elapsedRealtime()
        this.adQualityReporter = adQualityReporter
    }

    fun leftApplication() {
        mLeftApplicationTS = SystemClock.elapsedRealtime()
    }

    fun appForegrounded() {
        mAppForegroundedTS = SystemClock.elapsedRealtime()

        if (mClickTS == 0L || adQualityReporter == null || mLeftApplicationTS == 0L || mAppForegroundedTS <= mLeftApplicationTS) {
            reset()
            return
        }
        adQualityReporter!!.reportBackApp()
    }

    private fun reset() {
        mClickTS = 0L
        mLeftApplicationTS = 0L
        mAppForegroundedTS = 0L
        adQualityReporter = null
    }

    companion object {
        private var sVadQualityManager: VadQualityManager? = null

        @Synchronized
        fun getInstance(context: Context): VadQualityManager {
            if (sVadQualityManager == null) {
                sVadQualityManager = VadQualityManager(context.applicationContext)
            }
            return sVadQualityManager!!
        }
    }
}