package com.vpn.android.ads.format

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.vpn.android.ads.constant.AdFormat
import com.vpn.android.ads.network.IpUtil
import com.vpn.android.databinding.UnifiedNativeAdExitAppBinding
import com.vpn.android.databinding.UnifiedNativeAdMediaBigBinding
import com.vpn.android.databinding.UnifiedNativeAdMediaSmallBinding
import com.vpn.android.databinding.UnifiedNativeAdSmallBlackBinding
import com.vpn.android.databinding.UnifiedNativeAdSmallWhiteBinding
import com.vpn.android.report.AdReport
import ai.datatower.ad.AdPlatform
import ai.datatower.ad.AdType
import ai.datatower.ad.DTAdReport
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AFInAppEventType
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.vpn.android.base.utils.LogUtils
import com.vpn.android.report.ReportConstants.Param.IP_ADDRESS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AdNative(val context: Context, var adId: String) {
    private var start = 0L
    private var from = ""
    private var mNativeAd: NativeAd? = null
    private var mGarbageList = arrayListOf<NativeAd>()
    var seq: String = DTAdReport.generateUUID()
    private var placementId: String? = null
    private var mNativeAdLoadListener: NativeAdLoadListener? = null
    private var mNativeAdShowListener: NativeAdShowListener? = null
    private val adLoader = AdLoader.Builder(context, adId)
        .forNativeAd { ad: NativeAd ->
            ad.setOnPaidEventListener { adValue ->
                GlobalScope.launch(Dispatchers.Main) {
                    DTAdReport.reportPaid(
                        adId,
                        AdType.NATIVE,
                        AdPlatform.ADMOB,
                        placementId ?: "",
                        seq,
                        adValue.valueMicros.toDouble() / 1000000,
                        adValue.currencyCode,
                        adValue.precisionType.toString(),
                        mutableMapOf<String, Any>().apply {
                            put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
                            LogUtils.i("VpnReporter", "native paid [${this}]")
                        }
                    )
                    AdReport.reportAdImpressionRevenue(adValue, AdFormat.NATIVE, context)
                }
                mNativeAd?.apply {
                    LogUtils.i("VpnReporter", "native log appsflyer")
                    AppsFlyerLib.getInstance().logEvent(context, AFInAppEventType.AD_VIEW, mutableMapOf<String?, Any?>().apply {
                        put(AFInAppEventParameterName.CURRENCY, adValue.currencyCode)
                        put(AFInAppEventParameterName.REVENUE, adValue.valueMicros / 1000000.0)
                    }, object : AppsFlyerRequestListener {
                        override fun onSuccess() {
                            LogUtils.i("VpnReporter", "native log appsflyer onSuccess")
                        }

                        override fun onError(p0: Int, p1: String) {
                            LogUtils.e("VpnReporter", "native log appsflyer onError[$p0, $p1]")
                        }
                    })
                }
            }
            mNativeAd = ad
            mNativeAdLoadListener?.onAdLoaded()
            DTAdReport.reportLoadEnd(adId, AdType.NATIVE, AdPlatform.ADMOB, System.currentTimeMillis() - start, true, seq, 0, "", mutableMapOf<String, Any>().apply {
                put("from", from)
                put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
            })
        }
        .withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mNativeAdLoadListener?.onAdLoadFail(adError.code, adError.message)
                DTAdReport.reportLoadEnd(adId, AdType.NATIVE, AdPlatform.ADMOB, System.currentTimeMillis() - start, false, seq, adError.code, adError.message, mutableMapOf<String, Any>().apply {
                    put("from", from)
                    put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
                })
            }

            override fun onAdClicked() {
                DTAdReport.reportClick(adId, AdType.NATIVE, AdPlatform.ADMOB, placementId ?: "", seq, mutableMapOf<String, Any>().apply {
                    put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
                    LogUtils.i("VpnReporter", "native click [${this}]")
                })
                DTAdReport.reportConversionByClick(adId, AdType.NATIVE, ai.datatower.ad.AdPlatform.ADMOB, placementId ?: "", seq, mutableMapOf<String, Any>().apply {
                    put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
                    LogUtils.i("VpnReporter", "native conversion [${this}]")
                })
                mNativeAdShowListener?.onAdClicked()
            }

            override fun onAdImpression() {
                DTAdReport.reportShow(adId, AdType.NATIVE, AdPlatform.ADMOB, placementId ?: "", seq, mutableMapOf<String, Any>().apply {
                    put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
                    LogUtils.i("VpnReporter", "native show [${this}]")
                })
                mNativeAdShowListener?.onAdImpression()
            }
        })
        .withNativeAdOptions(
            NativeAdOptions.Builder()
                .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_LEFT)
                .build()
        )
        .build()

    fun loadAd(loadListener: NativeAdLoadListener?, from: String) {
        mNativeAdLoadListener = loadListener
        if (adLoader.isLoading) {
            return
        }
        if (mNativeAd != null) {
            loadListener?.onAdLoaded()
            return
        }
        start = System.currentTimeMillis()
        this.from = from
        DTAdReport.reportLoadBegin(adId, AdType.NATIVE, AdPlatform.ADMOB, seq, mutableMapOf<String, Any>().apply {
            put("from", from)
            put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
        })
        seq = DTAdReport.generateUUID()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun getNativeAdMediumView(
        bigStyle: Boolean,
        parent: ViewGroup,
        placementId: String,
        showListener: NativeAdShowListener?
    ): View? {
        this.placementId = placementId
        mNativeAdShowListener = showListener
        DTAdReport.reportToShow(adId, AdType.NATIVE, AdPlatform.ADMOB, placementId, seq, mutableMapOf<String, Any>().apply {
            put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
            LogUtils.i("VpnReporter", "native to show [${this}]")
        })
        return mNativeAd?.let { nativeAd ->
            if (bigStyle) {
                UnifiedNativeAdMediaBigBinding.inflate(
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                    parent,
                    false
                ).apply {
                    root.headlineView = title.apply { text = nativeAd.headline }
                    root.bodyView = content.apply { text = nativeAd.body }
                    nativeAd.icon?.drawable?.let {
                        root.iconView = icon.apply { setImageDrawable(it) }
                    }
                    root.callToActionView = button.apply { text = nativeAd.callToAction }
                    root.mediaView = media.apply {
                        mediaContent = nativeAd.mediaContent
                    }
                    root.setNativeAd(nativeAd)
                }.root
            } else {
                UnifiedNativeAdMediaSmallBinding.inflate(
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                    parent,
                    false
                ).apply {
                    root.headlineView = title.apply { text = nativeAd.headline }
                    root.bodyView = content.apply { text = nativeAd.body }
                    nativeAd.icon?.drawable?.let {
                        root.iconView = icon.apply { setImageDrawable(it) }
                    }
                    root.callToActionView = button.apply { text = nativeAd.callToAction }
                    root.mediaView = media.apply {
                        mediaContent = nativeAd.mediaContent
                    }
                    root.setNativeAd(nativeAd)
                }.root
            }
        }
    }

    fun getNativeAdSmallView(parent: ViewGroup, style: ViewStyle, placementId: String, showListener: NativeAdShowListener?): View? {
        this.placementId = placementId
        mNativeAdShowListener = showListener
        DTAdReport.reportToShow(adId, AdType.NATIVE, AdPlatform.ADMOB, placementId, seq, mutableMapOf<String, Any>().apply {
            put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
            LogUtils.i("VpnReporter", "native to show [${this}]")
        })
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return when (style) {
            ViewStyle.BLACK -> {
                mNativeAd?.let { nativeAd ->
                    UnifiedNativeAdSmallBlackBinding.inflate(layoutInflater, parent, false).apply {
                        root.headlineView = title.apply { text = nativeAd.headline }
                        root.bodyView = content.apply { text = nativeAd.body }
                        nativeAd.icon?.drawable?.let {
                            root.iconView = icon.apply { setImageDrawable(it) }
                        }
                        root.callToActionView = button.apply { text = nativeAd.callToAction }
                        root.setNativeAd(nativeAd)
                    }.root
                }
            }
            ViewStyle.WHITE -> {
                mNativeAd?.let { nativeAd ->
                    UnifiedNativeAdSmallWhiteBinding.inflate(layoutInflater, parent, false).apply {
                        root.headlineView = title.apply { text = nativeAd.headline }
                        root.bodyView = content.apply { text = nativeAd.body }
                        nativeAd.icon?.drawable?.let {
                            root.iconView = icon.apply { setImageDrawable(it) }
                        }
                        root.callToActionView = button.apply { text = nativeAd.callToAction }
                        root.setNativeAd(nativeAd)
                    }.root
                }
            }
        }
    }

    fun destroyShownAds() {
        mGarbageList.apply {
            forEach {
                it.destroy()
            }
            clear()
        }
    }

    fun markNativeAdShown() {
        mNativeAd?.let { mGarbageList.add(it) }
        mNativeAd = null
    }

    fun isLoaded(): Boolean {
        return mNativeAd != null
    }

    fun getNativeAdExitAppView(parent: ViewGroup, placementId: String, showListener: NativeAdShowListener): View? {
        this.placementId = placementId
        mNativeAdShowListener = showListener
        DTAdReport.reportToShow(adId, AdType.NATIVE, AdPlatform.ADMOB, placementId, seq, mutableMapOf<String, Any>().apply {
            put(IP_ADDRESS, IpUtil.getConnectedIdAddress())
            LogUtils.i("VpnReporter", "native to show [${this}]")
        })
        return mNativeAd?.let { nativeAd ->
            UnifiedNativeAdExitAppBinding.inflate(context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater, parent, false)
                .apply {
                    root.headlineView = title.apply { text = nativeAd.headline }
                    root.bodyView = content.apply { text = nativeAd.body }
                    nativeAd.icon?.drawable?.let {
                        root.iconView = icon.apply { setImageDrawable(it) }
                    }
                    root.callToActionView = button.apply { text = nativeAd.callToAction }
                    root.mediaView = media.apply {
                        mediaContent = nativeAd.mediaContent
                    }
                    root.setNativeAd(nativeAd)
                }.root
        }
    }
}

interface NativeAdLoadListener {
    fun onAdLoaded()
    fun onAdLoadFail(code: Int, message: String)
}

interface NativeAdShowListener {
    fun onAdImpression()
    fun onAdClicked()
}

enum class ViewStyle {
    BLACK, WHITE
}