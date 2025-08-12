package com.vpn.android.report

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.activity.result.contract.ActivityResultContract

class RequestVpnPermissionContract : ActivityResultContract<Unit, Boolean>() {
    override fun createIntent(context: Context, input: Unit): Intent =
        VpnService.prepare(context)

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
        resultCode == Activity.RESULT_OK

    override fun getSynchronousResult(
        context: Context,
        input: Unit
    ): SynchronousResult<Boolean>? =
        if (VpnService.prepare(context) != null) {
            null
        } else {
            SynchronousResult(true)
        }
}