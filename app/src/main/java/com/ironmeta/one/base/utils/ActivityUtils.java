package com.ironmeta.one.base.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.ironmeta.one.R;

public class ActivityUtils {
    public static boolean safeStartActivityWithIntent(@NonNull Context context, @NonNull Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            if (context instanceof Activity) {
                ((Activity) context).overridePendingTransition(R.anim.page_in, R.anim.page_out);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean safeStartActivityForResultWithIntent(@NonNull Activity activity, @NonNull Intent intent, int requestCode) {
        try {
            activity.startActivityForResult(intent, requestCode);
            activity.overridePendingTransition(R.anim.page_in, R.anim.page_out);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
