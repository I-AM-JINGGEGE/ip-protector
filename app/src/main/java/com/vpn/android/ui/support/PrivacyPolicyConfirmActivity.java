package com.vpn.android.ui.support;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.vpn.android.R;
import com.vpn.android.ui.common.CommonAppCompatActivity;

public class PrivacyPolicyConfirmActivity extends CommonAppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy_confirm);

        findViewById(R.id.confirm_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.confirm_button) {
            doConfirmPrivacyPolicy();
        }
    }

    private void doConfirmPrivacyPolicy() {
        SupportUtils.setPrivacyPolicyConfirmed(this);
        finish();
    }
}
