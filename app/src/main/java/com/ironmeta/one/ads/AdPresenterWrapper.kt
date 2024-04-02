package com.ironmeta.one.ads

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import com.ironmeta.one.MainApplication
import com.ironmeta.one.ads.constant.AdConstant
import com.ironmeta.one.base.net.NetworkManager
import com.ironmeta.one.ads.constant.AdFormat
import com.ironmeta.one.ads.bean.UserAdConfig
import com.ironmeta.one.ads.format.ViewStyle
import com.ironmeta.one.ads.presenter.AdPresenter
import com.ironmeta.one.ads.proxy.AdLoadListener
import com.ironmeta.one.ads.proxy.AdShowListener
import com.ironmeta.one.ads.proxy.IAdPresenterProxy
import com.ironmeta.one.ads.proxy.RewardedAdShowListener
import com.ironmeta.one.comboads.network.UserProfileRetrofit
import com.ironmeta.one.report.AppReport
import com.ironmeta.one.report.VpnReporter
import com.ironmeta.one.utils.TimeUtils
import com.ironmeta.tahiti.TahitiCoreServiceStateInfoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.CountDownLatch

class AdPresenterWrapper private constructor() : IAdPresenterProxy {
    private val context = MainApplication.context
    private var mAdPresenterDisconnected: IAdPresenterProxy? = null
    private var mAdPresenterConnected: IAdPresenterProxy? = null
    private var userProfile: UserAdConfig? = null
    private lateinit var countDownLatch: CountDownLatch
    var interstitialAdLoadLiveData = MutableLiveData<Boolean>()
    var appOpenAdLoadLiveData = MutableLiveData<Boolean>()
    var appStartAdNoFillLoadLiveData = MutableLiveData<Boolean>()
    var nativeAdLoadLiveData: MutableLiveData<Boolean> = MutableLiveData()
    var initialized: Boolean = false

    fun init(initListener: InitListener?) {
        AppReport.reportAdInitBegin(TahitiCoreServiceStateInfoManager.getInstance(context).coreServiceConnected)
        countDownLatch = CountDownLatch(1)
        NetworkManager.getInstance(context).connectedAsLiveData.observeForever { connectState ->
            if (connectState == null || !connectState) {
                return@observeForever
            }
            if (userProfile != null) {
                countDownLatch.countDown()
                return@observeForever
            }
            if (adUserProfileRequesting) {
                return@observeForever
            }
            if (TimeUtils.isNewUser(context)) {
                getAdUserProfile()
            } else {
                countDownLatch.countDown()
            }
        }
        GlobalScope.launch {
            countDownLatch.await()
            withContext(Dispatchers.Main) {
                initAdPresenter()
                initListener?.onInitialized()
            }
            initialized = true
        }
    }

    private fun getAdUserProfile() {
        val start = System.currentTimeMillis()
        adUserProfileRequesting = true
        VpnReporter.reportAdConfigRequestStart()
        UserProfileRetrofit.getInstance().getUserProfile(context, object : Callback<UserAdConfig> {
            override fun onResponse(call: Call<UserAdConfig>, response: Response<UserAdConfig>) {
                VpnReporter.reportAdConfigRequestEnd(0, "", true, userProfile, System.currentTimeMillis() - start)
                userProfile = response.body()
                adUserProfileRequesting = false
                countDownLatch.countDown()
            }

            override fun onFailure(call: Call<UserAdConfig>, t: Throwable) {
                VpnReporter.reportAdConfigRequestEnd(-1, "${t.message ?: t.toString()}", false, null, System.currentTimeMillis() - start)
                adUserProfileRequesting = false
                countDownLatch.countDown()
            }
        })
    }

    private fun initAdPresenter() {
        userProfile?.adConfig?.let {
            mAdPresenterDisconnected = AdPresenter(it.adUnitSetBeforeConnect, context)
            mAdPresenterConnected = AdPresenter(it.adUnitSetAfterConnect, context)
        }
    }

