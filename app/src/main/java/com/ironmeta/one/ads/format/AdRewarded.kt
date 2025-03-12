package com.ironmeta.one.ads.format

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.ironmeta.one.MainApplication
import com.ironmeta.one.ads.constant.AdFormat
import com.ironmeta.one.ads.network.IpUtil
import com.ironmeta.one.ads.proxy.AdLoadListener
import com.ironmeta.one.ads.proxy.AdShowListener
import com.ironmeta.one.ads.proxy.RewardedAdShowListener
import com.ironmeta.one.report.AdReport
import com.ironmeta.one.report.ReportConstants
import com.ironmeta.one.report.VpnReporter
import ai.datatower.ad.AdPlatform
import ai.datatower.ad.AdType
import ai.datatower.ad.DTAdReport
import android.text.TextUtils

class AdRewarded(var adId: String) {
    private var mRewardedAd: RewardedAd? = null
    var callBack: AdLoadListener? = null
    private var isLoadingAd: Boolean = false
    private var mAdShowListener: AdShowListener? = null
    private var seq = DTAdReport.generateUUID()
    private var placementId: String? = null

    fun loadAd(listener: AdLoadListener?, from: String) {
        val start = System.currentTimeMillis()
        if (isLoadingAd) {
            return
        }
        if (mRewardedAd != null) {
            listener?.onAdLoaded()
            return
        }
        callBack = listener
        isLoadingAd = true
        VpnReporter.reportAdLoadStart(AdFormat.REWARDED, from)
        var adRequest = AdRequest.Builder().build()
        RewardedAd.load(MainApplication.instance.applicationContext, adId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                VpnReporter.reportAdLoadEnd(AdFormat.REWARDED, adError.code, adError.message, false, from, System.currentTimeMillis() - start)
                isLoadingAd = false
                mRewardedAd = null
                callBack?.onFailure(adError.code, adError.message)
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
                VpnReporter.reportAdLoadEnd(AdFormat.REWARDED, 0, "", true, from, System.currentTimeMillis() - start)
                mRewardedAd = rewardedAd
                setAdShowCallback()
                isLoadingAd = false
                callBack?.onAdLoaded()
            }
        })
    }

    private fun setAdShowCallback() {
        mRewardedAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdClicked() {
                mAdShowListener?.onAdClicked()
                val apAddress = IpUtil.getConnectedIdAddress()
                DTAdReport.reportClick(adId, AdType.REWARDED, AdPlatform.ADMOB, placementId ?: "", seq, mutableMapOf<String, Any>().apply {
                    if (!TextUtils.isEmpty(apAddress)) {
                        put(ReportConstants.Param.IP_ADDRESS, apAddress)
                    }
                })
                DTAdReport.reportConversionByClick(adId, AdType.REWARDED, AdPlatform.ADMOB, placementId ?: "", seq, mutableMapOf<String, Any>().apply {
                    if (!TextUtils.isEmpty(apAddress)) {
                        put(ReportConstants.Param.IP_ADDRESS, apAddress)
                    }
                })
            }

            override fun onAdDismissedFullScreenContent() {
                mRewardedAd = null
                mAdShowListener?.onAdClosed()
                val apAddress = IpUtil.getConnectedIdAddress()
                DTAdReport.reportClose(adId, AdType.REWARDED, AdPlatform.ADMOB, placementId ?: "", seq, mutableMapOf<String, Any>().apply {
                    if (!TextUtils.isEmpty(apAddress)) {
                        put(ReportConstants.Param.IP_ADDRESS, apAddress)
                    }
                })
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                mRewardedAd = null
                mAdShowListener?.onAdFailToShow(adError.code, adError.message)
                val apAddress = IpUtil.getConnectedIdAddress()
                DTAdReport.reportShowFailed(adId, AdType.REWARDED, AdPlatform.ADMOB, placementId ?: "", seq, adError.code, adError.message, mutableMapOf<String, Any>().apply {
                    if (!TextUtils.isEmpty(apAddress)) {
                        put(ReportConstants.Param.IP_ADDRESS, apAddress)
                    }
                })
            }

            override fun onAdImpression() {}

            override fun onAdShowedFullScreenContent() {
                mAdShowListener?.onAdShown()
                val apAddress = IpUtil.getConnectedIdAddress()
                DTAdReport.reportShow(adId, AdType.REWARDED, AdPlatform.ADMOB, placementId ?: "", seq, mutableMapOf<String, Any>().apply {
                    if (!TextUtils.isEmpty(apAddress)) {
                        put(ReportConstants.Param.IP_ADDRESS, apAddress)
                    }
                })
            }
        }

        mRewardedAd?.setOnPaidEventListener { adValue ->
            // valueMicros:390, currencyCode:USD, precisionType:3
            adValue.apply {
                DTAdReport.reportPaid(
                    adId,
                    AdType.REWARDED,
                    AdPlatform.ADMOB,
                    placementId ?: "",
                    seq,
                    valueMicros.toDouble() / 1000000,
                    currencyCode,
                    precisionType.toString(),
                    mutableMapOf<String, Any>().apply {
                        put(ReportConstants.Param.IP_ADDRESS, IpUtil.getConnectedIdAddress())
                    }
                )
                AdReport.reportAdImpressionRevenue(this, AdFormat.REWARDED, MainApplication.instance.applicationContext)
            }
        }
    }

    fun show(activity: Activity, callBack: AdShowListener?, placementId: String) {
        mAdShowListener = callBack
        this.placementId = placementId
        try {
            mRewardedAd?.show(activity) {
                if (mAdShowListener is RewardedAdShowListener) {
                    (mAdShowListener as RewardedAdShowListener)?.onRewarded()
                }
            }
        } catch (e: Exception) {}
    }

    fun isLoaded() = mRewardedAd != null


    fun destroy() {

    }

    fun logToShow(placementId: String) {
        this.placementId = placementId
        seq = DTAdReport.generateUUID()
        val apAddress = IpUtil.getConnectedIdAddress()
        DTAdReport.reportToShow(adId, AdType.REWARDED, AdPlatform.ADMOB, placementId, seq, mutableMapOf<String, Any>().apply {
            if (!TextUtils.isEmpty(apAddress)) {
                put(ReportConstants.Param.IP_ADDRESS, apAddress)
            }
        })
    }
}