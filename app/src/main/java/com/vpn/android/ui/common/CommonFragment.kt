package com.vpn.android.ui.common

import androidx.fragment.app.Fragment
import me.jessyan.autosize.internal.CustomAdapt

open class CommonFragment : Fragment, CustomAdapt {
    constructor() : super()
    constructor(contentLayoutId: Int) : super(contentLayoutId) {

    }

    override fun isBaseOnWidth(): Boolean {
        return false
    }

    override fun getSizeInDp(): Float {
        return 640F
    }
}