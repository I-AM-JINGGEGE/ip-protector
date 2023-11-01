package com.ironmeta.one.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.ironmeta.one.R

class OpenNetworkSettingsDialog(context: Context) : Dialog(context, R.style.TranslucentDialog) {
    private lateinit var mCloseButton: TextView
    private lateinit var mOpenSettingsButton: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dialogWindow = window!!
        dialogWindow.setGravity(Gravity.CENTER)
        val wlp = dialogWindow.attributes
        wlp.y -= 100
        setContentView(R.layout.layout_dialog_open_network)
        //按空白处不能取消动画
        setCanceledOnTouchOutside(false)
        //初始化界面控件
        initView()
    }

    private fun initView() {
        mCloseButton = findViewById(R.id.tv_close)
        mCloseButton.paint.flags = Paint.UNDERLINE_TEXT_FLAG;
        mOpenSettingsButton = findViewById(R.id.btn_open)

        mCloseButton.setOnClickListener {
            mDialogListener?.onCloseClick()
        }
        mOpenSettingsButton.setOnClickListener {
            mDialogListener?.onOpenClick()
        }
    }

    private var mDialogListener: DialogListener? = null

    fun setDialogOnClickListener(dialogListener: DialogListener?) {
        mDialogListener = dialogListener
    }

    interface DialogListener {
        fun onCloseClick()
        fun onOpenClick()
    }
}