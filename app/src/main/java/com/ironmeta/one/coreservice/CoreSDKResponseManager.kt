package com.ironmeta.one.coreservice

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import com.adjust.sdk.AndroidIdUtil.getAndroidId
import com.ironmeta.one.BuildConfig
import com.ironmeta.one.MainApplication
import com.ironmeta.one.base.net.NetworkManager
import com.ironmeta.one.base.utils.LogUtils
import com.ironmeta.one.region.RegionConstants
import com.ironmeta.one.ui.support.LegalManager
import com.sdk.ssmod.IMSDK
import com.sdk.ssmod.IRefreshListener
import com.sdk.ssmod.IServers
import com.sdk.ssmod.api.http.beans.FetchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Response
import java.util.Collections

object CoreSDKResponseManager {
    private val TAG = CoreSDKResponseManager.javaClass.name
    private var fetchResponse: FetchResponse? = null
    val fetchResponseAsLiveData = MutableLiveData<FetchResponse>()
    val fetchResponseRefreshingAsLiveData = MutableLiveData<Boolean>()

    fun initNetworkObserver() {
        val observer: Observer<Boolean> = Observer {
            if (it) {
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        acquireFromNetwork()
                    } catch (ignore: Exception) {
                    }
                }
            }
        }
        val vpnStateLiveData: LiveData<Boolean> = Transformations.map(IMSDK.vpnState) {
            it == IMSDK.VpnState.Connected
        }
        NetworkManager.getInstance(MainApplication.instance).connectedAsLiveData.observeForever {
            vpnStateLiveData.removeObserver(observer)
            if (it) {
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        acquireFromNetwork()
                    } catch (ignore: Exception) {
                    } finally {
                        CoreServiceManager.getInstance(MainApplication.context).syncServerCountryCodeSelected(RegionConstants.REGION_CODE_DEFAULT)
                    }
                }
                vpnStateLiveData.observeForever(observer)
            }
        }
    }

    // Region acquire from network
    suspend fun acquireFromNetwork() {
        LogUtils.i(TAG, "obtainFromNetwork")
        fetchResponseRefreshingAsLiveData.postValue(true)
        val deviceId = if (BuildConfig.DEBUG) "ffa198c7f63f7bd2" else getAndroidId(MainApplication.context)
        try {
            fetchResponse = IMSDK.servers.refresh(deviceId, null, null)
                ?.also {
                it.serverZones?.apply {
                    Collections.shuffle(this)
                }
            }
        } catch (e: UnsatisfiedLinkError) {
            throw e
        }
        catch (e: IServers.GeoRestrictedException) {
            LegalManager.getInstance(MainApplication.context).logNotInLegalRegion()
        } catch (ignore: Exception) {

        }
        fetchResponseRefreshingAsLiveData.postValue(false)
        fetchResponse?.let {
            fetchResponseAsLiveData.postValue(it)
        }
    }
}
