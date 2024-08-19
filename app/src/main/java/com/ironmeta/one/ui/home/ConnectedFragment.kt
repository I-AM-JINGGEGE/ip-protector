package com.ironmeta.one.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.ironmeta.one.MainActivity
import com.ironmeta.one.MainActivityViewModel
import com.ironmeta.one.MainApplication
import com.ironmeta.one.R
import com.ironmeta.one.ads.AdPresenterWrapper
import com.ironmeta.one.ads.constant.AdConstant
import com.ironmeta.one.ads.constant.AdFormat
import com.ironmeta.one.ads.format.ViewStyle
import com.ironmeta.one.config.RemoteConfigManager
import com.ironmeta.one.databinding.ConnectedFragmentLayoutBinding
import com.ironmeta.one.region.RegionUtils
import com.ironmeta.one.report.ReportConstants
import com.ironmeta.one.ui.common.CommonDialog
import com.ironmeta.one.ui.common.CommonFragment
import com.ironmeta.one.ui.dialog.ConnectivityTestDialog
import com.ironmeta.one.ui.dialog.ConnectivityTestDialog.DialogListener
import com.ironmeta.one.ui.regionselector.card.ConnectedViewModel
import com.ironmeta.one.ui.regionselector2.ServerListActivity
import com.ironmeta.one.utils.TimeUtils.leastTwoDigitsFormat
import com.ironmeta.tahiti.TahitiCoreServiceStateInfoManager
import com.ironmeta.tahiti.constants.CoreServiceStateConstants
import org.libpag.PAGFile


class ConnectedFragment : CommonFragment {
    constructor() : super()

    private var _binding: ConnectedFragmentLayoutBinding? = null
    private val binding get() = _binding!!
    private lateinit var mMainActivityViewModel: MainActivityViewModel
    private lateinit var mConnectedViewModel: ConnectedViewModel

