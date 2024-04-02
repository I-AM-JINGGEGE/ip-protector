package com.ironmeta.one.ui

import android.os.Bundle
import android.text.format.Formatter
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.ironmeta.one.R
import com.ironmeta.one.ads.AdPresenterWrapper
import com.ironmeta.one.ads.constant.AdConstant
import com.ironmeta.one.ads.format.ViewStyle
import com.ironmeta.one.base.utils.AppStoreUtils
import com.ironmeta.one.databinding.DisconnectReportLayoutBinding
import com.ironmeta.one.region.RegionUtils
import com.ironmeta.one.report.AppReport
import com.ironmeta.one.report.ReportConstants
import com.ironmeta.one.ui.common.CommonAppCompatActivity
import com.ironmeta.one.ui.rating.RatingGuideToast
import com.ironmeta.one.ui.regionselector.card.ConnectedViewModel
import com.ironmeta.one.ui.support.SupportUtils
import com.ironmeta.one.ui.widget.RatingBar
import com.ironmeta.one.utils.TimeUtils
import com.jaeger.library.StatusBarUtil
import com.sdk.ssmod.beans.TrafficStats

class DisconnectReportActivity : CommonAppCompatActivity() {
    private lateinit var binding: DisconnectReportLayoutBinding

    private lateinit var mConnectedViewModel: ConnectedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DisconnectReportLayoutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initView()
        initViewModel()
        StatusBarUtil.setTransparent(this)
        AdPresenterWrapper.getInstance().loadNativeAd(
            AdConstant.AdPlacement.N_DISCONNECT_REPORT,
            null, "disconnect report")
    }

    private fun initView() {
        intent?.apply {
            setConnectedTime(getLongExtra(EXTRA_CONNECTED_MILLISECONDS, 0))
            getStringExtra(EXTRA_REGION_CODE)?.let {
                Glide.with(this@DisconnectReportActivity)
                    .load(RegionUtils.getRegionFlagImageResource(applicationContext, it))
                    .into(binding.regionImage)
            }
            getStringExtra(EXTRA_REGION_NAME)?.let {
                binding.regionName.text = it
            }
            getParcelableExtra<TrafficStats>(EXTRA_TRAFFIC_STATS)?.let { trafficStats ->
                Formatter.formatFileSize(applicationContext, trafficStats.rxTotal).let {
                    binding.downloadTotal.text = it
                }
                Formatter.formatFileSize(applicationContext, trafficStats.txTotal).let {
                    binding.uploadTotal.text = it
                }
            }
        }
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
            overridePendingTransition(R.anim.page_in, R.anim.page_out)
        }

        binding.ratingBar.setOnItemSelectedListener(object : RatingBar.OnItemSelectedListener {
            override fun onItemSelected(type: RatingBar.Type) {
                AppReport.reportClickRate(ReportConstants.AppReport.SOURCE_DISCONNECTED_REPORT_PAGE, type)
                when(type) {
                    RatingBar.Type.TERRIBLE, RatingBar.Type.BAD, RatingBar.Type.OK -> {
                        SupportUtils.sendFeedback(this@DisconnectReportActivity)
                    }
                    RatingBar.Type.GREAT, RatingBar.Type.GOOD -> {
                        if (AppStoreUtils.openPlayStoreOnlyWithUrl(this@DisconnectReportActivity, SupportUtils.GP_LINK)) {
                            RatingGuideToast.go(this@DisconnectReportActivity)
                        }
                    }
                }
            }
        })
    }

    private fun setConnectedTime(milliseconds: Long) {
        val connectedSeconds: Long = milliseconds / 1000
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
        binding.hourNumber.text = TimeUtils.leastTwoDigitsFormat(hour)
        binding.minuteNumber.text = TimeUtils.leastTwoDigitsFormat(minute)
        binding.secondNumber.text = TimeUtils.leastTwoDigitsFormat(second)
    }

    private fun initViewModel() {
        AdPresenterWrapper.getInstance().nativeAdLoadLiveData.observe(this) { result ->
            if (result && AdPresenterWrapper.getInstance().isNativeAdLoaded(AdConstant.AdPlacement.N_DISCONNECT_REPORT)) {
                binding.nativeAdContainer.apply {
                    visibility = View.VISIBLE
                    removeAllViews()
                    AdPresenterWrapper.getInstance().getNativeAdSmallView(
                        ViewStyle.WHITE,
                        AdConstant.AdPlacement.N_DISCONNECT_REPORT, this, null)?.let {
                        addView(it) }
                }
            }
        }
        mConnectedViewModel =
            ViewModelProvider(this).get(ConnectedViewModel::class.java)
    }

    override fun onBackPressed() {
        setResult(RESULT_OK)
        super.onBackPressed()
    }

    companion object {
        const val EXTRA_REGION_CODE = "extra_region_code"
        const val EXTRA_REGION_NAME = "extra_region_name"
        const val EXTRA_CONNECTED_MILLISECONDS = "extra_connected_milliseconds"
        const val EXTRA_TRAFFIC_STATS = "extra_traffic_stats"
    }
}