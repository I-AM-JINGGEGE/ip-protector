package com.ironmeta.one.ads.format

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.ironmeta.one.ads.constant.AdFormat
import com.ironmeta.one.ads.network.IpUtil
import com.ironmeta.one.ads.proxy.AdLoadListener
import com.ironmeta.one.ads.proxy.AdShowListener
import com.ironmeta.one.report.AdReport
import com.ironmeta.one.report.ReportConstants
import com.ironmeta.one.report.VpnReporter
import ai.datatower.ad.AdPlatform
import ai.datatower.ad.AdType
import ai.datatower.ad.DTAdReport

class AdInterstitialAdmob(var adId: String, val context: Context) {
    private var mInterstitialAd: InterstitialAd? = null
    var callBack: AdLoadListener? = null
    private var isLoadingAd: Boolean = false
    private var mAdShowListener: AdShowListener? = null
    internal var seq = DTAdReport.generateUUID()
        get() = field
    private var placementId: String? = null

    fun loadAd(listener: AdLoadListener?, from: String) {
        if (isLoadingAd) {
            return
        }
        if (mInterstitialAd != null) {
            listener?.onAdLoaded()
            return
        }
        callBack = listener
        isLoadingAd = true
        VpnReporter.reportAdLoadStart(AdFormat.INTERSTITIAL, from)
        var adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context,adId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                isLoadingAd = false
                mInterstitialAd = null
                callBack?.onFailure(adError.code, adError.message)
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
                setAdShowCallback()
                isLoadingAd = false
                callBack?.onAdLoaded()
            }
        })
    }

    private var lastClickedAdSeq: String? = null

    private fun setAdShowCallback() {
        mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdClicked() {
                mAdShowListener?.onAdClicked()
                DTAdReport.reportClick(adId, AdType.INTERSTITIAL, AdPlatform.ADMOB, placementId ?: "", seq)
                DTAdReport.reportConversionByClick(adId, AdType.INTERSTITIAL, AdPlatform.ADMOB, placementId ?: "", seq)
                if (lastClickedAdSeq != seq) {
//                    AdEventLogger.logInterstitialAdClick(MainApplication.getContext())
                }
                lastClickedAdSeq = seq
            }

            override fun onAdDismissedFullScreenContent() {
                mInterstitialAd = null
                mAdShowListener?.onAdClosed()
                DTAdReport.reportClose(adId, AdType.INTERSTITIAL, AdPlatform.ADMOB, placementId ?: "", seq)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                mInterstitialAd = null
                mAdShowListener?.onAdFailToShow(adError.code, adError.message)
                DTAdReport.reportShowFailed(adId, AdType.INTERSTITIAL, AdPlatform.ADMOB, placementId ?: "", seq, adError.code, adError.message)
            }

            override fun onAdImpression() {}

            override fun onAdShowedFullScreenContent() {
                mAdShowListener?.onAdShown()
                DTAdReport.reportShow(adId, AdType.INTERSTITIAL, AdPlatform.ADMOB, placementId ?: "", seq)
//                AdEventLogger.logInterstitialAdShow(MainApplication.getContext())
            }
        }

        mInterstitialAd?.setOnPaidEventListener { adValue ->
            // valueMicros:390, currencyCode:USD, precisionType:3
            adValue.apply {
                DTAdReport.reportPaid(
                    adId,
                    AdType.INTERSTITIAL,
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
                AdReport.reportAdImpressionRevenue(this, AdFormat.INTERSTITIAL, context)
            }
        }
    }

    fun show(activity: Activity, callBack: AdShowListener?, placementId: String) {
        mAdShowListener = callBack
        this.placementId = placementId
        try {
            mInterstitialAd?.show(activity)
        } catch (e: Exception) {}
    }

    fun isLoaded() = mInterstitialAd != null


    fun destroy() {

    }

    fun logToShow(placementId: String) {
        this.placementId = placementId
        seq = DTAdReport.generateUUID()
        DTAdReport.reportToShow(adId, AdType.INTERSTITIAL, AdPlatform.ADMOB, placementId, seq)
    }
}