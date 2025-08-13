package com.vpn.android.ui.settings.appsbypass;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vpn.android.R;
import com.vpn.android.base.utils.ToastUtils;
import com.vpn.android.ui.common.CommonAppCompatActivity;
import com.vpn.android.ui.helper.LanguageSettingHelper;
import com.vpn.tahiti.TahitiCoreServiceStateInfoManager;

public class AppsBypassSettingsActivity extends CommonAppCompatActivity {
    private AppsBypassSettingsViewModel mAppsBypassSettingsViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_bypass_settings);
        
        // 设置状态栏颜色
        setStatusBarColorRes(R.color.white, true);

        RecyclerView appsInfoRV = findViewById(R.id.recycler_view);

        mAppsBypassSettingsViewModel = new ViewModelProvider(this).get(AppsBypassSettingsViewModel.class);

        ImageButton navBtn = findViewById(R.id.btn_back);
        if (LanguageSettingHelper.getInstance(this).isNeedToChangeDirection()) {
            navBtn.setImageResource(R.mipmap.ic_back_1);
        } else {
            navBtn.setImageResource(R.mipmap.arrow_black_left);
        }
        navBtn.setOnClickListener(v -> onBackPressed());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        AppsInfoRecyclerViewAdapter appsInfoRecyclerViewAdapter = new AppsInfoRecyclerViewAdapter(mAppsBypassSettingsViewModel);
        appsInfoRV.setLayoutManager(linearLayoutManager);
        appsInfoRV.setAdapter(appsInfoRecyclerViewAdapter);
        appsInfoRV.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        appsInfoRV.setItemAnimator(new DefaultItemAnimator());

        String loadingTip = this.getResources().getString(R.string.vs_common_tips_loading);
        showLoading(loadingTip, true);
        mAppsBypassSettingsViewModel.getAppsInfo().observe(this, appsInfoResult -> {
            cancelLoading();
            appsInfoRecyclerViewAdapter.setAppsInfo(appsInfoResult);
        });
    }

    @Override
    protected void onDestroy() {
        mAppsBypassSettingsViewModel.syncAppsBypassData();
        if (TahitiCoreServiceStateInfoManager.getInstance(this).getCoreServiceConnected()) {
            ToastUtils.showToast(this, this.getResources().getString(R.string.vs_feature_app_bypass_tip));
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        super.onBackPressed();
    }
}
