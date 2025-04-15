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
import com.ironmeta.one.report.VpnReporter
import ai.datatower.ad.AdPlatform
import ai.datatower.ad.AdType
import ai.datatower.ad.DTAdReport
import android.content.Context
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AFInAppEventType
import com.appsflyer.AppsFlyerLib
import com.ironmeta.one.base.utils.LogUtils
import com.ironmeta.one.report.ReportConstants.Param.IP_ADDRESS

class AdRewarded(var adId: String, val context: Context) {
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
                DTAdReport.reportClick(adId, AdType.REWARDED, AdPlatform.ADMOB, placementId ?: "", seq, mutableMapOf<String, Any>().apply {
                    put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
                    LogUtils.i("VpnReporter", "rewarded - click [${this}]")
                })
                DTAdReport.reportConversionByClick(adId, AdType.REWARDED, AdPlatform.ADMOB, placementId ?: "", seq, mutableMapOf<String, Any>().apply {
                    put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
                    LogUtils.i("VpnReporter", "rewarded - conversion [${this}]")
                })
            }

            override fun onAdDismissedFullScreenContent() {
                mRewardedAd = null
                mAdShowListener?.onAdClosed()
                DTAdReport.reportClose(adId, AdType.REWARDED, AdPlatform.ADMOB, placementId ?: "", seq, mutableMapOf<String, Any>().apply {
                    put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
                    LogUtils.i("VpnReporter", "rewarded - close [${this}]")
                })
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                mRewardedAd = null
                mAdShowListener?.onAdFailToShow(adError.code, adError.message)
                DTAdReport.reportShowFailed(adId, AdType.REWARDED, AdPlatform.ADMOB, placementId ?: "", seq, adError.code, adError.message, mutableMapOf<String, Any>().apply {
                    put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
                    LogUtils.i("VpnReporter", "rewarded - fail show [${this}]")
                })
            }

            override fun onAdImpression() {}

            override fun onAdShowedFullScreenContent() {
                mAdShowListener?.onAdShown()
                DTAdReport.reportShow(adId, AdType.REWARDED, AdPlatform.ADMOB, placementId ?: "", seq, mutableMapOf<String, Any>().apply {
                    put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
                    LogUtils.i("VpnReporter", "rewarded - show [${this}]")
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
                    precisionType.toString(), mutableMapOf<String, Any>().apply {
                        put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
                        LogUtils.i("VpnReporter", "rewarded - paid [${this}]")
                    }
                )
                AdReport.reportAdImpressionRevenue(this, AdFormat.REWARDED, MainApplication.instance.applicationContext)
            }
            mRewardedAd?.apply {
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
            mRewardedAd?.show(activity) {
                if (mAdShowListener is RewardedAdShowListener) {
                    (mAdShowListener as RewardedAdShowListener)?.onRewarded()
                }
                DTAdReport.reportRewarded(adId, AdType.REWARDED, AdPlatform.ADMOB, placementId ?: "", seq, mutableMapOf<String, Any>().apply {
                    put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
                    LogUtils.i("VpnReporter", "rewarded - rewarded [${this}]")
                })
                DTAdReport.reportConversionByRewarded(adId, AdType.REWARDED, AdPlatform.ADMOB, placementId ?: "", seq, mutableMapOf<String, Any>().apply {
                    put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
                    LogUtils.i("VpnReporter", "rewarded - conversion [${this}]")
                })
            }
        } catch (e: Exception) {}
    }

    fun isLoaded() = mRewardedAd != null


    fun destroy() {

    }

    fun logToShow(placementId: String) {
        this.placementId = placementId
        seq = DTAdReport.generateUUID()
        DTAdReport.reportToShow(adId, AdType.REWARDED, AdPlatform.ADMOB, placementId, seq, mutableMapOf<String, Any>().apply {
            put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
            LogUtils.i("VpnReporter", "rewarded - to show [${this}]")
        })
    }
}