package com.vpn.android.ads.proxy

interface AdShowListener {
    fun onAdShown()
    fun onAdFailToShow(errorCode: Int, errorMessage: String)
    fun onAdClosed()
    fun onAdClicked()
}