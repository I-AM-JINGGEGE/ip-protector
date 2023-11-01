package com.ironmeta.one.ui.common

import androidx.fragment.app.DialogFragment
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.internal.CustomAdapt

open class CommonDialogFragment : DialogFragment, CustomAdapt {
    constructor() : super() {
        AutoSizeConfig.getInstance().isCustomFragment = true
    }
    constructor(contentLayoutId: Int) : super(contentLayoutId) {

    }

    override fun isBaseOnWidth(): Boolean {
        return false
    }

    override fun getSizeInDp(): Float {
        return 640F
    }
}