package com.vpn.android.ads

import com.vpn.android.MainApplication
import com.vpn.android.base.utils.LogUtils
import com.vpn.android.config.RemoteConfigManager
import com.vpn.android.region.RegionConstants.KEY_DAILY_AD_CLICKS
import com.vpn.android.region.RegionConstants.KEY_LAST_RESET_DATE
import com.vpn.base.vstore.VstoreManager
import java.text.SimpleDateFormat
import java.util.*

object AdClickManager {
    private val adClickMap = mutableMapOf<Int, AdClickDetector>()
    
    // 记录每日不同广告的点击（需要物理存储）
    private val dailyAdClicks = mutableSetOf<Int>()
    private var lastResetDate = getCurrentDate()

    fun onAdClick(adHashCode: Int, onDetected: () -> Unit, onMultipleAdsDetected: () -> Unit): Boolean {
        // 检查并重置每日计数
        checkAndResetDailyCount()
        
        val detector = adClickMap.getOrPut(adHashCode) { AdClickDetector() }

        // 先记录这次点击
        detector.recordClick()

        // 全局开关已关闭
        if (!AdPresenterWrapper.getInstance().isAdTurnOn()) {
            return false
        }

        // 检查单个广告高频点击
        if (detector.isHighFrequencyClick()) {
            AdPresenterWrapper.getInstance().turnOffAd()
            onDetected.invoke()
            return false
        }
        
        // 检查是否是新广告（非连击）
        if (!detector.isContinuousClick()) {
            dailyAdClicks.add(adHashCode)
            
            saveDailyAdClicks()
            
            // 检查每日不同广告点击次数
            if (dailyAdClicks.size > RemoteConfigManager.getInstance().maximumInterstitialAdClickLimit) {
                AdPresenterWrapper.getInstance().turnOffAd()
                onMultipleAdsDetected.invoke()
                LogUtils.e("AdQualityReporter", "onMultipleAdsDetected")
                return false
            }
        }

        return true
    }
    
    private fun checkAndResetDailyCount() {
        val currentDate = getCurrentDate()
        if (currentDate != lastResetDate) {
            dailyAdClicks.clear()
            lastResetDate = currentDate
            clearDailyAdClicksFromStorage()
        } else {
            loadDailyAdClicksFromStorage()
        }
    }
    
    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
    
    private fun saveDailyAdClicks() {
        // 保存 dailyAdClicks 到 VstoreManager
        val vstoreManager = VstoreManager.getInstance(MainApplication.instance)
        
        // 将 Set<Int> 转换为 Set<String> 保存
        val stringSet = dailyAdClicks.map { it.toString() }.toSet()
        
        vstoreManager.encode(true, KEY_DAILY_AD_CLICKS, stringSet)
        vstoreManager.encode(true, KEY_LAST_RESET_DATE, lastResetDate)
    }
    
    private fun loadDailyAdClicksFromStorage() {
        // 从存储中读取 dailyAdClicks
        val vstoreManager = VstoreManager.getInstance(MainApplication.instance)
        
        val stringSet = vstoreManager.decode(true, KEY_DAILY_AD_CLICKS, emptySet<String>())
        dailyAdClicks.clear()
        dailyAdClicks.addAll(stringSet.map { it.toInt() })
        
        lastResetDate = vstoreManager.decode(true, KEY_LAST_RESET_DATE, getCurrentDate())
    }
    
    private fun clearDailyAdClicksFromStorage() {
        // 清理存储中的每日广告点击记录
        val vstoreManager = VstoreManager.getInstance(MainApplication.instance)
        
        vstoreManager.remove(true, KEY_DAILY_AD_CLICKS)
        vstoreManager.remove(true, KEY_LAST_RESET_DATE)
    }
}

class AdClickDetector {
    // 内存存储，用于判断连击和高频点击
    private val clickTimes = mutableListOf<Long>()
    private val maxClicks = 3
    private val timeWindow = 100L
    private val continuousTimeWindow = 1000L // 1秒内算连击

    fun recordClick() {
        val currentTime = System.currentTimeMillis()
        clickTimes.add(currentTime)
        clickTimes.removeAll { currentTime - it > timeWindow }
    }

    fun isHighFrequencyClick(): Boolean {
        return clickTimes.size >= maxClicks
    }
    
    fun isContinuousClick(): Boolean {
        if (clickTimes.size < 2) return false
        
        val lastClickTime = clickTimes.last()
        val secondLastClickTime = clickTimes[clickTimes.size - 2]
        
        // 如果两次点击间隔小于1秒，认为是连击
        return (lastClickTime - secondLastClickTime) < continuousTimeWindow
    }
}