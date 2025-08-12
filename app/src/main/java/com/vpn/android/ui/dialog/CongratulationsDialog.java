package com.vpn.android.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.vpn.android.R;

public class CongratulationsDialog extends Dialog {
    private TextView mTitleTV;
    private TextView mMessageTV;
    private Button mGotBTN;

    private String mTitle, mMessage, mNameOfBtn;

    private DialogListener mDialogListener;

    public CongratulationsDialog(@NonNull Context context) {
        super(context, R.style.TranslucentDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window dialogWindow = getWindow();
        assert dialogWindow != null;
        dialogWindow.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams wlp = dialogWindow.getAttributes();
        wlp.y -= 100;
        setContentView(R.layout.layout_dialog_congra);

        //按空白处不能取消动画
        setCanceledOnTouchOutside(false);

        //初始化界面控件
        initView();

        initEvent();

        initData();
    }

    private void initView() {
        mTitleTV = findViewById(R.id.tv_title);
        mMessageTV = findViewById(R.id.tv_message);
        mGotBTN = findViewById(R.id.btn_got);
    }

    private void initEvent() {
        if (mGotBTN != null) {
            mGotBTN.setOnClickListener(v -> {
                if (mDialogListener != null) {
                    mDialogListener.onButtonClick();
                }
            });
        }
    }

    private void initData() {
        if (mTitle != null) {
            mTitleTV.setText(mTitle);
        }
        if (mMessage != null) {
            mMessageTV.setText(mMessage);
        }
        if (mNameOfBtn != null) {
            mGotBTN.setText(mNameOfBtn);
        }
    }

    public void setDialogOnClickListener(DialogListener dialogListener) {
        mDialogListener = dialogListener;
    }

    public interface DialogListener {
        void onButtonClick();
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public void setMessage(String message) {
        this.mMessage = message;
    }

    public void setNameOfBtn(String nameOfBtn) {
        this.mNameOfBtn = nameOfBtn;
    }
}
