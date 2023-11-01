package com.ironmeta.one.ui.support;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.Nullable;

import com.ironmeta.one.R;
import com.ironmeta.one.ui.common.CommonAppCompatActivity;
import com.ironmeta.one.ui.common.CommonWebViewFragment;
import com.ironmeta.one.ui.helper.LanguageSettingHelper;

public class TermsOfServiceActivity extends CommonAppCompatActivity {
    private static final String URL = "https://one.ironmeta.com/terms-of-service";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiy_terms_of_service);

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
}
