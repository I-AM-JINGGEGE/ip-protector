package com.vpn.android.ads.proxy

interface AdLoadListener {
    fun onAdLoaded()
    fun onFailure(errorCode: Int, errorMessage: String)
}