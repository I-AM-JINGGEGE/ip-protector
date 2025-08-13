package com.vpn.android.ui.common

import android.app.ProgressDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.gyf.immersionbar.ktx.immersionBar
import com.gyf.immersionbar.BarHide
import com.vpn.android.R
import com.vpn.android.ads.AdPresenterWrapper
import com.vpn.android.ui.helper.LanguageSettingHelper

open class CommonAppCompatActivity : AppCompatActivity() {
    
    companion object {
        const val STATUS_BAR_TRANSPARENT = 0
        const val STATUS_BAR_COLORED = 1
        const val STATUS_BAR_GRADIENT = 2
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageSettingHelper.getInstance(this).initLanguageLocale(this)
        super.onCreate(savedInstanceState)
        
        // 默认沉浸式状态栏配置
        setupImmersiveStatusBar()
        
        AdPresenterWrapper.getInstance().destroyShownNativeAd()
    }

    /**
     * 设置沉浸式状态栏（默认配置）
     */
    protected fun setupImmersiveStatusBar() {
        immersionBar {
            statusBarColor(R.color.colorPrimary)
            navigationBarColor(R.color.colorPrimary)
            statusBarDarkFont(false)
            navigationBarDarkIcon(false)
        }
    }
    
    /**
     * 设置透明状态栏
     */
    protected fun setTransparentStatusBar() {
        immersionBar {
            transparentStatusBar()
            statusBarDarkFont(false)
            navigationBarColor(R.color.colorPrimary)
            navigationBarDarkIcon(false)
        }
    }
    
    /**
     * 设置状态栏颜色（使用资源ID）
     */
    protected fun setStatusBarColorRes(colorResId: Int, isDarkFont: Boolean = false) {
        immersionBar {
            statusBarColor(colorResId)
            statusBarDarkFont(isDarkFont)
            navigationBarColor(R.color.colorPrimary)
            navigationBarDarkIcon(false)
        }
    }
    
    /**
     * 设置状态栏颜色（使用颜色值）
     */
    protected fun setStatusBarColor(color: Int, isDarkFont: Boolean = false) {
        immersionBar {
            statusBarColor(color)
            statusBarDarkFont(isDarkFont)
            navigationBarColor(R.color.colorPrimary)
            navigationBarDarkIcon(false)
        }
    }
    
    /**
     * 设置渐变状态栏
     */
    protected fun setGradientStatusBar(startColor: Int, endColor: Int, isDarkFont: Boolean = false) {
        immersionBar {
            statusBarColor(startColor)
            statusBarDarkFont(isDarkFont)
            navigationBarColor(R.color.colorPrimary)
            navigationBarDarkIcon(false)
        }
    }
    
    /**
     * 设置全屏沉浸式（隐藏状态栏和导航栏）
     */
    protected fun setFullScreenImmersive() {
        immersionBar {
            fullScreen(true)
            hideBar(BarHide.FLAG_HIDE_STATUS_BAR)
        }
    }
    
    /**
     * 设置状态栏文字颜色
     */
    protected fun setStatusBarTextColor(isDark: Boolean) {
        immersionBar {
            statusBarDarkFont(isDark)
        }
    }
    
    /**
     * 设置导航栏颜色（使用资源ID）
     */
    protected fun setNavigationBarColorRes(colorResId: Int, isDarkIcon: Boolean = false) {
        immersionBar {
            navigationBarColor(colorResId)
            navigationBarDarkIcon(isDarkIcon)
        }
    }
    
    /**
     * 设置导航栏颜色（使用颜色值）
     */
    protected fun setNavigationBarColor(color: Int, isDarkIcon: Boolean = false) {
        immersionBar {
            navigationBarColor(color)
            navigationBarDarkIcon(isDarkIcon)
        }
    }
    
    /**
     * 设置状态栏高度为内容区域的padding
     */
    protected fun setStatusBarPadding(view: View) {
        val statusBarHeight = getStatusBarHeight()
        view.setPadding(
            view.paddingLeft,
            view.paddingTop + statusBarHeight,
            view.paddingRight,
            view.paddingBottom
        )
    }
    
    /**
     * 获取状态栏高度
     */
    protected open fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }
    
    /**
     * 设置系统UI可见性
     */
    protected fun setSystemUiVisibility(visibility: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            window.decorView.systemUiVisibility = visibility
        }
    }
    
    /**
     * 设置窗口标志
     */
    protected fun setWindowFlags(flags: Int, mask: Int) {
        window.setFlags(flags, mask)
    }
    
    /**
     * 清除窗口标志
     */
    protected fun clearWindowFlags(flags: Int) {
        window.clearFlags(flags)
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
    }

    override fun onPause() {
        super.onPause()
    }
}