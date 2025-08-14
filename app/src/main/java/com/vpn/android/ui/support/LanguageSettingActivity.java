package com.vpn.android.ui.support;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.Bundle;

import com.vpn.android.R;
import com.vpn.android.ui.adapter.LanguageSettingRecyclerViewAdapter;
import com.vpn.android.ui.common.CommonAppCompatActivity;

public class LanguageSettingActivity extends CommonAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_setting);
        
        // 设置状态栏颜色
        setStatusBarColorRes(R.color.white, true);

        initView();
    }

    /**
     * init view begin
     */
    private RecyclerView mRecycleViewLanguageSetting;

    private void initView() {
        // toolbar
        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());

        mRecycleViewLanguageSetting = findViewById(R.id.recycler_view_language_setting);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecycleViewLanguageSetting.setLayoutManager(linearLayoutManager);
        LanguageSettingRecyclerViewAdapter adapter = new LanguageSettingRecyclerViewAdapter(this);
        mRecycleViewLanguageSetting.setAdapter(adapter);
    }
    /** init view end **/

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        super.onBackPressed();
    }
}