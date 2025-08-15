package com.vpn.android.ui.support;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.vpn.android.R;
import com.vpn.android.ui.common.CommonAppCompatActivity;
import com.vpn.android.ui.common.CommonWebViewFragment;
import com.vpn.android.ui.helper.LanguageSettingHelper;

public class PrivacyPolicyActivity extends CommonAppCompatActivity {
    private static final String URL = "https://www.ip-protector.net/privacy-policy.html";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        ImageView navBtn = findViewById(R.id.btn_back);
        navBtn.setOnClickListener(v -> onBackPressed());

        showLoading(getResources().getString(R.string.vs_common_tips_loading), true);
        CommonWebViewFragment commonWebViewFragment = new CommonWebViewFragment(URL, (view, url) -> cancelLoading());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_web_view, commonWebViewFragment)
                .commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        super.onBackPressed();
    }
}
