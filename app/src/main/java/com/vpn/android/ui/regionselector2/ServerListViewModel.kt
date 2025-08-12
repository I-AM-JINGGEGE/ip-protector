package com.vpn.android.ui.regionselector2

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vpn.android.R
import com.vpn.android.base.utils.ToastUtils
import com.vpn.android.coreservice.CoreSDKResponseManager
import com.vpn.android.coreservice.CoreSDKResponseManager.fetchResponseAsLiveData
import com.sdk.ssmod.api.http.beans.FetchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ServerListViewModel(application: Application) : AndroidViewModel(application) {
    val vPNServerRegionList: LiveData<FetchResponse>

    @SuppressLint("StaticFieldLeak")
    private val appContext: Context

    /** servers refresh begin  */
    private var mServersRefreshingAsLiveData: MutableLiveData<Boolean?>? = null

    init {
        appContext = application.applicationContext
        vPNServerRegionList = fetchResponseAsLiveData
        initServersRefresh()
    }

    private fun initServersRefresh() {
        mServersRefreshingAsLiveData = MutableLiveData(false)
        fetchResponseAsLiveData.observeForever {
            val successTip =
                appContext.resources.getString(R.string.vs_feature_region_refresh_tips_success)
            ToastUtils.showToast(getApplication(), successTip)
        }
    }

    fun refreshServers() {
        if (mServersRefreshingAsLiveData!!.value == null) {
            return
        }
        mServersRefreshingAsLiveData!!.value = true
        GlobalScope.launch(Dispatchers.IO) {
            try {
                CoreSDKResponseManager.acquireFromNetwork("click refresh")
            } catch (e: Exception) {
                val failTip =
                    appContext.resources.getString(R.string.vs_common_tips_network_unavailable)
                ToastUtils.showToast(getApplication(), failTip)
            }
            mServersRefreshingAsLiveData!!.postValue(false)
        }
    }

    val serversRefreshingAsLiveData: LiveData<Boolean?>?
        get() = mServersRefreshingAsLiveData

    fun refreshServersLanguage(context: Context) {

    }
    /** servers refresh end  */
}