package com.ironmeta.one

import android.content.Intent
import android.os.Bundle
import com.ironmeta.one.ui.common.CommonAppCompatActivity

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