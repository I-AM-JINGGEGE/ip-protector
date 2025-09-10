package com.vpn.android.coreservice

import android.content.Context
import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData
import com.vpn.base.vstore.VstoreManager
import com.vpn.android.MainApplication
import com.vpn.android.base.utils.LogUtils
import com.vpn.android.region.RegionConstants
import com.vpn.android.region.RegionConstants.KEY_CONNECTED_VPN_IP
import com.vpn.android.server.UpTimeHelper
import com.vpn.tahiti.TahitiCoreServiceAppsBypassUtils
import com.vpn.tahiti.TahitiCoreServiceStateInfoManager
import com.sdk.ssmod.IMSDK
import com.sdk.ssmod.IMSDKRuntimeException
import com.sdk.ssmod.api.http.beans.FetchResponse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CoreServiceManager private constructor(context: Context) {
    private val TAG = "CoreServiceManager"
    /** region end  */
    companion object {
        private var sCoreServiceManager: CoreServiceManager = CoreServiceManager(MainApplication.context)
        @JvmStatic
        @MainThread
        @Synchronized
        fun getInstance(context: Context): CoreServiceManager {
            return sCoreServiceManager
        }
    }
    private val isConnectingAsLiveData = MutableLiveData<Boolean>()
    val selectedRegionIdLiveData = MutableLiveData<String>()

    init {
        val selectedZoneCode = VstoreManager.getInstance(MainApplication.context).decode(true, RegionConstants.Key_Selected_Region_Country_Code, RegionConstants.REGION_CODE_DEFAULT)
        selectedRegionIdLiveData.postValue(selectedZoneCode)
    }

    fun connect(serverZone: FetchResponse.ServerZone?) {
        GlobalScope.launch {
            if (TahitiCoreServiceStateInfoManager.getInstance(MainApplication.context).coreServiceConnected) {
                disconnectFromServer()
            }
            syncServerCountryCodeSelected(serverZone?.country ?: RegionConstants.REGION_CODE_DEFAULT)
            try {
                if (serverZone == null || serverZone.id == RegionConstants.REGION_UUID_DEFAULT) {
                    connectToBest()
                } else {
                    connectToServer(serverZone)
                }
            } catch (e: IMSDKRuntimeException) {
                LogUtils.i(TAG, "${e.errorCode}, ${e.message}")
                isConnectingAsLiveData.postValue(false)
            } catch (e: Exception) {
                LogUtils.i(TAG, "${e.message}")
                isConnectingAsLiveData.postValue(false)
            }
        }
    }

    fun disconnect() {
        GlobalScope.launch {
            disconnectFromServer()
        }
    }

    private suspend fun disconnectFromServer() {
        val canStop = IMSDK.vpnState.value?.canStop
        if (canStop != null && canStop) {
            UpTimeHelper.stopUpTime()
            IMSDK.disconnect()
        }
    }

    private suspend fun connectToServer(serverZone: FetchResponse.ServerZone?) {
        isConnectingAsLiveData.postValue(true)
        val response = IMSDK.withResponse(CoreSDKResponseManager.fetchResponseAsLiveData.value!!)
            .toServerZone(serverZone?.id ?: RegionConstants.REGION_UUID_DEFAULT)
            .bypassPackageNames(TahitiCoreServiceAppsBypassUtils.getAppsBypassPackageName(MainApplication.context).toList())
            .bypassDomains(TahitiCoreServiceAppsBypassUtils.getDomainsBypass())
            .connect()
        LogUtils.e("VpnReporter", "connectToServer ${response.host?.host}")
        VstoreManager.getInstance(MainApplication.instance).encode(false, KEY_CONNECTED_VPN_IP, response.host?.host ?: "")
        isConnectingAsLiveData.postValue(false)
    }

    private suspend fun connectToBest(): String? {
        isConnectingAsLiveData.postValue(true)
        val response = IMSDK.withResponse(CoreSDKResponseManager.fetchResponseAsLiveData.value!!)
            .bypassPackageNames(TahitiCoreServiceAppsBypassUtils.getAppsBypassPackageName(MainApplication.context).toList())
            .bypassDomains(TahitiCoreServiceAppsBypassUtils.getDomainsBypass())
            .toBest()
            .connect()
        LogUtils.e("VpnReporter", "connectToBest ${response.host?.host}")
        VstoreManager.getInstance(MainApplication.instance).encode(false, KEY_CONNECTED_VPN_IP, response.host?.host ?: "")
        isConnectingAsLiveData.postValue(false)
        return response.zoneId
    }

    fun syncServerCountryCodeSelected(serverZoneIdSelected: String?) {
        val id = serverZoneIdSelected ?: RegionConstants.REGION_CODE_DEFAULT
        VstoreManager.getInstance(MainApplication.context).encode(true, RegionConstants.Key_Selected_Region_Country_Code, id)
        selectedRegionIdLiveData.postValue(id)
    }
}