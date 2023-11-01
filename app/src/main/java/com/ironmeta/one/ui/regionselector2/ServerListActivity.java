package com.ironmeta.one.ui.regionselector2;

import android.app.Activity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ironmeta.one.R;
import com.ironmeta.one.coreservice.CoreServiceManager;
import com.ironmeta.one.report.AppReport;
import com.ironmeta.one.report.ReportConstants;
import com.ironmeta.one.ui.common.CommonAppCompatActivity;
import com.ironmeta.one.ui.common.CommonDialog;
import com.ironmeta.one.ui.support.LegalManager;

import java.util.concurrent.atomic.AtomicReference;

public class ServerListActivity extends CommonAppCompatActivity {
    private ServerListViewModel mServerListViewModel;
    private final AtomicReference<CommonDialog> mLegalNoticeDialog = new AtomicReference<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_list);

        RecyclerView serverListRV = findViewById(R.id.recycler_view);

        mServerListViewModel = new ViewModelProvider(this).get(ServerListViewModel.class);

        ((Toolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> onBackPressed());

        getWindow().setBackgroundDrawableResource(R.color.white);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        ServerListRecyclerViewAdapter serverListRecyclerViewAdapter = new ServerListRecyclerViewAdapter(ServerListActivity.this, vpnServerRegion -> {
            AppReport.setConnectionSource(ReportConstants.AppReport.SOURCE_CONNECTION_PAGE_SERVER);
            AppReport.reportToConnection();
            CoreServiceManager.getInstance(getApplicationContext()).connect(vpnServerRegion);
            finish();
        });

        serverListRV.setLayoutManager(linearLayoutManager);
        serverListRV.setAdapter(serverListRecyclerViewAdapter);
        serverListRV.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        serverListRV.setItemAnimator(new DefaultItemAnimator());
        mServerListViewModel.getVPNServerRegionList().observe(this, serverListRecyclerViewAdapter::setVPNServerRegions);

        LegalManager.getInstance(getApplicationContext()).getInLegalRegionAsLiveData().observe(this, this::legalRegionObserverHandler);

        initServersRefresh();
    }

    private void initServersRefresh() {
        mServerListViewModel.getServersRefreshingAsLiveData().observe(this, serversRefreshingResult -> {
            if (serversRefreshingResult != null && serversRefreshingResult) {
                showLoading(getResources().getString(R.string.vs_feature_region_refresh_tips_loading), false);
                return;
            }
            cancelLoading();
        });
        findViewById(R.id.servers_refresh_btn).setOnClickListener(v -> mServerListViewModel.refreshServers());
    }

    private void legalRegionObserverHandler(boolean isLegal) {
        if (isLegal) return;

        final CommonDialog dialog = new CommonDialog(this);
        if (!mLegalNoticeDialog.compareAndSet(null, dialog)) return;
        dialog.setCancelable(false);
        dialog.setOnlyOKButton(true);
        dialog.setTitle(getString(R.string.vs_legal_notices_dialog_title));
        dialog.setMessage(getString(R.string.vs_legal_notices_dialog_content));
        dialog.setOKButton(getString(R.string.vs_common_dialog_ok_button));
        dialog.setOkOnclickListener(() -> {
            mLegalNoticeDialog.set(null);
            System.exit(0);
        });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        super.onBackPressed();
    }
}
