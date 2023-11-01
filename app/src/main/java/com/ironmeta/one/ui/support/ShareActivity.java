package com.ironmeta.one.ui.support;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ironmeta.one.R;
import com.ironmeta.one.ui.adapter.ShareAppsRecyclerViewAdapter;
import com.ironmeta.one.ui.common.CommonAppCompatActivity;
import com.ironmeta.one.ui.helper.LanguageSettingHelper;

public class ShareActivity extends CommonAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        initView();
    }


    /**
     * init view begin
     */
    private ImageView mIvQRCode;
    private TextView mTvShareQRCode;
    private RecyclerView mRecycleViewShareApps;

    private void initView() {
        ImageButton navBtn = findViewById(R.id.btn_back);
        if (LanguageSettingHelper.getInstance(this).isNeedToChangeDirection()) {
            navBtn.setImageResource(R.mipmap.ic_back_1);
        } else {
            navBtn.setImageResource(R.mipmap.ic_back_0);
        }
        navBtn.setOnClickListener(v -> onBackPressed());
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(R.string.vs_feature_share_title);

        mIvQRCode = findViewById(R.id.ic_share_qr_code);
        mIvQRCode.setImageResource(R.mipmap.ic_share_qr_code);

        mTvShareQRCode = findViewById(R.id.text_share_qr_code);
        mTvShareQRCode.setText(R.string.vs_feature_share_statement);

        mRecycleViewShareApps = findViewById(R.id.recycler_view_share_apps);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecycleViewShareApps.setLayoutManager(linearLayoutManager);
        ShareAppsRecyclerViewAdapter adapter = new ShareAppsRecyclerViewAdapter(this);
        mRecycleViewShareApps.setAdapter(adapter);
    }
    /** init view end **/

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        super.onBackPressed();
    }
}