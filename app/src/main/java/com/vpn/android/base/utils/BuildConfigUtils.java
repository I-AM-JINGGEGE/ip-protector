package com.vpn.android.base.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.vpn.android.MainApplication;

public class BuildConfigUtils {
    static public String getPackageName(@NonNull Context context) {
        return context.getApplicationInfo().packageName;
    }

    static public String getVersionName(@NonNull Context context) {
        try {
            return context.getPackageManager().getPackageInfo(getPackageName(context), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            throw new  RuntimeException();
        }
    }
    static public String getVersionCode(@NonNull Context context) {
        try {
            return String.valueOf(context.getPackageManager().getPackageInfo(getPackageName(context), 0).versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            throw new  RuntimeException();
        }
    }

    static public String getCnl(@NonNull Context context) {
        return ((MainApplication) context.getApplicationContext()).getCnl();
    }

    static public boolean getDebug(@NonNull Context context) {
        return ((MainApplication) context.getApplicationContext()).getDebug();
    }
}
