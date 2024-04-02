package com.ironmeta.one.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.ironmeta.one.MainActivity
import com.ironmeta.one.MainActivityViewModel
import com.ironmeta.one.MainApplication
import com.ironmeta.one.R
import com.ironmeta.one.ads.AdPresenterWrapper
import com.ironmeta.one.ads.constant.AdConstant
import com.ironmeta.one.ads.format.ViewStyle
import com.ironmeta.one.base.utils.LogUtils
import com.ironmeta.one.base.utils.ToastUtils
import com.ironmeta.one.coreservice.CoreServiceManager
import com.ironmeta.one.coreservice.FakeConnectingProgressManager
import com.ironmeta.one.coreservice.FakeConnectingProgressManager.Companion.getInstance
import com.ironmeta.one.coreservice.FakeConnectionState
import com.ironmeta.one.databinding.DisconnectFragmentLayoutBinding
import com.ironmeta.one.region.RegionUtils
import com.ironmeta.one.report.AppReport
import com.ironmeta.one.report.ReportConstants
import com.ironmeta.one.report.RequestVpnPermissionContract
import com.ironmeta.one.report.VpnReporter
import com.ironmeta.one.ui.common.CommonFragment
import com.ironmeta.one.ui.regionselector2.ServerListActivity
import com.ironmeta.tahiti.TahitiCoreServiceStateInfoManager
import com.ironmeta.tahiti.constants.CoreServiceStateConstants
import com.sdk.ssmod.IMSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.jvm.internal.Ref

class DisconnectFragment : CommonFragment {
    companion object {
        const val EXTRA_AUTO_CONNECT = "extra_auto_connect"
    }
    constructor() : super()

