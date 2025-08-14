package com.vpn.android.coreservice

import androidx.lifecycle.MutableLiveData
import com.vpn.android.BuildConfig
import com.vpn.android.MainApplication
import com.vpn.android.base.net.NetworkManager
import com.vpn.android.base.utils.LogUtils
import com.vpn.android.region.RegionConstants
import com.vpn.android.report.VpnReporter
import com.vpn.android.ui.support.LegalManager
import com.vpn.tahiti.TahitiCoreServiceUserUtils
import com.sdk.ssmod.IMSDK
import com.sdk.ssmod.IServers
import com.sdk.ssmod.api.http.beans.FetchResponse
import com.vpn.android.utils.ChannelUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Collections

object CoreSDKResponseManager {
    private val TAG = CoreSDKResponseManager.javaClass.name
    private var fetchResponse: FetchResponse? = null
    val fetchResponseAsLiveData = MutableLiveData<FetchResponse>()
    val fetchResponseRefreshingAsLiveData = MutableLiveData<Boolean>()

    fun initNetworkObserver() {
        NetworkManager.getInstance(MainApplication.instance).connectedAsLiveData.observeForever {
            if (it) {
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        acquireFromNetwork("network valid")
                    } catch (ignore: Exception) {
                    } finally {
                        CoreServiceManager.getInstance(MainApplication.context).syncServerCountryCodeSelected(RegionConstants.REGION_CODE_DEFAULT)
                    }
                }
            }
        }
    }

    // Region acquire from network
    suspend fun acquireFromNetwork(from: String) {
        LogUtils.i(TAG, "obtainFromNetwork")
        var start = System.currentTimeMillis()
        VpnReporter.reportServerRequestStart(from)
        fetchResponseRefreshingAsLiveData.postValue(true)
        val deviceId = if (ChannelUtils.isDebugFlavor()) "0000adf33b485ef8" else TahitiCoreServiceUserUtils.getAndroidID(MainApplication.context)
        try {
            fetchResponse = IMSDK.servers.refresh(deviceId)
                ?.also {
                it.serverZones?.apply {
                    Collections.shuffle(this)
                }
            }
            fetchResponse?.let {
                VpnReporter.reportServersRefreshFinish(from, true, 0, "", cost = (System.currentTimeMillis() - start), areaCount = (it.serverZones?.size ?: 0))
                fetchResponseAsLiveData.postValue(it)
            }
        } catch (e: UnsatisfiedLinkError) {
            throw e
        }
        catch (e: IServers.GeoRestrictedException) {
            VpnReporter.reportServersRefreshFinish(from, false, -1, e.toString(), System.currentTimeMillis() - start, 0)
            LegalManager.getInstance(MainApplication.context).logNotInLegalRegion()
        } catch (ignore: Exception) {
            VpnReporter.reportServersRefreshFinish(from, false, -2, ignore.toString(), System.currentTimeMillis() - start, 0)
        }
        fetchResponseRefreshingAsLiveData.postValue(false)
    }
}
