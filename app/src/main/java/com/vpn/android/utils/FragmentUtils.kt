package com.vpn.android.utils

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.vpn.android.base.utils.LogUtils
import java.lang.Exception

object FragmentUtils {
    fun safeCommitReplace(
        manager: FragmentManager,
        fragmentClass: Class<out Fragment>,
        args: Bundle?,
        layoutId: Int,
        animIn: Int,
        animOut: Int
    ) {
        if (manager.isDestroyed || manager.isStateSaved) {
            LogUtils.logException(IllegalStateException("Fragment commit after fragment manager [Destroyed=${manager.isDestroyed}, StateSaved=${manager.isStateSaved}"))
            return
        }
        try {
            manager.beginTransaction()
                .setCustomAnimations(animIn, animOut)
                .replace(layoutId, fragmentClass, args).commitAllowingStateLoss()
        } catch (e: Exception) {
            LogUtils.logException(IllegalStateException("Fragment commitAllowingStateLoss exception", e))
        }
    }
}