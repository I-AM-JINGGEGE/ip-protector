package com.ironmeta.tahiti

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.ironmeta.base.utils.YoLog
import com.sdk.ssmod.IMSDK
import com.sdk.ssmod.IMSDK.VpnState
import com.sdk.ssmod.IMSDK.connectedServer
import com.sdk.ssmod.IMSDK.trafficStats
import com.sdk.ssmod.IMSDK.vpnState
import com.sdk.ssmod.beans.TrafficStats
import java.time.Duration

class TahitiCoreServiceStateInfoManager private constructor() {
    val coreServiceConnectedServerAsLiveData: LiveData<IMSDK.WithResponseBuilder.ConnectedTo?> = connectedServer
    var coreServiceConnected = false
    private val mCoreServiceConnectedAsLiveData = MutableLiveData<Boolean>()
    val coreServiceState = VpnState.Idle

    fun getCoreServiceStateAsLiveData(): LiveData<VpnState> {
        return vpnState
    }

    fun getTrafficStatsAsLiveData(): LiveData<TrafficStats> {
        return trafficStats
    }

    /** used up begin  */
    private var usedUpRemainMillisecondsAsLiveData: MediatorLiveData<Long>? = null

    init {
        vpnState.observeForever { vpnState: VpnState ->
            YoLog.i(TAG, "stateChanged, state: $vpnState")
            coreServiceConnected = (vpnState == VpnState.Connected)
            mCoreServiceConnectedAsLiveData.postValue(coreServiceConnected)
        }
        initUsedUp()
    }

    private fun initUsedUp() {
        usedUpRemainMillisecondsAsLiveData = MediatorLiveData()
        usedUpRemainMillisecondsAsLiveData!!.addSource(mCoreServiceConnectedAsLiveData) { connectedResult: Boolean? ->
            if (connectedResult != null && connectedResult) {
                return@addSource
            }
            usedUpRemainMillisecondsAsLiveData!!.setValue(0L)
        }
    }

    fun getUsedUpRemainMillisecondsAsLiveData(): Long {
        return IMSDK.uptimeLimit.ongoing.remaining
    }

    @SuppressLint("NewApi")
    fun addUsedUpMinutes(minutes: Int) {
        IMSDK.uptimeLimit.ongoing.extend(Duration.ofMinutes(minutes.toLong()))
    }

    /** used up end  */
    companion object {
        private val TAG = TahitiCoreServiceStateInfoManager::class.java.simpleName
        private var sTahitiCoreServiceStateInfoManager: TahitiCoreServiceStateInfoManager = TahitiCoreServiceStateInfoManager()
        @JvmStatic
        @Synchronized
        fun getInstance(context: Context?): TahitiCoreServiceStateInfoManager {
            return sTahitiCoreServiceStateInfoManager
        }
    }
}