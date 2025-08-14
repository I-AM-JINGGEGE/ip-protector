package com.vpn.android.ui.regionselector2

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vpn.android.R
import com.vpn.android.coreservice.CoreServiceManager.Companion.getInstance
import com.vpn.android.databinding.ActivityServerListBinding
import com.vpn.android.report.RequestVpnPermissionContract
import com.vpn.android.report.VpnReporter
import com.vpn.android.ui.common.CommonAppCompatActivity
import com.vpn.android.ui.common.CommonDialog
import com.vpn.android.ui.support.LegalManager
import com.sdk.ssmod.api.http.beans.FetchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.jvm.internal.Ref

class ServerListActivity : CommonAppCompatActivity() {
    private var mServerListViewModel: ServerListViewModel? = null
    private val mLegalNoticeDialog = AtomicReference<CommonDialog?>()
    private lateinit var binding: ActivityServerListBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        mServerListViewModel = ViewModelProvider(this).get(ServerListViewModel::class.java)
        binding.btnBack.setOnClickListener { onBackPressed() }

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        val serverListRecyclerViewAdapter =
            ServerListRecyclerViewAdapter(this@ServerListActivity) { vpnServerRegion: FetchResponse.ServerZone? ->
                VpnReporter.reportToStartConnect(VpnReporter.PARAM_VALUE_FROM_SERVER_LIST)
                lifecycleScope.launch {
                    if (!requestVpnPermission(this@ServerListActivity)) {
                        return@launch
                    }
                    VpnReporter.reportStartConnect(VpnReporter.PARAM_VALUE_FROM_SERVER_LIST)
                    getInstance(applicationContext).connect(vpnServerRegion)
                    launch(Dispatchers.Main) { finish() }
                }
            }
        binding.recyclerView.layoutManager = linearLayoutManager
        binding.recyclerView.adapter = serverListRecyclerViewAdapter
        binding.recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.recyclerView.itemAnimator = DefaultItemAnimator()
        
        mServerListViewModel!!.vPNServerRegionList.observe(this) { vpnServerRegions: FetchResponse? ->
            serverListRecyclerViewAdapter.setVPNServerRegions(
                vpnServerRegions
            )
        }
        LegalManager.getInstance(applicationContext).inLegalRegionAsLiveData.observe(this) { isLegal: Boolean ->
            legalRegionObserverHandler(
                isLegal
            )
        }
        initServersRefresh()
    }

    private var requestVpnPermissionCallbackRef = Ref.ObjectRef<(Boolean) -> Unit>()
    private val requestVpnPermissionContract = registerForActivityResult(RequestVpnPermissionContract()) {
            requestVpnPermissionCallbackRef.element?.invoke(it)
        }
    private suspend fun requestVpnPermission(activity: CommonAppCompatActivity) = suspendCoroutine<Boolean> { continuation ->
        requestVpnPermissionCallbackRef.element = {
            requestVpnPermissionCallbackRef.element = null
            continuation.resume(it)
        }
        requestVpnPermissionContract.launch(Unit)
    }

    private fun initServersRefresh() {
        mServerListViewModel?.serversRefreshingAsLiveData?.observe(this) { serversRefreshingResult: Boolean? ->
                if (serversRefreshingResult != null && serversRefreshingResult) {
                    showLoading(resources.getString(R.string.vs_feature_region_refresh_tips_loading), false)
                    return@observe
                }
                cancelLoading()
            }
        binding.serversRefreshBtn.setOnClickListener { mServerListViewModel!!.refreshServers() }
    }

    private fun legalRegionObserverHandler(isLegal: Boolean) {
        if (isLegal) return
        val dialog = CommonDialog(this)
        if (!mLegalNoticeDialog.compareAndSet(null, dialog)) return
        dialog.setCancelable(false)
        dialog.setOnlyOKButton(true)
        dialog.setTitle(getString(R.string.vs_legal_notices_dialog_title))
        dialog.setMessage(getString(R.string.vs_legal_notices_dialog_content))
        dialog.setOKButton(getString(R.string.vs_common_dialog_ok_button))
        dialog.setOkOnclickListener {
            mLegalNoticeDialog.set(null)
            System.exit(0)
        }
        dialog.show()
    }

    override fun onBackPressed() {
        setResult(RESULT_OK)
        super.onBackPressed()
    }
}