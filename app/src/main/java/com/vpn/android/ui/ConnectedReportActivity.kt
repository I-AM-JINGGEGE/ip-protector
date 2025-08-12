package com.vpn.android.ui

import android.os.Bundle
import android.text.format.Formatter
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.vpn.android.MainApplication
import com.vpn.android.R
import com.vpn.android.ads.AdPresenterWrapper
import com.vpn.android.ads.constant.AdConstant
import com.vpn.android.ads.constant.AdFormat
import com.vpn.android.ads.format.ViewStyle
import com.vpn.android.ads.proxy.AdLoadListener
import com.vpn.android.ads.proxy.AdShowListener
import com.vpn.android.base.utils.AppStoreUtils
import com.vpn.android.base.utils.ThreadUtils
import com.vpn.android.base.utils.ToastUtils
import com.vpn.android.constants.RemoteConstants.ADD_TIME_MAX_DURATION_VALUE_DEFAULT
import com.vpn.android.databinding.ConnectedReportLayoutBinding
import com.vpn.android.region.RegionUtils
import com.vpn.android.report.AppReport
import com.vpn.android.report.ReportConstants
import com.vpn.android.report.ReportConstants.AppReport.SOURCE_ADD_TIME_REPORT_PAGE_1
import com.vpn.android.report.ReportConstants.AppReport.SOURCE_ADD_TIME_REPORT_PAGE_2
import com.vpn.android.ui.common.CommonAppCompatActivity
import com.vpn.android.ui.dialog.CongratulationsDialog
import com.vpn.android.ui.dialog.ConnectivityTestDialog
import com.vpn.android.ui.home.HomeViewModel
import com.vpn.android.ui.rating.RatingGuideToast
import com.vpn.android.ui.regionselector.card.ConnectedViewModel
import com.vpn.android.ui.splash.AddTimeLoadingFragment
import com.vpn.android.ui.support.SupportUtils
import com.vpn.android.ui.widget.RatingBar
import com.vpn.android.utils.TimeUtils
import com.vpn.tahiti.TahitiCoreServiceStateInfoManager
import com.jaeger.library.StatusBarUtil
import org.libpag.PAGFile
import java.util.Random

class ConnectedReportActivity : CommonAppCompatActivity() {
    private lateinit var binding: ConnectedReportLayoutBinding

