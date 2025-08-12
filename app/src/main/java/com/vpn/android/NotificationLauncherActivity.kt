package com.vpn.android

import android.content.Intent
import android.os.Bundle
import com.vpn.android.ui.common.CommonAppCompatActivity

class NotificationLauncherActivity : CommonAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(generateIntent())
        finish()
    }

    private fun generateIntent(): Intent {
        return Intent(this, MainActivity::class.java).apply {
            intent.extras?.let { putExtras(it) }
        }
    }
}