package com.vpn.android.ui

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.lifecycle.lifecycleScope

import com.vpn.android.MainApplication
import com.vpn.android.R
import com.vpn.android.ads.AdPresenterWrapper
import com.vpn.android.ads.constant.AdConstant
import com.vpn.android.ads.constant.AdFormat
import com.vpn.android.ads.proxy.AdShowListener
import com.vpn.android.constants.RemoteConstants
import com.vpn.android.coreservice.CoreSDKResponseManager
import com.vpn.android.databinding.SplashLayoutBinding
import com.vpn.android.ui.common.CommonAppCompatActivity
import com.vpn.tahiti.TahitiCoreServiceStateInfoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        
        // 设置全屏沉浸式状态栏
        setFullScreenImmersive()

        binding.progressBar.setCompletedColor(resources.getColor(R.color.progress_completed, null))
        binding.progressBar.setUncompletedColor(resources.getColor(R.color.progress_uncompleted, null))
        binding.progressBar.setProgressImage(R.mipmap.progress_indicator)

        initViewModel()
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
        if (!TahitiCoreServiceStateInfoManager.getInstance(MainApplication.context).coreServiceConnected && mIsColdStart) {
            CoreSDKResponseManager.fetchResponseAsLiveData.observe(this@SplashActivity) {
                if (it != null) { finish() }
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            delay(500)
            AdPresenterWrapper.getInstance().interstitialAdLoadLiveData.observe(this@SplashActivity) {
                it?.apply {
                    if (it && AdPresenterWrapper.getInstance().isLoadedExceptNative(AdFormat.INTERSTITIAL, mAdPlacement)) {
                        showInterstitialAd()
                    }
                }
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

    private fun startCountingDown() {
        mValueAnimator = ValueAnimator.ofInt(0, 100).apply {
            interpolator = LinearInterpolator()
            duration = mLoadingTime
            addUpdateListener {
                val progress = it.animatedValue as Int
                binding.progressBar.setProgress(progress.toFloat() / 100)
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