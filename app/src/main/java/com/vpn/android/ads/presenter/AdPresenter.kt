package com.vpn.android.ads.presenter

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.AdRequest
import com.vpn.android.BuildConfig
import com.vpn.android.ads.constant.AdConstant
import com.vpn.android.ads.constant.AdFormat
import com.vpn.android.ads.constant.AdPlatform
import com.vpn.android.ads.bean.UserAdConfig
import com.vpn.android.ads.format.*
import com.vpn.android.ads.proxy.AdLoadListener
import com.vpn.android.ads.proxy.AdShowListener
import com.vpn.android.ads.proxy.IAdPresenterProxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.schedule

class AdPresenter(adUnitSet: UserAdConfig.AdUnitSet, val context: Context) : IAdPresenterProxy {
    private var adNative: AdNative? = null
    private var adInterstitial: AdInterstitialAdmob? = null

    init {
        initAds(adUnitSet, context)
    }

    private fun initAds(adUnitSet: UserAdConfig.AdUnitSet, context: Context) {
        if (adUnitSet.switch != true) {
            return
        }
        adUnitSet.interstitial?.let { list ->
            if (list.isEmpty()) {
                return@let
            }
            when (list[0].adPlatform) {
                AdPlatform.ADMOB.id.toString() -> {
                    val adUnitId = if (BuildConfig.APPLICATION_ID == "com.vpn.android.debug") {
                        generateAdUnitDebugId(AdFormat.INTERSTITIAL, AdPlatform.ADMOB)
                    } else {
                        list[0].id
                    }
                    adInterstitial = AdInterstitialAdmob(adUnitId, context)
                }
            }
        }
        adUnitSet.native?.let { list ->
            if (list.isEmpty()) {
                return@let
            }
            when (list[0].adPlatform) {
                AdPlatform.ADMOB.id.toString() -> {
                    val adUnitId = if (BuildConfig.APPLICATION_ID == "com.vpn.android.debug") {
                        generateAdUnitDebugId(AdFormat.NATIVE, AdPlatform.ADMOB)
                    } else {
                        list[0].id
                    }
                    adNative = AdNative(context, adUnitId)
                }
            }
        }
    }

    override fun loadAdExceptNative(
        adFormat: AdFormat,
        adPlacement: String,
        loadListener: AdLoadListener?,
        from: String
    ) {
        when (adFormat) {
            AdFormat.INTERSTITIAL -> {
                var loadStart = System.currentTimeMillis()
                var loadTimes = 0
                val loadListenerProxy = object : AdLoadListener {
                    override fun onAdLoaded() {
                        loadListener?.onAdLoaded()
                    }

                    override fun onFailure(errorCode: Int, errorMessage: String) {
                        if (loadTimes == 1 && errorCode != AdRequest.ERROR_CODE_NO_FILL && errorCode != AdRequest.ERROR_CODE_MEDIATION_NO_FILL) {
                            Timer().schedule(2000) {
                                loadTimes ++
                                GlobalScope.launch(Dispatchers.Main) {
                                    adInterstitial?.loadAd(loadListener, from)
                                }
                            }
                        } else {
                            loadListener?.onFailure(errorCode, errorMessage)
                        }
                    }
                }
                adInterstitial?.loadAd(loadListenerProxy, from)
                loadTimes = 1
            }

            else -> {}
        }
    }

    override fun loadNativeAd(adPlacement: String, loadListener: AdLoadListener?, from: String) {
        adNative?.loadAd(object : NativeAdLoadListener {
            override fun onAdLoaded() {
                loadListener?.onAdLoaded()
            }

            override fun onAdLoadFail(code: Int, message: String) {
                loadListener?.onFailure(code, message)
            }
        }, from)
    }

    override fun isLoadedExceptNative(adFormat: AdFormat, adPlacement: String): Boolean {
        return when (adFormat) {
            AdFormat.INTERSTITIAL -> {
                adInterstitial?.isLoaded() == true
            }
            else -> {
                false
            }
        }
    }

    override fun isNativeAdLoaded(adPlacement: String): Boolean {
        return adNative?.isLoaded() == true
    }

    override fun showAdExceptNative(
        activity: Activity,
        adFormat: AdFormat,
        adPlacement: String,
        listener: AdShowListener?
    ) {
        when (adFormat) {
            AdFormat.INTERSTITIAL -> {
                adInterstitial?.show(activity, listener, adPlacement)
            }

            else -> {}
        }
    }

    override fun getNativeAdExitAppView(
        placementId: String,
        parent: ViewGroup,
        listener: AdShowListener?
    ): View? {
        return adNative?.getNativeAdExitAppView(parent, placementId, object : NativeAdShowListener {
            override fun onAdImpression() {
                listener?.onAdShown()
            }

            override fun onAdClicked() {
                listener?.onAdClicked()
            }

        })
    }

    override fun getNativeAdMediumView(
        bigStyle: Boolean,
        placementId: String,
        parent: ViewGroup,
        listener: AdShowListener?
    ): View? {
        return adNative?.getNativeAdMediumView(bigStyle, parent, placementId, object : NativeAdShowListener {
            override fun onAdImpression() {
                listener?.onAdShown()
            }

            override fun onAdClicked() {
                listener?.onAdClicked()
            }

        })
    }

    override fun getNativeAdSmallView(style: ViewStyle, placementId: String, parent: ViewGroup, listener: AdShowListener?): View? {
        return adNative?.getNativeAdSmallView(parent, style, placementId, object : NativeAdShowListener {
            override fun onAdImpression() {
                listener?.onAdShown()
            }

            override fun onAdClicked() {
                listener?.onAdClicked()
            }
        })
    }

    override fun destroyShownNativeAd() {
        adNative?.destroyShownAds()
    }

    private fun generateAdUnitDebugId(adFormat: AdFormat, adPlatform: AdPlatform): String {
        when (adFormat) {
            AdFormat.INTERSTITIAL -> {
                when(adPlatform) {
                    AdPlatform.ADMOB -> {
                        return AdConstant.AdUnitId.ADMOB_INTERSTITIAL_ID_TEST
                    }

                    else -> {}
                }
            }
            AdFormat.NATIVE -> {
                when(adPlatform) {
                    AdPlatform.ADMOB -> {
                        return AdConstant.AdUnitId.ADMOB_NATIVE_ID_TEST
                    }

                    else -> {}
                }
            }
            AdFormat.REWARDED -> {
                when(adPlatform) {
                    AdPlatform.ADMOB -> {
                        return AdConstant.AdUnitId.ADMOB_REWARDED_ID_TEST
                    }

                    else -> {}
                }
            }
            AdFormat.APP_OPEN -> {
                when(adPlatform) {
                    AdPlatform.ADMOB -> {
                        return AdConstant.AdUnitId.ADMOB_APP_OPEN_ID_TEST
                    }

                    else -> {}
                }
            }
        }
        return ""
    }

    override fun logToShow(type: AdFormat, adPlacement: String) {
        when (type) {
            AdFormat.INTERSTITIAL -> {
                adInterstitial?.logToShow(adPlacement)
            }

            else -> {}
        }
    }

    override fun markNativeAdShown(adPlacement: String) {
        adNative?.markNativeAdShown()
    }
}