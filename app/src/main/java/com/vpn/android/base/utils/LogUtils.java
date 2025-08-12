package com.vpn.android.base.utils;

import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.vpn.android.BuildConfig;

public class LogUtils {
    private volatile static boolean sPrintSwitch = BuildConfig.DEBUG;

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
        // record exception with firebase
        try {
            FirebaseCrashlytics.getInstance().recordException(tr);
        } catch (Throwable throwable) {
            if (BuildConfig.DEBUG) {
                throw new RuntimeException(throwable);
            }
        }
        // throw exception in debug model
        if (BuildConfig.DEBUG) {
            if (tr instanceof RuntimeException) {
                throw (RuntimeException) tr;
            } else {
                throw new RuntimeException(tr);
            }
        }
    }
}
