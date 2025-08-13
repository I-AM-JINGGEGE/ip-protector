package com.vpn.android.ui.common

import android.app.ProgressDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gyf.immersionbar.ktx.immersionBar
import com.vpn.android.R
import com.vpn.android.ads.AdPresenterWrapper
import com.vpn.android.ui.helper.LanguageSettingHelper
import com.jaeger.library.StatusBarUtil

open class CommonAppCompatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageSettingHelper.getInstance(this).initLanguageLocale(this)
        super.onCreate(savedInstanceState)
        immersionBar {
            statusBarColor(R.color.colorPrimary)
            navigationBarColor(R.color.colorPrimary)
        }
        AdPresenterWrapper.getInstance().destroyShownNativeAd()
        StatusBarUtil.setTransparent(this)
    }

    override fun onStop() {
        super.onStop()
        AdPresenterWrapper.getInstance().destroyShownNativeAd()
    }

    private var mProgressDialog: ProgressDialog? = null
    fun showLoading(message: String?, cancelable: Boolean) {
        if (isDestroyed) {
            return
        }
        cancelLoading()
        mProgressDialog = ProgressDialog.show(this, "", message, true, cancelable)
    }

    fun cancelLoading() {
        if (isDestroyed) {
            return
        }
        if (mProgressDialog == null) {
            return
        }
        mProgressDialog!!.cancel()
        mProgressDialog = null
    }

    override fun onResume() {
        super.onResume()
//        Adjust.onResume()
    }

    override fun onPause() {
        super.onPause()
//        Adjust.onPause()
    }
}