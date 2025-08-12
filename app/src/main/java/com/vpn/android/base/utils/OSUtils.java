package com.vpn.android.base.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vpn.android.R;

public class OSUtils {
    public static boolean openBrowserWithUrl(@NonNull Context context, @NonNull String url) {
        Context appContext = context.getApplicationContext();

        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri uri = Uri.parse(url);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return ActivityUtils.safeStartActivityWithIntent(appContext, intent);
    }

    public static boolean sendEmail(@NonNull Context context, @NonNull String toAddress, @Nullable String subject) {
        Context appContext = context.getApplicationContext();

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + toAddress));
        if (!TextUtils.isEmpty(subject)) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (!ActivityUtils.safeStartActivityWithIntent(appContext, intent)) {
            ToastUtils.showToast(appContext, appContext.getString(R.string.vs_common_tips_no_email_app));
            return false;
        }
        return true;
    }
}
