package com.ironmeta.one.ads.proxy

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import com.ironmeta.one.ads.constant.AdFormat
import com.ironmeta.one.ads.format.ViewStyle

interface IAdPresenterProxy {
    fun loadAdExceptNative(type: AdFormat, adPlacement: String, loadListener: AdLoadListener?)
    fun loadNativeAd(adPlacement: String, loadListener: AdLoadListener?)
    fun isLoadedExceptNative(type: AdFormat, adPlacement: String): Boolean
    fun isNativeAdLoaded(adPlacement: String): Boolean
    fun showAdExceptNative(activity: Activity, type: AdFormat, adPlacement: String, listener: AdShowListener? = null)
    fun getNativeAdExitAppView(placementId: String, parent: ViewGroup, listener: AdShowListener?): View?
    fun getNativeAdMediumView(bigStyle: Boolean, placementId: String, parent: ViewGroup, listener: AdShowListener?): View?
    fun getNativeAdSmallView(style: ViewStyle, placementId: String, parent: ViewGroup, listener: AdShowListener?): View?
    fun destroyShownNativeAd()
    fun logToShow(type: AdFormat, adPlacement: String)
    fun markNativeAdShown(adPlacement: String)
}