    private var adUserProfileRequesting = false

    override fun loadAdExceptNative(
        type: AdFormat,
        adPlacement: String,
        loadListener: AdLoadListener?,
        from: String
    ) {
        if (!TahitiCoreServiceStateInfoManager.getInstance(context).coreServiceConnected) {
            loadListener?.onFailure(-4, "vpn not connected")
            return
        }
        val listener = object : AdLoadListener {
            override fun onAdLoaded() {
                loadListener?.onAdLoaded()
                when(type) {
                    AdFormat.INTERSTITIAL -> {
                        interstitialAdLoadLiveData.value = true
                    }
                    AdFormat.APP_OPEN -> {
                        appOpenAdLoadLiveData.value = true
                    }
                }
            }

            override fun onFailure(errorCode: Int, errorMessage: String) {
                loadListener?.onFailure(errorCode, errorMessage)
                if (type == AdFormat.INTERSTITIAL &&
                    (adPlacement == AdConstant.AdPlacement.I_APP_START_DISCONNECT ||
                            adPlacement == AdConstant.AdPlacement.I_APP_START_CONNECT ||
                            adPlacement == AdConstant.AdPlacement.I_HOME_RESTART_CONNECT ||
                            adPlacement == AdConstant.AdPlacement.I_HOME_RESTART_DISCONNECT)
                ) {
                    appStartAdNoFillLoadLiveData.value = true
                }
            }
        }
        loadAdInternal(type, adPlacement, listener, from)
    }

    override fun loadNativeAd(adPlacement: String, loadListener: AdLoadListener?, from: String) {
        if (!TahitiCoreServiceStateInfoManager.getInstance(context).coreServiceConnected) {
            loadListener?.onFailure(-4, "vpn not connected")
            return
        }
        VpnReporter.reportAdLoadStart(AdFormat.NATIVE, from)
        val start = System.currentTimeMillis()
        val listener = object : AdLoadListener {
            override fun onAdLoaded() {
                loadListener?.onAdLoaded()
                nativeAdLoadLiveData.value = true
            }

            override fun onFailure(errorCode: Int, errorMessage: String) {
                loadListener?.onFailure(errorCode, errorMessage)
            }
        }
        when (TahitiCoreServiceStateInfoManager.getInstance(context).coreServiceConnected) {
            true -> {
                if (mAdPresenterConnected == null) {
                    VpnReporter.reportAdLoadEnd(AdFormat.NATIVE, -1, "no ad config", false, from, System.currentTimeMillis() - start)
                }
                mAdPresenterConnected?.loadNativeAd(adPlacement, listener, from)
            }
            false -> {
                mAdPresenterDisconnected?.loadNativeAd(adPlacement, listener, from)
            }
        }
    }

    private fun loadAdInternal(type: AdFormat, adPlacement: String, loadListener: AdLoadListener?, from: String) {
        VpnReporter.reportAdLoadStart(type, from)
        val start = System.currentTimeMillis()
        when (TahitiCoreServiceStateInfoManager.getInstance(context).coreServiceConnected) {
            true -> {
                if (mAdPresenterConnected == null) {
                    VpnReporter.reportAdLoadEnd(type, -1, "no ad config", false, from, System.currentTimeMillis() - start)
                }
                mAdPresenterConnected?.loadAdExceptNative(type, adPlacement, loadListener, from) ?: AppReport.reportAdIgnoreLoading(true, type)
            }
            false -> {
                mAdPresenterDisconnected?.loadAdExceptNative(type, adPlacement, loadListener, from) ?: AppReport.reportAdIgnoreLoading(false, type)
            }
        }
    }

    override fun isLoadedExceptNative(type: AdFormat, adPlacement: String): Boolean {
        return isLoadedInternal(type, adPlacement)
    }

