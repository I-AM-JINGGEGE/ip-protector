package com.ironmeta.one.base.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class ToastUtils {
    public static void showToast(Context context, String content) {
        if (context == null || TextUtils.isEmpty(content)) {
            return;
        }

        showToast(context.getApplicationContext(), content, Toast.LENGTH_SHORT);
    }

    private static void showToast(@NonNull final Context appContext, @NonNull final String content, final int duration) {
        ThreadUtils.runOnMainThread(() -> {
            Toast.makeText(appContext, content, duration).show();
        });
    }
}
