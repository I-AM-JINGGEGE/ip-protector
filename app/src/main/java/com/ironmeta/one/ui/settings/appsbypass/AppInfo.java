package com.ironmeta.one.ui.settings.appsbypass;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private String mAppName;
    private String mPackageName;
    private String mVersionName;
    private int mVersionCode;
    private Drawable mAppIcon;
    private int iconResId;
    private boolean mBypass = false;

    public String getAppName() {
        return mAppName;
    }

    public void setAppName(String appName) {
        mAppName = appName;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    public String getVersionName() {
        return mVersionName;
    }

    public void setVersionName(String versionName) {
        mVersionName = versionName;
    }

    public int getVersionCode() {
        return mVersionCode;
    }

    public void setVersionCode(int versionCode) {
        mVersionCode = versionCode;
    }

    public Drawable getAppIcon() {
        return mAppIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        mAppIcon = appIcon;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public void setBypass(boolean bypass) {
        mBypass = bypass;
    }

    public boolean getBypass() {
        return mBypass;
    }
}
