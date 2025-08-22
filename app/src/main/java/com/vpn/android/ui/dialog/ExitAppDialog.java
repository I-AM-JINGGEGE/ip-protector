package com.vpn.android.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.vpn.android.R;
import com.vpn.android.ads.AdPresenterWrapper;
import com.vpn.android.ads.constant.AdConstant;
import com.vpn.android.utils.DensityUtil;
import com.vpn.tahiti.TahitiCoreServiceStateInfoManager;

public class ExitAppDialog extends Dialog implements LifecycleOwner {
    private Button mGotBTN;
    private FrameLayout mNativeAdContainer;
    private DialogListener mDialogListener;

    public ExitAppDialog(@NonNull Context context) {
        super(context, R.style.TranslucentDialog);
        lifecycleRegistry = new LifecycleRegistry(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lifecycleRegistry.markState(Lifecycle.State.CREATED);
        setContentView(R.layout.layout_dialog_exit);

        //初始化界面控件
        initView();
        initEvent();
    }

    private void initView() {
        mGotBTN = findViewById(R.id.btn_yes);
        mNativeAdContainer = findViewById(R.id.native_ad_container);
        String adPlacement = TahitiCoreServiceStateInfoManager.getInstance(getContext()).getCoreServiceConnected() ? AdConstant.AdPlacement.N_EXIT_CONNECTED : AdConstant.AdPlacement.N_EXIT_DISCONNECTED;
        AdPresenterWrapper.Companion.getInstance().loadNativeAd(null, "exit app dialog");
        AdPresenterWrapper.Companion.getInstance().getNativeAdLoadLiveData().observe(this, result -> {
            View view = AdPresenterWrapper.Companion.getInstance().getNativeAdExitAppView(adPlacement, mNativeAdContainer, null);
            if (result && view != null) {
                mNativeAdContainer.removeAllViews();
                mNativeAdContainer.addView(view);
            }
        });
    }

    private void initEvent() {
        if (mGotBTN != null) {
            mGotBTN.setOnClickListener(v -> {
                if (mDialogListener != null) {
                    mDialogListener.onButtonClick();
                }
                cancel();
            });
        }
        findViewById(R.id.btn_no).setOnClickListener(v -> {
            cancel();
        });
    }

    public void setDialogOnClickListener(DialogListener dialogListener) {
        mDialogListener = dialogListener;
    }

    @Override
    public void show() {
        super.show();
        Window dialogWindow = getWindow();
        assert dialogWindow != null;
        dialogWindow.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams wlp = dialogWindow.getAttributes();
        wlp.width = DensityUtil.getScreenWidth(getContext()) - DensityUtil.dip2px(getContext(), 30);
        dialogWindow.setAttributes(wlp);
    }

    public interface DialogListener {
        void onButtonClick();
    }

    private LifecycleRegistry lifecycleRegistry;

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }

    @Override
    protected void onStart() {
        super.onStart();
        lifecycleRegistry.markState(Lifecycle.State.RESUMED);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        lifecycleRegistry.markState(Lifecycle.State.DESTROYED);
    }
}
