package com.vpn.android.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.vpn.android.R;
import com.vpn.android.ui.support.SupportUtils;
import com.vpn.android.ui.widget.RatingBar;

public class RatingDialog extends Dialog {
    private ImageView mCloseIV;
    private TextView mTitleTV;
    private RatingBar mSmileyRating;
    private Button mConfirmBTN;

    private ConfirmBtnClickListener mConfirmBtnClickListener;

    public RatingDialog(@NonNull Context context) {
        super(context, R.style.CommonDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window dialogWindow = getWindow();
        assert dialogWindow != null;
        dialogWindow.setGravity(Gravity.CENTER);
        setContentView(R.layout.layout_dialog_my);

        setCanceledOnTouchOutside(false);
        setCancelable(false);
        initView();
        initEvent();
    }

    private void initEvent() {
        mCloseIV.setOnClickListener(v -> dismiss());
        mConfirmBTN.setOnClickListener(v -> {
            if (mConfirmBtnClickListener != null) {
                mConfirmBtnClickListener.onConfirmBtnClick();
            }
        });
        findViewById(R.id.not_show).setOnClickListener(v -> {
            SupportUtils.markDoNotShowRateDialog(getContext());
            dismiss();
        });
    }

    public interface ConfirmBtnClickListener {
        void onConfirmBtnClick();
    }

    public void setConfirmBtnClickListener(ConfirmBtnClickListener confirmBtnClickListener) {
        mConfirmBtnClickListener = confirmBtnClickListener;
    }

    private void initView() {
        mCloseIV = findViewById(R.id.ic_tip_close);
        mTitleTV = findViewById(R.id.tv_title);
        mSmileyRating = findViewById(R.id.smile_rating);
        mConfirmBTN = findViewById(R.id.confirm_button);
    }

    public void setTitle(String title) {
        mTitleTV.setText(title);
    }

    public RatingBar.Type getRatingGrade() {
        return mSmileyRating.getSelectedSmiley();
    }
}
