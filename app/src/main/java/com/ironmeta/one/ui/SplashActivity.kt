package com.ironmeta.one.ui

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import androidx.lifecycle.lifecycleScope
import com.ironmeta.one.MainApplication
import com.ironmeta.one.R
import com.ironmeta.one.ads.AdPresenterWrapper
import com.ironmeta.one.ads.constant.AdConstant
import com.ironmeta.one.ads.constant.AdFormat
import com.ironmeta.one.ads.proxy.AdShowListener
import com.ironmeta.one.constants.RemoteConstants
import com.ironmeta.one.coreservice.CoreSDKResponseManager
import com.ironmeta.one.databinding.SplashLayoutBinding
import com.ironmeta.one.ui.common.CommonAppCompatActivity
import com.ironmeta.tahiti.TahitiCoreServiceStateInfoManager
import com.jaeger.library.StatusBarUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.libpag.PAGFile
import org.libpag.PAGView
import java.lang.Exception

class SplashActivity : CommonAppCompatActivity() {
    companion object {
        const val AD_PLACEMENT = "ad_placement"
        const val IS_COLD_START = "is_cold_start"
        const val LOADING_TIME_MAX = "loading_time_max"
    }
    private lateinit var binding: SplashLayoutBinding
    private var mLoadingTime = RemoteConstants.OPEN_AD_LOAD_MAX_DURATION_VALUE_DEFAULT
    private var mValueAnimator: ValueAnimator? = null
    private var mIsColdStart = true
    private lateinit var mAdPlacement: String
    private var logToShowReported = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SplashLayoutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        StatusBarUtil.setTransparent(this)
        initViewModel()
        startAnimation()
        startCountingDown()
        AdPresenterWrapper.getInstance().apply {
            if (initialized) {
                logToShow(AdFormat.INTERSTITIAL, mAdPlacement)
                logToShowReported = true
            }
        }
    }

    private fun initViewModel() {
        mAdPlacement = intent.getStringExtra(AD_PLACEMENT) ?: AdConstant.AdPlacement.I_APP_START_DISCONNECT
        mLoadingTime = intent.getLongExtra(LOADING_TIME_MAX, RemoteConstants.OPEN_AD_LOAD_MAX_DURATION_VALUE_DEFAULT)
        mIsColdStart = intent.getBooleanExtra(IS_COLD_START, true)
        AdPresenterWrapper.getInstance().interstitialAdLoadLiveData.observe(this) {
            it?.apply {
                if (it && AdPresenterWrapper.getInstance().isLoadedExceptNative(AdFormat.INTERSTITIAL, mAdPlacement)) {
                    showInterstitialAd()
                }
            }
        }
        if (!TahitiCoreServiceStateInfoManager.getInstance(MainApplication.context).coreServiceConnected && mIsColdStart) {
            CoreSDKResponseManager.fetchResponseAsLiveData.observe(this@SplashActivity) {
                if (it != null) { finish() }
            }
        }
    }

    private fun showInterstitialAd() {
        mValueAnimator?.cancel()
        if (!logToShowReported) {
            AdPresenterWrapper.getInstance().logToShow(AdFormat.INTERSTITIAL, mAdPlacement)
        }
        AdPresenterWrapper.getInstance().showAdExceptNative(
            this@SplashActivity, AdFormat.INTERSTITIAL,
            mAdPlacement,
            object : AdShowListener {
                override fun onAdShown() {}

                override fun onAdFailToShow(errorCode: Int, errorMessage: String) {
                    finish()
                }

                override fun onAdClosed() {
                    finish()
                }

                override fun onAdClicked() {}

            })
    }

    private fun startAnimation() {
        val delayMills = 2400L
        binding.loading.apply {
            composition = PAGFile.Load(MainApplication.context.assets, "loading.pag")
            lifecycleScope.launch(Dispatchers.IO) {
                playLoadingOnce(this@apply)
                delay(delayMills)
                playLoadingOnce(this@apply)
                delay(delayMills)
                playLoadingOnce(this@apply)
                delay(delayMills)
                playLoadingOnce(this@apply)
                delay(delayMills)
                playLoadingOnce(this@apply)
            }
        }
    }

    private fun playLoadingOnce(pagView: PAGView) {
        lifecycleScope.launch(Dispatchers.Main) {
            pagView.stop()
            pagView.progress = 0.0
            pagView.play()
        }
    }

    private fun startCountingDown() {
        mValueAnimator = ValueAnimator.ofInt(0, 100).apply {
            interpolator = DecelerateInterpolator(1.2F)
            duration = mLoadingTime
            addUpdateListener {
                val progress = it.animatedValue as Int
                binding.progressBar.progress = progress
                binding.progressNumber.text = "$progress%"
                if (progress == 100) {
                    finish()
                }
            }
            start()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.page_in, R.anim.page_out)
        mValueAnimator?.apply {
            if (isRunning) {
                cancel()
            }
        }
    }

    override fun onBackPressed() {
        if (mValueAnimator?.isRunning == true) {
             return
        } else {
            super.onBackPressed()
        }
    }
}