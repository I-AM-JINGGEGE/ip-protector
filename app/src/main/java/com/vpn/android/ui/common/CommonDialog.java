package com.vpn.android.ui.common;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.vpn.android.R;

public class CommonDialog extends Dialog {
    private Button mOkBtn;
    private Button mCancelBtn;
    private TextView mTitleTv;
    private TextView mMessageTv;

    private String mTitleStr;
    private String mMessageStr;
    private String mOkStr, mCancelStr;
    private boolean mOnlyOKButton;

    private DialogListener mCancelOnclickListener;
    private DialogListener mOkOnclickListener;


    public CommonDialog(@NonNull Context context) {
        super(context, R.style.CommonDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window dialogWindow = getWindow();
        assert dialogWindow != null;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setBackgroundDrawableResource(R.color.transparent);
        setContentView(R.layout.layout_dialog_base);

        setCanceledOnTouchOutside(false);

        initView();
        initEvent();
        initData();
    }

    private void initEvent() {
        mOkBtn.setOnClickListener(v -> {
            if (mOkOnclickListener != null) {
                mOkOnclickListener.onButtonClick();
            }
        });
        if (!mOnlyOKButton) {
            mCancelBtn.setOnClickListener(v -> {
                if (mCancelOnclickListener != null) {
                    mCancelOnclickListener.onButtonClick();
                }
            });
        }
    }

    private void initData() {
        if (mTitleStr != null) {
            mTitleTv.setText(mTitleStr);
        }
        if (mMessageStr != null) {
            mMessageTv.setText(mMessageStr);
        }
        if (mOkStr != null) {
            mOkBtn.setText(mOkStr);
        }
        if (mOnlyOKButton) {
            mCancelBtn.setVisibility(View.GONE);
        } else {
            mCancelBtn.setVisibility(View.VISIBLE);
            if (mCancelStr != null) {
                mCancelBtn.setText(mCancelStr);
            }
        }
    }

    private void initView() {
        mTitleTv = findViewById(R.id.title);
        mMessageTv = findViewById(R.id.message);
        mOkBtn = findViewById(R.id.btn_ok);
        mCancelBtn = findViewById(R.id.btn_cancel);
    }

    public void setTitle(String title) {
        mTitleStr = title;
    }

    public void setMessage(String message) {
        mMessageStr = message;
    }

    public void setOKButton(String content) {
        mOkStr = content;
    }

    public void setOkOnclickListener(DialogListener dialogListener) {
        mOkOnclickListener = dialogListener;
    }

    public void setCancelButton(String content) {
        mCancelStr = content;
    }

    public void setCancelOnclickListener(DialogListener dialogListener) {
        mCancelOnclickListener = dialogListener;
    }

    public void setOnlyOKButton(boolean onlyOKButton) {
        mOnlyOKButton = onlyOKButton;
    }

    public interface DialogListener {
        void onButtonClick();
    }
}
