package com.ironmeta.one.ads.format

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.ironmeta.one.ads.constant.AdFormat
import com.ironmeta.one.ads.network.IpUtil
import com.ironmeta.one.ads.proxy.AdLoadListener
import com.ironmeta.one.ads.proxy.AdShowListener
import com.ironmeta.one.report.AdReport
import ai.datatower.ad.AdPlatform
import ai.datatower.ad.AdType
import ai.datatower.ad.DTAdReport
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AFInAppEventType
import com.appsflyer.AppsFlyerLib
import com.ironmeta.one.base.utils.LogUtils
import com.ironmeta.one.report.ReportConstants.Param.IP_ADDRESS
import com.ironmeta.one.report.VpnReporter

class AdAppOpenAdmob(var adId: String, val context: Context) {
    private var mAppOpenAd: AppOpenAd? = null
    var callBack: AdLoadListener? = null
    private var isLoadingAd: Boolean = false
    private var mAdShowListener: AdShowListener? = null
    private var seq = DTAdReport.generateUUID()
    private var placementId: String? = null

    fun loadAd(listener: AdLoadListener?, from: String) {
        if (isLoadingAd) {
            return
        }
        if (mAppOpenAd != null) {
            listener?.onAdLoaded()
            return
        }
        callBack = listener
        isLoadingAd = true
        VpnReporter.reportAdLoadStart(AdFormat.APP_OPEN, from)
        var adRequest = AdRequest.Builder().build()
        AppOpenAd.load(context, adId, adRequest, object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                isLoadingAd = false
                mAppOpenAd = null
                callBack?.onFailure(adError.code, adError.message)
            }

            override fun onAdLoaded(appOpenAd: AppOpenAd) {
                mAppOpenAd = appOpenAd
                setAdShowCallback()
                isLoadingAd = false
                callBack?.onAdLoaded()
            }
        })
    }

    private fun setAdShowCallback() {
        mAppOpenAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdClicked() {
                mAdShowListener?.onAdClicked()
                DTAdReport.reportClick(adId, AdType.INTERSTITIAL, AdPlatform.ADMOB, placementId ?: "", seq, mutableMapOf<String, Any>().apply {
                    put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
                    LogUtils.i("VpnReporter", "app open - click [${this}]")
                })
                DTAdReport.reportConversionByClick(adId, AdType.INTERSTITIAL, AdPlatform.ADMOB, placementId ?: "", seq, mutableMapOf<String, Any>().apply {
                    put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
                    LogUtils.i("VpnReporter", "app open - conversion [${this}]")
                })
            }

            override fun onAdDismissedFullScreenContent() {
                mAppOpenAd = null
                mAdShowListener?.onAdClosed()
                DTAdReport.reportClose(adId, AdType.INTERSTITIAL, AdPlatform.ADMOB, placementId ?: "", seq, mutableMapOf<String, Any>().apply {
                    put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
                    LogUtils.i("VpnReporter", "app open - close [${this}]")
                })
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                mAppOpenAd = null
                mAdShowListener?.onAdFailToShow(adError.code, adError.message)
                DTAdReport.reportShowFailed(adId, AdType.INTERSTITIAL, AdPlatform.ADMOB, placementId ?: "", seq, adError.code, adError.message, mutableMapOf<String, Any>().apply {
                    put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
                    LogUtils.i("VpnReporter", "app open - fail show [${this}]")
                })
            }

            override fun onAdImpression() {}

            override fun onAdShowedFullScreenContent() {
                mAdShowListener?.onAdShown()
                DTAdReport.reportShow(adId, AdType.INTERSTITIAL, AdPlatform.ADMOB, placementId ?: "", seq, mutableMapOf<String, Any>().apply {
                    put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
                    LogUtils.i("VpnReporter", "app open - show [${this}]")
                })
//                AdEventLogger.logInterstitialAdShow(MainApplication.getContext())
            }
        }

        mAppOpenAd?.setOnPaidEventListener { adValue ->
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
                    precisionType.toString(), mutableMapOf<String, Any>().apply {
                        put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
                        LogUtils.i("VpnReporter", "app open - paid [${this}]")
                    }
                )
                AdReport.reportAdImpressionRevenue(this, AdFormat.APP_OPEN, context)
            }
            mAppOpenAd?.apply {
                AppsFlyerLib.getInstance().logEvent(context, AFInAppEventType.AD_VIEW, mutableMapOf<String?, Any?>().apply {
                    put(AFInAppEventParameterName.CURRENCY, adValue.currencyCode)
                    put(AFInAppEventParameterName.REVENUE, adValue.valueMicros / 1000000.0)
                })
            }
        }
    }

    fun show(activity: Activity, callBack: AdShowListener?, placementId: String) {
        mAdShowListener = callBack
        this.placementId = placementId
        try {
            mAppOpenAd?.show(activity)
        } catch (e: Exception) {}
    }

    fun isLoaded() = mAppOpenAd != null


    fun destroy() {

    }

    fun logToShow(placementId: String) {
        this.placementId = placementId
        seq = DTAdReport.generateUUID()
        DTAdReport.reportToShow(adId, AdType.INTERSTITIAL, AdPlatform.ADMOB, placementId, seq, mutableMapOf<String, Any>().apply {
            put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
            LogUtils.i("VpnReporter", "app open - to show [${this}]")
        })
    }
}