    private var mWillDisconnectDialog: CommonDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ConnectedFragmentLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()
    }

    private fun initView() {
        binding.clickTipsText.setOnClickListener {
            stopCoreService()
        }
        binding.connectAnimLayout.setOnClickListener {
            if (activity is OnAddTimeClickListener) {
                (activity as OnAddTimeClickListener).onAddTwoHourClick()
            }
        }
        binding.speed.setOnClickListener {
            AdPresenterWrapper.getInstance().loadAdExceptNative(
                AdFormat.INTERSTITIAL,
                AdConstant.AdPlacement.I_CONNECTIVITY_TEST,
                null, "network test[main]")
            ConnectivityTestDialog(requireActivity()).apply {
                setCancelable(false)
                setDialogOnClickListener(object : DialogListener {
                    override fun onCloseClick() {
                        dismiss()
                        AdPresenterWrapper.getInstance().apply {
                            logToShow(AdFormat.INTERSTITIAL, AdConstant.AdPlacement.I_CONNECTIVITY_TEST)
                            showAdExceptNative(
                                requireActivity(), AdFormat.INTERSTITIAL,
                                AdConstant.AdPlacement.I_CONNECTIVITY_TEST,
                                null
                            )
                        }
                    }

                    override fun onRetestClick() {
                        dismiss()
                        binding.speed.performClick()
                    }
                })
                show()
            }
        }
        binding.addTime1.setOnClickListener {
            if (activity is OnAddTimeClickListener) {
                (activity as OnAddTimeClickListener).onAddOneHourClick(ReportConstants.AppReport.SOURCE_ADD_TIME_MAIN_PAGE_1)
            }
        }
        binding.serverInter.setOnClickListener {
            if (activity is MainActivity) {
                (activity as MainActivity).launchActivityForShowingAds(Intent(requireActivity(), ServerListActivity::class.java))
            }
        }
        binding.repairNetwork.setOnClickListener{
            if (activity is OnReconnectClickListener) {
                (activity as OnReconnectClickListener).onReconnectClick()
            }
        }

        binding.menu.setOnClickListener {
            if (activity is OnSlideClickListener) {
                (activity as OnSlideClickListener).onSlideClick()
            }
        }
        binding.report.setOnClickListener {
            if (activity is OnConnectedReportClickListener) {
                (activity as OnConnectedReportClickListener).onConnectedReportClick()
            }
        }
        playConnectedAnimation()
    }

    private fun playConnectedAnimation() {
        AnimationUtils.loadAnimation(requireContext(), R.anim.connected_anim_inverse).apply {
            interpolator = LinearInterpolator()
            binding.connectedWheel.startAnimation(this)
        }
        AnimationUtils.loadAnimation(requireContext(), R.anim.connected_anim_clockwise).apply {
            interpolator = LinearInterpolator()
            binding.connectedProgressbar.startAnimation(this)
        }

        binding.light.apply {
            composition =
                PAGFile.Load(MainApplication.context.assets, "add_time_light_3s.pag")
            setRepeatCount(0)
            play()
        }
        binding.addTime1Clock.apply {
            composition =
                PAGFile.Load(MainApplication.context.assets, "clock.pag")
            setRepeatCount(0)
            play()
        }
    }

    private fun initViewModel() {
        mMainActivityViewModel = ViewModelProvider(this).get(
            MainActivityViewModel::class.java
        )
        mMainActivityViewModel.toolbarRegionIdAsLiveData.observe(viewLifecycleOwner) { codeResult: String? ->
            codeResult?.apply {
                Glide.with(requireActivity()).load(
                    RegionUtils.getRegionFlagImageResource(
                        requireContext(),this)
                ).into(binding.regionImage)
                binding.regionText.text = RegionUtils.getRegionName(requireContext(), this)
            }
        }
        mConnectedViewModel =
            ViewModelProvider(requireActivity()).get(ConnectedViewModel::class.java)
        mConnectedViewModel.usedUpRemainSecondsAsLiveData.observe(viewLifecycleOwner) { connectedSeconds ->
            if (connectedSeconds == null) {
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
            binding.hourNumber.text = leastTwoDigitsFormat(hour)
            binding.minuteNumber.text = leastTwoDigitsFormat(minute)
            binding.secondNumber.text = leastTwoDigitsFormat(second)
        }

        TahitiCoreServiceStateInfoManager.getInstance(MainApplication.context).getCoreServiceStateAsLiveData()
            .observe(viewLifecycleOwner) { coreServiceState ->
            if (CoreServiceStateConstants.isDisconnecting(coreServiceState)) {
                binding.connectStateText.setText(R.string.vs_core_service_state_disconnecting)
            }
        }
    }

    private val observer = Observer<Boolean> { result ->
        if (result && RemoteConfigManager.getInstance().connectedNativeAdSwitch) {
            binding.nativeAdContainer.apply {
                removeAllViews()
                AdPresenterWrapper.getInstance().getNativeAdSmallView(
                    ViewStyle.WHITE, AdConstant.AdPlacement.N_CONNECTED,
                    this,
                    null
                )?.let {
                    addView(it)
                    invalidate()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AdPresenterWrapper.getInstance().nativeAdLoadLiveData.observe(viewLifecycleOwner, observer)
    }

    override fun onPause() {
        super.onPause()
        AdPresenterWrapper.getInstance().nativeAdLoadLiveData.removeObserver(observer)
    }

    private fun stopCoreService() {
        if (mWillDisconnectDialog != null) {
            mWillDisconnectDialog!!.cancel()
            mWillDisconnectDialog = null
        }
        mWillDisconnectDialog = CommonDialog(requireActivity())
        mWillDisconnectDialog!!.setTitle(resources.getString(R.string.vs_will_disconnect_dialog_title))
        mWillDisconnectDialog!!.setMessage(resources.getString(R.string.vs_will_disconnect_dialog_desc_for_disconnect2))
        mWillDisconnectDialog!!.setOKButton(resources.getString(R.string.vs_will_disconnect_dialog_cancel))
        mWillDisconnectDialog!!.setOkOnclickListener { mWillDisconnectDialog!!.cancel() }
        mWillDisconnectDialog!!.setCancelButton(resources.getString(R.string.vs_will_disconnect_dialog_ok))
        mWillDisconnectDialog!!.setCancelOnclickListener {
            mWillDisconnectDialog!!.cancel()
            if (activity is OnClickDisconnectListener) {
                (activity as OnClickDisconnectListener).onClickDisconnect()
            }
        }
        mWillDisconnectDialog!!.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}