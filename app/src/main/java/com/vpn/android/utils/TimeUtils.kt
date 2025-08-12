package com.vpn.android.utils

import android.content.Context
import com.vpn.base.vstore.VstoreManager
import com.vpn.android.constants.KvStoreConstants

object TimeUtils {
    fun leastTwoDigitsFormat(timeCount: Int): String {
        val retStr: String = if (timeCount in 0..9) {
            "0$timeCount"
        } else {
            "$timeCount"
        }
        return retStr
    }

    fun isNewUser(context: Context): Boolean {
        val installationTime = VstoreManager.getInstance(context)
            .decode(true, KvStoreConstants.KEY_APP_INSTALLATION_TIME, 0L)
        return (System.currentTimeMillis() - installationTime) <= 48 * 60 * 60 * 1000
    }
}