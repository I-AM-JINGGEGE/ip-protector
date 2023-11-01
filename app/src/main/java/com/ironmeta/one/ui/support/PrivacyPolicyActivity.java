package com.ironmeta.one.ui.support;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.Nullable;

import com.ironmeta.one.R;
import com.ironmeta.one.ui.common.CommonAppCompatActivity;
import com.ironmeta.one.ui.common.CommonWebViewFragment;
import com.ironmeta.one.ui.helper.LanguageSettingHelper;

public class PrivacyPolicyActivity extends CommonAppCompatActivity {
    private static final String URL = "https://one.ironmeta.com/privacy-policy";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        ImageButton navBtn=findViewById(R.id.btn_back);
        if (LanguageSettingHelper.getInstance(this).isNeedToChangeDirection()) {
            navBtn.setImageResource(R.mipmap.ic_back_1);
        } else {
            navBtn.setImageResource(R.mipmap.ic_back_0);
        }
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
