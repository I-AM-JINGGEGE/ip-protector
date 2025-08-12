package com.vpn.base.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.vpn.android.BuildConfig;

public class YoLog {
    private volatile static boolean sPrintSwitch = BuildConfig.DEBUG;

    public static void init(@NonNull Context context, boolean debug) {
        sPrintSwitch = debug;
    }

    public static void v(String tag, String msg) {
        if (TextUtils.isEmpty(tag) || TextUtils.isEmpty(msg)) {
            return;
        }

        if (sPrintSwitch) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (TextUtils.isEmpty(tag) || TextUtils.isEmpty(msg)) {
            return;
        }

        if (sPrintSwitch) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (TextUtils.isEmpty(tag) || TextUtils.isEmpty(msg)) {
            return;
        }

        if (sPrintSwitch) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (TextUtils.isEmpty(tag) || TextUtils.isEmpty(msg)) {
            return;
        }

        if (sPrintSwitch) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (TextUtils.isEmpty(tag) || TextUtils.isEmpty(msg)) {
            return;
        }

        if (sPrintSwitch) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (TextUtils.isEmpty(tag) || TextUtils.isEmpty(msg)) {
            return;
        }

        if (sPrintSwitch) {
            Log.e(tag, msg, tr);
        }
    }

    public static void logException(Throwable tr) {
        if (sPrintSwitch) {
            tr.printStackTrace();
        }
    }
}
