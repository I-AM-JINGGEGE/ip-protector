package com.vpn.android.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.vpn.android.MainApplication
import com.vpn.android.R
import com.vpn.android.base.utils.ThreadUtils
import org.libpag.PAGFile
import org.libpag.PAGView
import java.net.HttpURLConnection
import java.net.URL

class ConnectivityTestDialog(context: Context) : Dialog(context, R.style.TranslucentDialog) {
    private lateinit var connectivityTestingAnim: PAGView
    private lateinit var testingLayout: View
    private lateinit var successLayout: View
    private lateinit var successOk: View
    private lateinit var successRetest: View
    private lateinit var tvSuccessMessage: TextView
    private lateinit var failLayout: View
    private lateinit var failClose: View
    private lateinit var failRetest: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dialogWindow = window!!
        dialogWindow.setGravity(Gravity.CENTER)
        val wlp = dialogWindow.attributes
        wlp.y -= 100
        setContentView(R.layout.layout_dialog_connectivity_test)
        //按空白处不能取消动画
        setCanceledOnTouchOutside(false)
        //初始化界面控件
        initView()
        test()
    }

    private fun initView() {
        connectivityTestingAnim = findViewById(R.id.connectivity_testing_anim)
        testingLayout = findViewById(R.id.testing_layout)
        successLayout = findViewById(R.id.success_layout)
        successOk = findViewById(R.id.btn_success_got)
        successRetest = findViewById(R.id.tv_success_retest)
        tvSuccessMessage = findViewById(R.id.tv_success_message)
        failLayout = findViewById(R.id.fail_layout)
        failClose = findViewById(R.id.tv_fail_close)
        failRetest = findViewById(R.id.btn_fail_retest)

        successOk.setOnClickListener { mDialogListener?.onCloseClick() }
        successRetest.setOnClickListener { mDialogListener?.onRetestClick() }

        failClose.setOnClickListener { mDialogListener?.onCloseClick() }
        failRetest.setOnClickListener { mDialogListener?.onRetestClick() }

        connectivityTestingAnim.apply {
            composition = PAGFile.Load(
                MainApplication.context.assets,
                "connectivity_test.pag"
            )
            setRepeatCount(-1)
            play()
        }
    }

    private var mDialogListener: DialogListener? = null

    fun setDialogOnClickListener(dialogListener: DialogListener?) {
        mDialogListener = dialogListener
    }

    interface DialogListener {
        fun onCloseClick()
        fun onRetestClick()
    }

    private fun test() {
        Thread {
            var conn: HttpURLConnection? = null
            try {
                val url = URL("https", "www.google.com", "/generate_204")
                conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("Connection", "close")
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                conn.instanceFollowRedirects = false
                conn.useCaches = false
                val start = SystemClock.elapsedRealtime()
                val code = conn.responseCode
                val elapsed = SystemClock.elapsedRealtime() - start
                val length = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    conn.contentLengthLong
                } else {
                    conn.contentLength.toLong()
                }
                if (code == 204 || code == 200 && length == 0L) {
                    ThreadUtils.runOnMainThread {
                        if (!this@ConnectivityTestDialog.isShowing) {
                            return@runOnMainThread
                        }
                        successLayout.visibility = View.VISIBLE
                        testingLayout.visibility = View.GONE
                        tvSuccessMessage.text = context.resources.getString(R.string.connectivity_test_duration, "$elapsed")
                    }
                } else {
                    ThreadUtils.runOnMainThread {
                        if (!this@ConnectivityTestDialog.isShowing) {
                            return@runOnMainThread
                        }
                        failLayout.visibility = View.VISIBLE
                        testingLayout.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ThreadUtils.runOnMainThread {
                    if (!this@ConnectivityTestDialog.isShowing) {
                        return@runOnMainThread
                    }
                    failLayout.visibility = View.VISIBLE
                    testingLayout.visibility = View.GONE
                }
            } finally {
                conn?.disconnect()
            }
        }.start()
    }
}