    override fun isNativeAdLoaded(adPlacement: String): Boolean {
        return when (TahitiCoreServiceStateInfoManager.getInstance(context).coreServiceConnected) {
            true -> {
                mAdPresenterConnected?.isNativeAdLoaded(adPlacement) == true
            }
            false -> {
                mAdPresenterDisconnected?.isNativeAdLoaded(adPlacement) == true
            }
        }
    }

    private fun isLoadedInternal(type: AdFormat, adPlacement: String): Boolean {
        return when (TahitiCoreServiceStateInfoManager.getInstance(context).coreServiceConnected) {
            true -> {
                mAdPresenterConnected?.isLoadedExceptNative(type, adPlacement) == true
            }
            false -> {
                mAdPresenterDisconnected?.isLoadedExceptNative(type, adPlacement) == true
            }
        }
    }

    override fun showAdExceptNative(
        activity: Activity,
        type: AdFormat,
        adPlacement: String,
        listener: AdShowListener?
    ) {
        if (fullScreenAdShown || !isAppForeground) {
            return
        }
        showAdInternal(activity, type, adPlacement, listener)
    }

    private fun showAdInternal(activity: Activity, type: AdFormat, adPlacement: String, listener: AdShowListener?) {
        when (TahitiCoreServiceStateInfoManager.getInstance(context).coreServiceConnected) {
            true -> {
                mAdPresenterConnected?.showAdExceptNative(
                    activity,
                    type,
                    adPlacement,
                    object : RewardedAdShowListener {
                        override fun onAdShown() {
                            listener?.onAdShown()
                            fullScreenAdShow()
                        }

                        override fun onAdFailToShow(errorCode: Int, errorMessage: String) {
                            listener?.onAdFailToShow(errorCode, errorMessage)
                            if (type == AdFormat.INTERSTITIAL) {
                                loadAdInternal(type, adPlacement, null, "ad fail to show")
                            }
                        }

                        override fun onAdClosed() {
                            fullScreenAdClosed()
                            listener?.onAdClosed()
                        }

                        override fun onAdClicked() {
                            listener?.onAdClicked()
                        }

                        override fun onRewarded() {
                            if (listener is RewardedAdShowListener) {
                                listener?.onRewarded()
                            }
                        }
                    })
            }
            false -> {
                mAdPresenterDisconnected?.showAdExceptNative(
                    activity,
                    type,
                    adPlacement,
                    object : RewardedAdShowListener {
                        override fun onAdShown() {
                            listener?.onAdShown()
                            fullScreenAdShow()
                        }

                        override fun onAdFailToShow(errorCode: Int, errorMessage: String) {
                            listener?.onAdFailToShow(errorCode, errorMessage)
                            if (type == AdFormat.INTERSTITIAL) {
                                loadAdInternal(type, adPlacement, null, "ad fail to show")
                            }
                        }

                        override fun onAdClosed() {
                            fullScreenAdClosed()
                            listener?.onAdClosed()
                        }

                        override fun onAdClicked() {
                            listener?.onAdClicked()
                        }

                        override fun onRewarded() {
                            if (listener is RewardedAdShowListener) {
                                listener?.onRewarded()
                            }
                        }
                    })
            }
        }
    }

    override fun getNativeAdExitAppView(
        placementId: String,
        parent: ViewGroup,
        listener: AdShowListener?
    ): View? {
        val templateListener = object : AdShowListener {
            override fun onAdShown() {
                listener?.onAdShown()
            }

            override fun onAdFailToShow(errorCode: Int, errorMessage: String) {
                listener?.onAdFailToShow(errorCode, errorMessage)
            }

            override fun onAdClosed() {
                listener?.onAdClosed()
            }

            override fun onAdClicked() {
                markNativeAdShown(placementId)
                loadNativeAd(placementId, null, "ad clicked")
                listener?.onAdClicked()

            }
        }
        return when (TahitiCoreServiceStateInfoManager.getInstance(context).coreServiceConnected) {
            true -> {
                mAdPresenterConnected?.getNativeAdExitAppView(placementId, parent, templateListener)
            }
            false -> {
                mAdPresenterDisconnected?.getNativeAdExitAppView(placementId, parent, templateListener)
            }
        }
    }

