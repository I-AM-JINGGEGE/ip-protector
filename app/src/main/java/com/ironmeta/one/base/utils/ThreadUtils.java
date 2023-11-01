package com.ironmeta.one.base.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

public class ThreadUtils {
    public static void runOnMainThread(@NonNull Runnable action) {
        if (isMainThread()) {
            action.run();
        } else {
            getMainHandler().post(action);
        }
    }

    public static void delayRunOnMainThread(@NonNull Runnable action, long delayMillis) {
        getMainHandler().postDelayed(action, delayMillis);
    }

    public static void removeCallback(@NonNull Runnable runnable) {
        getMainHandler().removeCallbacks(runnable);
    }

    public static boolean isMainThread() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return true;
        }
        return false;
    }

    private static Handler sMainHandler = new Handler(Looper.getMainLooper());

    private static synchronized Handler getMainHandler() {
        return sMainHandler;
    }
}