    private lateinit var mHomeViewModel: HomeViewModel
    private lateinit var mConnectedViewModel: ConnectedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ConnectedReportLayoutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initView()
        startAnimations()
        initViewModel()
        StatusBarUtil.setTransparent(this)
        AdPresenterWrapper.getInstance().loadNativeAd(
            AdConstant.AdPlacement.N_CONNECTED_REPORT,
            null, "connected report")
    }
    private fun initView() {
        binding.testButton.setOnClickListener {
            AdPresenterWrapper.getInstance().loadAdExceptNative(
                AdFormat.INTERSTITIAL,
                AdConstant.AdPlacement.I_CONNECTIVITY_TEST,
                null, "network test[report]")
            ConnectivityTestDialog(this).apply {
                setCancelable(false)
                setDialogOnClickListener(object : ConnectivityTestDialog.DialogListener {
                    override fun onCloseClick() {
                        dismiss()
                        AdPresenterWrapper.getInstance().apply {
                            logToShow(AdFormat.INTERSTITIAL, AdConstant.AdPlacement.I_CONNECTIVITY_TEST)
                            showAdExceptNative(
                                this@ConnectedReportActivity, AdFormat.INTERSTITIAL,
                                AdConstant.AdPlacement.I_CONNECTIVITY_TEST,
                                null
                            )
                        }
                    }

                    override fun onRetestClick() {
                        dismiss()
                        binding.testButton.performClick()
                    }
                })
                show()
            }
        }
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        binding.addTime1.setOnClickListener {
            AppReport.reportClickAddTime1(SOURCE_ADD_TIME_REPORT_PAGE_1)
            addTime(AdConstant.AdPlacement.I_ADD_TIME_1_REPORT_PAGE, 60)
        }
        binding.addTime2.setOnClickListener {
            AppReport.reportClickAddTime1(SOURCE_ADD_TIME_REPORT_PAGE_2)
            addTime(AdConstant.AdPlacement.I_ADD_TIME_2_REPORT_PAGE, Random().nextInt(60) + 60)
        }
        binding.ratingBar.setOnItemSelectedListener(object : RatingBar.OnItemSelectedListener {
            override fun onItemSelected(type: RatingBar.Type) {
                AppReport.reportClickRate(ReportConstants.AppReport.SOURCE_CONNECTED_REPORT_PAGE, type)
                when(type) {
                    RatingBar.Type.TERRIBLE, RatingBar.Type.BAD, RatingBar.Type.OK -> {
                        SupportUtils.sendFeedback(this@ConnectedReportActivity)
                    }
                    RatingBar.Type.GREAT, RatingBar.Type.GOOD -> {
                        if (AppStoreUtils.openPlayStoreOnlyWithUrl(this@ConnectedReportActivity, SupportUtils.getGPLink(this@ConnectedReportActivity))) {
                            RatingGuideToast.go(this@ConnectedReportActivity)
                        }
                    }
                }
            }
        })
        TahitiCoreServiceStateInfoManager.getInstance(applicationContext).coreServiceConnectedServerAsLiveData.value?.let {
            Glide.with(this)
                .load(RegionUtils.getRegionFlagImageResource(applicationContext, it.country))
                .into(binding.regionImage)
            binding.regionContent.text = it.zoneId
            binding.ipContent.text = it.host?.host
        }
    }

    private fun addTime(adPlacement: String, addedMinutes: Int) {
        mHomeViewModel.startShowLoadingAd()
        val canceled = booleanArrayOf(false)
        ThreadUtils.delayRunOnMainThread({
            if (!canceled[0] && mHomeViewModel.showAddTimeLoadingAsLiveData.value == true) {
                mHomeViewModel.stopShowLoadingAd()
                ToastUtils.showToast(this@ConnectedReportActivity, resources.getString(R.string.add_time_fail))
            }
        }, ADD_TIME_MAX_DURATION_VALUE_DEFAULT)
        AdPresenterWrapper.getInstance().loadAdExceptNative(
            AdFormat.INTERSTITIAL, adPlacement,
            object : AdLoadListener {
                override fun onAdLoaded() {
                    canceled[0] = true
                    mHomeViewModel.stopShowLoadingAd()
                    mHomeViewModel.addTimeA(addedMinutes).observe(this@ConnectedReportActivity) { aBoolean: Boolean ->
                        if (!aBoolean) {
                            return@observe
                        }
                        AdPresenterWrapper.getInstance().logToShow(AdFormat.INTERSTITIAL, adPlacement)
                        if (AdPresenterWrapper.getInstance().isLoadedExceptNative(AdFormat.INTERSTITIAL, adPlacement)) {
                            AdPresenterWrapper.getInstance().showAdExceptNative(this@ConnectedReportActivity, AdFormat.INTERSTITIAL, adPlacement, object : AdShowListener {
                                override fun onAdShown() {}

                                override fun onAdFailToShow(errorCode: Int, errorMessage: String) {}

                                override fun onAdClosed() {
                                    showAddTimeSuccessDialog(addedMinutes)
                                }

                                override fun onAdClicked() {}
                            })
                        } else {
                            showAddTimeSuccessDialog(addedMinutes)
                        }
                    }
                }

                override fun onFailure(errorCode: Int, errorMessage: String) {
                    canceled[0] = true
                    mHomeViewModel.stopShowLoadingAd()
                    ToastUtils.showToast(this@ConnectedReportActivity, resources.getString(R.string.add_time_fail))
                }
            }, if (adPlacement == AdConstant.AdPlacement.I_ADD_TIME_1_REPORT_PAGE) "add time 1[report]" else "add time 2[report]")
    }

    private fun startAnimations() {
        binding.light.apply {
            composition =
                PAGFile.Load(MainApplication.context.assets, "add_time_light_2s.pag")
            setRepeatCount(0)
            play()
        }
        binding.clock.apply {
            composition =
                PAGFile.Load(MainApplication.context.assets, "clock.pag")
            setRepeatCount(0)
            play()
        }

        AnimationUtils.loadAnimation(this, R.anim.connected_anim_inverse).apply {
            interpolator = LinearInterpolator()
            binding.connectedWheel.startAnimation(this)
        }
        AnimationUtils.loadAnimation(this, R.anim.connected_anim_clockwise).apply {
            interpolator = LinearInterpolator()
            binding.connectedProgressbar.startAnimation(this)
        }
    }

    private fun initViewModel() {
        AdPresenterWrapper.getInstance().nativeAdLoadLiveData.observe(this) { result ->
            if (result && AdPresenterWrapper.getInstance().isNativeAdLoaded(AdConstant.AdPlacement.N_CONNECTED_REPORT)) {
                binding.nativeAdContainer.apply {
                    visibility = View.VISIBLE
                    removeAllViews()
                    AdPresenterWrapper.getInstance()
                        .getNativeAdSmallView(
                            ViewStyle.WHITE, AdConstant.AdPlacement.N_CONNECTED_REPORT,
                            this,
                            null
                        )?.let {
                            addView(it)
                        }
                }
            }
        }
        mHomeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        mConnectedViewModel =
            ViewModelProvider(this).get(ConnectedViewModel::class.java)
        mHomeViewModel.showAddTimeLoadingAsLiveData.observe(
            this
        ) { showAddTimeLoadingResult: Boolean? ->
            if (showAddTimeLoadingResult != null && showAddTimeLoadingResult) {
                showAddTimeLoading()
                return@observe
            }
            cancelAddTimeLoading()
        }
        mHomeViewModel.trafficStatsAsLiveData.observe(this) { trafficStats ->
            Formatter.formatFileSize(applicationContext, trafficStats.rxRate).let {
                binding.downloadTitle.text = "$it/s"
            }
            Formatter.formatFileSize(applicationContext, trafficStats.txRate).let {
                binding.uploadTitle.text = "$it/s"
            }
            Formatter.formatFileSize(applicationContext, trafficStats.rxTotal).let {
                binding.downloadContent.text = "Total: $it"
            }
            Formatter.formatFileSize(applicationContext, trafficStats.txTotal).let {
                binding.uploadContent.text = "Total: $it"
            }
        }
        mConnectedViewModel.usedUpRemainSecondsAsLiveData.observe(this) { connectedSeconds ->
            if (connectedSeconds == null) {
                return@observe
            }
            if (connectedSeconds <= 0) {
                finish()
                return@observe
            }
            var hour = 0
            var minute = (connectedSeconds / 60).toInt()
            val second: Int
            if (minute < 60) {
                second = (connectedSeconds % 60).toInt()
            } else {
                hour = minute / 60
                minute %= 60
                second = (connectedSeconds - hour * 3600 - minute * 60).toInt()
            }
            binding.remainingTime.text = "${TimeUtils.leastTwoDigitsFormat(hour)} : ${
                TimeUtils.leastTwoDigitsFormat(
                    minute
                )
            } : ${TimeUtils.leastTwoDigitsFormat(second)}"
        }
    }

    private var mAddTimeLoadingFragment: AddTimeLoadingFragment? = null

    private fun showAddTimeLoading() {
        cancelAddTimeLoading()
        mAddTimeLoadingFragment = AddTimeLoadingFragment().apply {
            show(supportFragmentManager, "")
        }
    }

    private fun cancelAddTimeLoading() {
        if (mAddTimeLoadingFragment?.isAdded == true) {
            mAddTimeLoadingFragment!!.dismiss()
            mAddTimeLoadingFragment = null
        }
    }

    private fun showAddTimeSuccessDialog(addedMinutes: Int) {
        CongratulationsDialog(this).apply {
            setCancelable(false)
            setDialogOnClickListener {
                cancel()
                SupportUtils.checkToShowRating(this@ConnectedReportActivity)
            }
            setMessage(getString(R.string.increased_x_minutes, addedMinutes))
            show()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.page_in, R.anim.page_out)
    }

    override fun onBackPressed() {
        setResult(RESULT_OK)
        super.onBackPressed()
    }
}