    override fun getNativeAdMediumView(
        bigStyle: Boolean,
        placementId: String,
        parent: ViewGroup,
        listener: AdShowListener?
    ): View? {
        val templateListener = object : AdShowListener {
            override fun onAdShown() {
                listener?.onAdShown()
            }

            override fun onAdFailToShow(errorCode: Int, errorMessage: String) {
                listener?.onAdFailToShow(errorCode, errorMessage)
            }

            override fun onAdClosed() {
                listener?.onAdClosed()
            }

            override fun onAdClicked() {
                markNativeAdShown(placementId)
                loadNativeAd(placementId, null, "ad clicked")
                listener?.onAdClicked()

            }
        }
        return when (TahitiCoreServiceStateInfoManager.getInstance(context).coreServiceConnected) {
            true -> {
                mAdPresenterConnected?.getNativeAdMediumView(
                    bigStyle, placementId,
                    parent,
                    templateListener
                )
            }
            false -> {
                mAdPresenterDisconnected?.getNativeAdMediumView(
                    bigStyle, placementId,
                    parent,
                    templateListener
                )
            }
        }
    }

    override fun getNativeAdSmallView(
        style: ViewStyle,
        placementId: String,
        parent: ViewGroup,
        listener: AdShowListener?
    ): View? {
        val templateListener = object : AdShowListener {
            override fun onAdShown() {
                listener?.onAdShown()
            }

            override fun onAdFailToShow(errorCode: Int, errorMessage: String) {
                listener?.onAdFailToShow(errorCode, errorMessage)
            }

            override fun onAdClosed() {
                listener?.onAdClosed()
            }

            override fun onAdClicked() {
                listener?.onAdClicked()
                markNativeAdShown(placementId)
                loadNativeAd(placementId, null, "ad clicked")
            }
        }
        return when (TahitiCoreServiceStateInfoManager.getInstance(context).coreServiceConnected) {
            true -> {
                mAdPresenterConnected?.getNativeAdSmallView(style, placementId, parent, templateListener)
            }
            false -> {
                mAdPresenterDisconnected?.getNativeAdSmallView(
                    style, placementId,
                    parent,
                    templateListener
                )
            }
        }
    }

    override fun markNativeAdShown(placementId: String) {
        when (TahitiCoreServiceStateInfoManager.getInstance(context).coreServiceConnected) {
            true -> {
                mAdPresenterConnected?.markNativeAdShown(placementId)
            }
            false -> {
                mAdPresenterDisconnected?.markNativeAdShown(placementId)
            }
        }
    }

    override fun destroyShownNativeAd() {
        when (TahitiCoreServiceStateInfoManager.getInstance(context).coreServiceConnected) {
            true -> {
                mAdPresenterConnected?.destroyShownNativeAd()
            }
            false -> {
                mAdPresenterDisconnected?.destroyShownNativeAd()
            }
        }
    }

    override fun logToShow(type: AdFormat, adPlacement: String) {
        when (TahitiCoreServiceStateInfoManager.getInstance(context).coreServiceConnected) {
            true -> {
                mAdPresenterConnected?.logToShow(type, adPlacement)
            }
            false -> {
                mAdPresenterDisconnected?.logToShow(type, adPlacement)
            }
        }
    }

    var isAppForeground = false

    private var fullScreenAdShown = false

    fun isFullScreenAdShown(): Boolean {
        return fullScreenAdShown
    }

    private fun fullScreenAdShow() {
        fullScreenAdShown = true
    }

    private fun fullScreenAdClosed() {
        fullScreenAdShown = false
    }

    companion object {
        private var presenter: AdPresenterWrapper = AdPresenterWrapper()

        @Synchronized
        fun getInstance(): AdPresenterWrapper {
            return presenter
        }
    }

    interface InitListener {
        fun onInitialized()
    }
}