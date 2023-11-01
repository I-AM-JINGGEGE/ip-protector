package com.ironmeta.one.base.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.annotation.NonNull;

public class AppStoreUtils {
    public static boolean openPlayStoreOnlyWithUrl(@NonNull Context context, @NonNull String url) {
        if (openPlayStoreWithUrl(context, url)) {
            return true;
        }
        return OSUtils.openBrowserWithUrl(context, url);
    }

    private static boolean openPlayStoreWithUrl(@NonNull Context context, @NonNull String url) {
        if (!isPlayStoreAvailable(context)) {
            return false;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName("com.android.vending", "com.google.android.finsky.activities.MainActivity");
        intent.setData(Uri.parse(url));
        return ActivityUtils.safeStartActivityWithIntent(context, intent);
    }

    private static boolean isPlayStoreAvailable(@NonNull Context context) {
        String googlePlayPkgName = "com.android.vending";
        try {
            PackageManager packageManager = context.getApplicationContext().getPackageManager();
            PackageInfo pInfo = packageManager.getPackageInfo(googlePlayPkgName, 0);
            if (pInfo != null) {
                return true;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
