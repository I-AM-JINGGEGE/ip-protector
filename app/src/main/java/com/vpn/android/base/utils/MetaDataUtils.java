package com.vpn.android.base.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

public class MetaDataUtils {
    public static String getMetaDataString(@NonNull Context context, @NonNull String key) {
        Context appContext = context.getApplicationContext();
        try {
            return appContext.getPackageManager().getApplicationInfo(appContext.getPackageName(), PackageManager.GET_META_DATA).metaData.getString(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
