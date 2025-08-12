package com.vpn.android.server

import android.annotation.SuppressLint
import androidx.lifecycle.MediatorLiveData
import com.vpn.android.MainApplication
import com.vpn.android.coreservice.CoreServiceManager
import com.sdk.ssmod.IMSDK
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.*

object UpTimeHelper {
    internal const val UP_TIME_INITIAL_MINUTES = 60L
    private const val UP_TIME_INITIAL_MILLISECONDS = UP_TIME_INITIAL_MINUTES * 60_000L

    val upTimeRemainingTimeAsLiveData = MediatorLiveData<Long>()
    private var timer: Timer? = null

    init {
        initUpTime()
    }

    private fun initUpTime() {
        upTimeRemainingTimeAsLiveData.addSource(IMSDK.vpnState) {
            if (it == IMSDK.VpnState.Connected) {
                refreshUpTimeRemainingTime()
            } else if (it == IMSDK.VpnState.Stopped) {
                stopUpTime()
            }
        }
    }

    @DelicateCoroutinesApi
    private fun refreshUpTimeRemainingTime() {
        GlobalScope.launch {
            try {
                triggerUpTimeLimit()
                syncUpTimePerSeconds()
            } catch (e: Exception) {
            }
        }
    }

    @SuppressLint("NewApi")
    private suspend fun triggerUpTimeLimit() {
        RuntimeException("isRunning=${IMSDK.uptimeLimit.ongoing.isRunning}").printStackTrace()
        if (!IMSDK.uptimeLimit.ongoing.isRunning) {
            try {
                upTimeRemainingTimeAsLiveData.postValue(UP_TIME_INITIAL_MILLISECONDS / 1000)
                IMSDK.uptimeLimit.startNew(Duration.ofMinutes(UP_TIME_INITIAL_MINUTES))
            } catch (e: Exception) {
                RuntimeException("isRunning=${IMSDK.uptimeLimit.ongoing.isRunning}").printStackTrace()
                e.printStackTrace()
            }
        }
    }

    private fun syncUpTimePerSeconds() {
        timer = Timer()
        val task = object : TimerTask() {
            override fun run() {
                if (IMSDK.uptimeLimit.ongoing.remaining <= 0L) {
                    CoreServiceManager.getInstance(MainApplication.context).disconnect()
                }
                upTimeRemainingTimeAsLiveData.postValue(IMSDK.uptimeLimit.ongoing.remaining / 1000)
            }
        }
        timer!!.schedule(task, 1000, 1000)
    }

    fun stopUpTime() {
        IMSDK.uptimeLimit.ongoing.cancel()
        timer?.cancel()
        timer = null
        upTimeRemainingTimeAsLiveData.postValue(0)
    }
}
