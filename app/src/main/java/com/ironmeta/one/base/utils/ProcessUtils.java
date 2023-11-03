package com.ironmeta.one.base.utils;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ProcessUtils {
    public static boolean isInMainProcess(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        String processName = getProcessName(appContext);
        return processName == null || TextUtils.equals(processName, appContext.getPackageName());
    }

    public static boolean isInServiceProcess(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        String processName = getProcessName(appContext);
        return processName != null && processName.endsWith(":bg");
    }

    @Nullable
    private static String sProcessName;

    @Nullable
    public static String getProcessName(Context context) {
        if (sProcessName != null) {
            return sProcessName;
        } else if (Build.VERSION.SDK_INT >= 28) {
            return getProcessNameAPI28();
        } else {
            Context applicationContext = context.getApplicationContext();
            if (applicationContext instanceof Application) {
                sProcessName = getProcessNameViaReflection((Application) applicationContext);
                return sProcessName;
            } else {
                return null;
            }
        }
    }

    @Nullable
    private static String getProcessNameAPI28() {
        try {
            Method getProcessName = Application.class.getMethod("getProcessName", (Class[]) null);
            return (String) getProcessName.invoke(null, (Object[]) null);
        } catch (Exception var1) {
            return null;
        }
    }

    @Nullable
    private static String getProcessNameViaReflection(Application app) {
        try {
            Field loadedApkField = app.getClass().getField("mLoadedApk");
            loadedApkField.setAccessible(true);
            Object loadedApk = loadedApkField.get(app);
            Field activityThreadField = loadedApk.getClass().getDeclaredField("mActivityThread");
            activityThreadField.setAccessible(true);
            Object activityThread = activityThreadField.get(loadedApk);
            Method getProcessName = activityThread.getClass().getDeclaredMethod("getProcessName", (Class[]) null);
            return (String) getProcessName.invoke(activityThread, (Object[]) null);
        } catch (Exception var6) {
            return null;
        }
    }
}