    private var _binding: DisconnectFragmentLayoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var mMainActivityViewModel: MainActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DisconnectFragmentLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()
    }

    private fun initView() {
        binding.disconnectAnimLayout.setOnClickListener {
            connect(VpnReporter.PARAM_VALUE_FROM_BUTTON)
        }
        binding.clickTipsText.setOnClickListener {
            connect(VpnReporter.PARAM_VALUE_FROM_TAP_TEXT)
        }
        binding.serverInter.setOnClickListener {
            if (getInstance().isStart() || getInstance().isWaitingForConnecting() || getInstance().isProgressingAfterConnected()) {
                context?.apply {
                    ToastUtils.showToast(this, this.getString(R.string.vs_core_service_state_connecting2))
                }
                return@setOnClickListener
            }
            if (activity is MainActivity) {
                (activity as MainActivity).launchActivityForShowingAds(Intent(requireActivity(), ServerListActivity::class.java))
            }
        }
        playIdleAnimations()
        arguments?.apply {
            if (getBoolean(EXTRA_AUTO_CONNECT)) {
                connect(VpnReporter.PARAM_VALUE_FROM_FIX_NETWORK)
            }
        }
    }

    fun connect(from: String) {
        if (getInstance().isStart() || getInstance().isWaitingForConnecting() || getInstance().isProgressingAfterConnected()) {
            context?.apply {
                ToastUtils.showToast(this, this.getString(R.string.vs_core_service_state_connecting2))
            }
            return
        }
        GlobalScope.launch {
            VpnReporter.reportToStartConnect(from)
            if (!requestVpnPermission()) {
                return@launch
            }
            VpnReporter.reportStartConnect(from)
            CoreServiceManager.getInstance(requireContext()).connect(null)
            getInstance().stateLiveData.postValue(FakeConnectionState(FakeConnectionState.STATE_START, 0F))
        }
    }
    private var requestVpnPermissionCallbackRef = Ref.ObjectRef<(Boolean) -> Unit>()
    private val requestVpnPermissionContract =
        registerForActivityResult(RequestVpnPermissionContract()) {
            requestVpnPermissionCallbackRef.element?.invoke(it)
        }
    private suspend fun requestVpnPermission() = suspendCoroutine<Boolean> { continuation ->
        requestVpnPermissionCallbackRef.element = {
            requestVpnPermissionCallbackRef.element = null
            continuation.resume(it)
        }
        requestVpnPermissionContract.launch(Unit)
    }

    private fun playIdleAnimations() {
        binding.finger.apply {
            AnimationSet(true).apply {
                addAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.finger_anim_alpha))
                addAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.finger_anim_scale))
                addAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.finger_anim_translate))
                startAnimation(this)
            }
        }
        AnimationUtils.loadAnimation(requireContext(), R.anim.connecting_anim_blue).apply {
            binding.blueRing.startAnimation(this)
        }
        AnimationUtils.loadAnimation(requireContext(), R.anim.connecting_anim_black).apply {
            binding.blackRing.startAnimation(this)
        }
        binding.menu.setOnClickListener {
            if (activity is OnSlideClickListener) {
                (activity as OnSlideClickListener).onSlideClick()
            }
        }
    }

    private fun initViewModel() {
        mMainActivityViewModel = ViewModelProvider(this).get(
            MainActivityViewModel::class.java
        )
        AdPresenterWrapper.getInstance().nativeAdLoadLiveData.observe(viewLifecycleOwner) { result ->
            if (result && !TahitiCoreServiceStateInfoManager.getInstance(requireContext()).coreServiceConnected) {
                binding.nativeAdContainer.apply {
                    removeAllViews()
                    AdPresenterWrapper.getInstance().getNativeAdSmallView(
                        ViewStyle.BLACK,
                        AdConstant.AdPlacement.N_DISCONNECT, this, null)?.let { addView(it) }
                }
            }
        }
        mMainActivityViewModel.toolbarRegionIdAsLiveData.observe(viewLifecycleOwner) { codeResult: String? ->
            codeResult?.apply {
                Glide.with(requireActivity()).load(
                    RegionUtils.getRegionFlagImageResource(
                        requireContext(),this)
                ).into(binding.regionImage)
                binding.regionText.text = RegionUtils.getRegionName(requireContext(), this)
            }
        }

        TahitiCoreServiceStateInfoManager.getInstance(MainApplication.context).getCoreServiceStateAsLiveData().observe(viewLifecycleOwner) { coreServiceState ->
            updateCoreServiceStateRelatedUI(coreServiceState)
        }

        FakeConnectingProgressManager.getInstance().stateLiveData.observe(viewLifecycleOwner) {
            when(it.state) {
                FakeConnectionState.STATE_START -> {
                    AnimationUtils.loadAnimation(requireContext(), R.anim.connecting_anim_lighting).apply {
                        binding.connectingLight.startAnimation(this)
                    }
                    setUiToConnecting()
                }
                FakeConnectionState.STATE_WAITING -> {
                    updateConnectingProgress(it.progress)
                }
                FakeConnectionState.STATE_CONNECTING -> {
                    updateConnectingProgress(it.progress)
                }
                FakeConnectionState.STATE_FINISH -> {
                    binding.connectingLight.clearAnimation()
                }
            }
        }
    }

    private fun updateCoreServiceStateRelatedUI(coreServiceState: IMSDK.VpnState) {
        when {
            CoreServiceStateConstants.isDisconnected(coreServiceState) -> {
                setUiToDisconnect()
            }
        }
    }

    private fun updateConnectingProgress(progress: Float) {
        binding.connectingProgress.update(progress)
    }

    private fun setUiToConnecting() {
        binding.disconnectAnimLayout.isClickable = false
        binding.disconnectAnimLayout.visibility = View.INVISIBLE
        binding.connectErrorTips.visibility = View.GONE
        binding.clickTipsText.visibility = View.INVISIBLE
        binding.connectingAnimLayout.visibility = View.VISIBLE
        binding.connectStateText.setText(R.string.vs_core_service_state_testing)
        binding.connectStateText.setTextColor(resources.getColor(R.color.state_connecting))
        binding.finger.clearAnimation()
        binding.blueRing.clearAnimation()
        binding.blackRing.clearAnimation()
    }

    private fun setUiToDisconnect() {
        binding.disconnectAnimLayout.isClickable = true
        binding.disconnectAnimLayout.visibility = View.VISIBLE
        binding.connectingAnimLayout.visibility = View.GONE
        binding.connectErrorTips.visibility = View.GONE
        binding.clickTipsText.visibility = View.VISIBLE
        binding.connectStateText.setText(R.string.vs_core_service_state_disconnected)
        binding.connectStateText.setTextColor(resources.getColor(R.color.state_disconnect))
        playIdleAnimations()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.finger.clearAnimation()
        _binding = null
    }
}