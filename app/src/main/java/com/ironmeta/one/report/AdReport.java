package com.ironmeta.one.report;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.ads.AdValue;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.ironmeta.base.vstore.VstoreManager;
import com.ironmeta.one.ads.constant.AdFormat;
import com.ironmeta.one.vlog.VlogManager;

public class AdReport {
    private static String TAICHI_TROAS_CACHE = "TaichiTroasCache";
    private static String EVENT_NAME_AD_IMPRESSION_REVENUE = "Ad_Impression_Revenue";
    private static String EVENT_NAME_TOTAL_ADS_REVENUE_001 = "Total_Ads_Revenue_001";


    public static void reportAdImpressionRevenue(AdValue adValue, AdFormat adFormat, Context context) {
        VstoreManager vstoreManager = VstoreManager.getInstance(context.getApplicationContext());
        VlogManager vlogManager = VlogManager.getInstance(context.getApplicationContext());
        Bundle params = new Bundle();
        double currentImpressionRevenue = (double) adValue.getValueMicros()/ (double) 1000000;
        //getValueMicros() returns the value of the ad in micro units. For example, a getValueMicros() return value of 5,000 means the ad is estimated to be worth $0.005.
        params.putDouble(FirebaseAnalytics.Param.VALUE, currentImpressionRevenue);
        params.putString(FirebaseAnalytics.Param.CURRENCY, "USD");
        String precisionType;
        switch(adValue.getPrecisionType()){
            case 0 : precisionType = "UNKNOWN";break;
            case 1 : precisionType = "ESTIMATED";break;
            case 2 : precisionType = "PUBLISHER_PROVIDED";break;
            case 3 : precisionType = "PRECISE";break;
            default: precisionType = "Invalid";break;
        }
        params.putString("precisionType", precisionType);//(Optional) 记录PrecisionType
        vlogManager.logEvent(EVENT_NAME_AD_IMPRESSION_REVENUE, params);// 给Taichi用
        double previousTaichiTroasCache = vstoreManager.decode(true, TAICHI_TROAS_CACHE, (double) 0); //App本地存储用 于累计tROAS的缓存值,sharedPref只是作为事例，可以选择其它本地存储的方式
        double currentTaichiTroasCache = (previousTaichiTroasCache + currentImpressionRevenue);//累加tROAS的缓存值
        //check是否应该发送TaichitROAS事件
        if (currentTaichiTroasCache >= 0.01) {//如果超过0.01就触发一次tROAS taichi事件
            Bundle roasbundle = new Bundle();
            roasbundle.putDouble(FirebaseAnalytics.Param.VALUE, currentTaichiTroasCache);//(Required)tROAS事件必须带Double类型的Value
            roasbundle.putString(FirebaseAnalytics.Param.CURRENCY, "USD");//(Required)tROAS事件必须 带Currency的币种，如果是USD的话，就写USD，如果不是USD，务必把其他币种换算成USD
            vlogManager.logEvent(EVENT_NAME_TOTAL_ADS_REVENUE_001, roasbundle);
            vstoreManager.encode(true, TAICHI_TROAS_CACHE, (double) 0);//重新清零，开始计算
        } else {
            vstoreManager.encode(true, TAICHI_TROAS_CACHE, currentTaichiTroasCache);//先存着直到超过0.01才发送 
        }
    }
}
