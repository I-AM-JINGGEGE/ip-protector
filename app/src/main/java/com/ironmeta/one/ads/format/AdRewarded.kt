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
import com.roiquery.ad.AdPlatform
import com.roiquery.ad.AdType
import com.roiquery.ad.DTAdReport

class AdRewarded(var adId: String) {
    private var mRewardedAd: RewardedAd? = null
    var callBack: AdLoadListener? = null
    private var isLoadingAd: Boolean = false
    private var mAdShowListener: AdShowListener? = null
    private var seq = DTAdReport.generateUUID()
    private var placementId: String? = null

    fun loadAd(listener: AdLoadListener?) {
        if (isLoadingAd) {
            return
        }
        if (mRewardedAd != null) {
            listener?.onAdLoaded()
            return
        }
        callBack = listener
        isLoadingAd = true

        var adRequest = AdRequest.Builder().build()
        RewardedAd.load(MainApplication.instance.applicationContext, adId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                isLoadingAd = false
                mRewardedAd = null
                callBack?.onFailure(adError.code, adError.message)
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
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
                DTAdReport.reportClick(adId, AdType.REWARDED, AdPlatform.ADMOB, placementId ?: "", seq)
                DTAdReport.reportConversionByClick(adId, AdType.REWARDED, AdPlatform.ADMOB, placementId ?: "", seq)
            }

            override fun onAdDismissedFullScreenContent() {
                mRewardedAd = null
                mAdShowListener?.onAdClosed()
                DTAdReport.reportClose(adId, AdType.REWARDED, AdPlatform.ADMOB, placementId ?: "", seq)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                mRewardedAd = null
                mAdShowListener?.onAdFailToShow(adError.code, adError.message)
                DTAdReport.reportShowFailed(adId, AdType.REWARDED, AdPlatform.ADMOB, placementId ?: "", seq, adError.code, adError.message)
            }

            override fun onAdImpression() {}

            override fun onAdShowedFullScreenContent() {
                mAdShowListener?.onAdShown()
                DTAdReport.reportShow(adId, AdType.REWARDED, AdPlatform.ADMOB, placementId ?: "", seq)
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
                    valueMicros.toString(),
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
        DTAdReport.reportToShow(adId, AdType.REWARDED, AdPlatform.ADMOB, placementId, seq)
    }
}