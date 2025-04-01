package com.ironmeta.one.ads.format

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
import com.ironmeta.one.ads.constant.AdFormat
import com.ironmeta.one.ads.network.IpUtil
import com.ironmeta.one.databinding.UnifiedNativeAdExitAppBinding
import com.ironmeta.one.databinding.UnifiedNativeAdMediaBigBinding
import com.ironmeta.one.databinding.UnifiedNativeAdMediaSmallBinding
import com.ironmeta.one.databinding.UnifiedNativeAdSmallBlackBinding
import com.ironmeta.one.databinding.UnifiedNativeAdSmallWhiteBinding
import com.ironmeta.one.report.AdReport
import ai.datatower.ad.AdPlatform
import ai.datatower.ad.AdType
import ai.datatower.ad.DTAdReport
import com.ironmeta.one.base.utils.LogUtils
import com.ironmeta.one.report.ReportConstants.Param.IP_ADDRESS
import com.ironmeta.one.report.VpnReporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AdNative(val context: Context, var adId: String) {
    private var mNativeAd: NativeAd? = null
    private var mGarbageList = arrayListOf<NativeAd>()
    var seq: String = DTAdReport.generateUUID()
    private var placementId: String? = null
    private var mNativeAdLoadListener: NativeAdLoadListener? = null
    private var mNativeAdShowListener: NativeAdShowListener? = null
    private val adLoader = AdLoader.Builder(context, adId)
        .forNativeAd { ad: NativeAd ->
            ad.setOnPaidEventListener {adValue ->
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
            }
            mNativeAd = ad
            mNativeAdLoadListener?.onAdLoaded()
        }
        .withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mNativeAdLoadListener?.onAdLoadFail(adError.code, adError.message)
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
        VpnReporter.reportAdLoadStart(AdFormat.NATIVE, from)
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