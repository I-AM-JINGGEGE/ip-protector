package com.ironmeta.tahiti;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.ironmeta.one.BuildConfig;

public class TahitiCoreServiceUserUtils {
    private static final String DEFAULT_UID = "0000000000000000";

    @NonNull
    public static String getUid(@NonNull Context context) {
        String androidID = getAndroidID(context);
        if (!TextUtils.isEmpty(androidID)) {
            return androidID;
        }
        return DEFAULT_UID;
    }

    public static String getAndroidID(@NonNull Context context) {
        if (BuildConfig.DEBUG) {
            return "ffa198c7f63f7bd2";
        }
        try {
            ContentResolver cr = context.getContentResolver();
            return Settings.Secure.getString(cr, Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
