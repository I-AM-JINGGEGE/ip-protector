package com.vpn.android.ui.splash;

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vpn.android.MainApplication
import com.vpn.android.databinding.AddTimeLoadingBinding
import com.vpn.android.ui.common.CommonDialogFragment

class AddTimeLoadingFragment : CommonDialogFragment {

    constructor() : super()

    private var _binding: AddTimeLoadingBinding? = null
    private val binding get() = _binding!!

    override fun onStart() {
        super.onStart()
        val dm = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(dm)
        dialog?.apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
        dialog?.window?.apply {
            setLayout(dm.widthPixels, dialog!!.window!!.attributes.height)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AddTimeLoadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
