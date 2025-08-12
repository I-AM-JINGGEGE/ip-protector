package com.vpn.android.ads.bean

import com.google.gson.annotations.SerializedName

class UserAdConfig {
    @SerializedName("ad_config")
    var adConfig: AdConfig = AdConfig()

    class AdConfig {
        @SerializedName("before_connect")
        var adUnitSetBeforeConnect: AdUnitSet = AdUnitSet()

        @SerializedName("after_connect")
        var adUnitSetAfterConnect: AdUnitSet = AdUnitSet()

    }

    class AdUnitSet {
        @SerializedName("switch")
        var switch: Boolean? = null

        @SerializedName("banner")
        var banner: List<AdUnit>? = null

        @SerializedName("interstitial")
        var interstitial: List<AdUnit>? = null

        @SerializedName("native")
        var native: List<AdUnit>? = null

        @SerializedName("rewarded")
        var rewarded: List<AdUnit>? = null

        @SerializedName("open")
        var appOpen: List<AdUnit>? = null
    }

    class AdUnit {
        @SerializedName("ad_unit_id")
        lateinit var id: String

        @SerializedName("ad_platform")
        lateinit var adPlatform: String
    }
}