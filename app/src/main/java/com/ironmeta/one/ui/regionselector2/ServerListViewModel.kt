package com.ironmeta.one.ui.regionselector2

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ironmeta.one.R
import com.ironmeta.one.base.utils.ToastUtils
import com.ironmeta.one.coreservice.CoreSDKResponseManager.acquireFromNetwork
import com.ironmeta.one.coreservice.CoreSDKResponseManager.fetchResponseAsLiveData
import com.ironmeta.one.region.RegionConstants
import com.ironmeta.one.report.ReportConstants
import com.ironmeta.one.server.ServerResultCodeConstants
import com.sdk.ssmod.api.http.beans.FetchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.coroutines.Continuation

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
        mServersRefreshingAsLiveData!!.setValue(true)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                acquireFromNetwork()
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