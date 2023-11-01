package com.ironmeta.one.ads.proxy

interface AdLoadListener {
    fun onAdLoaded()
    fun onFailure(errorCode: Int, errorMessage: String)
}