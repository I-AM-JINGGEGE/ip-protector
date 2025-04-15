package com.ironmeta.one.ui.common

import android.app.ProgressDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gyf.immersionbar.ktx.immersionBar
import com.ironmeta.one.R
import com.ironmeta.one.ads.AdPresenterWrapper
import com.ironmeta.one.ui.helper.LanguageSettingHelper
import com.jaeger.library.StatusBarUtil
import me.jessyan.autosize.internal.CustomAdapt

open class CommonAppCompatActivity : AppCompatActivity(), CustomAdapt {
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

    override fun isBaseOnWidth(): Boolean {
        return false
    }

    override fun getSizeInDp(): Float {
        return 680F
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