package com.vpn.android.ads

object AdClickManager {
    private val adClickMap = mutableMapOf<Int, AdClickDetector>()

    fun onAdClick(adHashCode: Int): Boolean {
        val detector = adClickMap.getOrPut(adHashCode) { AdClickDetector() }

        // 先记录这次点击
        detector.recordClick()

        // 全局开关已关闭
        if (!AdPresenterWrapper.getInstance().isAdTurnOn()) {
            return false
        }

        // 检查是否高频点击
        if (detector.isHighFrequencyClick()) {
            AdPresenterWrapper.getInstance().turnOffAd()
            return false
        }

        return true
    }
}

class AdClickDetector {
    private val clickTimes = mutableListOf<Long>()
    private val maxClicks = 3
    private val timeWindow = 100L

    fun recordClick() {
        val currentTime = System.currentTimeMillis()
        clickTimes.add(currentTime)
        clickTimes.removeAll { currentTime - it > timeWindow }
    }

    fun isHighFrequencyClick(): Boolean {
        return clickTimes.size >= maxClicks
    }
}