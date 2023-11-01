package com.ironmeta.one.ui.settings.appsbypass;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ironmeta.one.BuildConfig;
import com.ironmeta.tahiti.TahitiCoreServiceAppsBypassUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AppsBypassSettingsViewModel extends AndroidViewModel {
    private MutableLiveData<List<AppInfo>> mAppsInfoAsLiveData;

    public AppsBypassSettingsViewModel(@NonNull Application application) {
        super(application);
        mAppsInfoAsLiveData = new MutableLiveData<>();

        new Thread(() -> {
            PackageManager pm = getApplication().getPackageManager();
            List<AppInfo> appInfoList = new ArrayList<>();
            List<PackageInfo> packages = pm.getInstalledPackages(0);
            for (int i = 0; i < packages.size(); i++) {
                PackageInfo packageInfo = packages.get(i);
                if (BuildConfig.APPLICATION_ID.equals(packageInfo.applicationInfo.packageName)) {
                    continue;
                }
                AppInfo appInfo = new AppInfo();
                appInfo.setAppName(packageInfo.applicationInfo.loadLabel(pm).toString());
                appInfo.setPackageName(packageInfo.packageName);
                appInfo.setVersionName(packageInfo.versionName);
                appInfo.setVersionCode(packageInfo.versionCode);
                int resId = packageInfo.applicationInfo.icon;
                if (resId == 0) {
                    appInfo.setAppIcon(packageInfo.applicationInfo.loadIcon(pm));
                }
                appInfo.setIconResId(resId);
                appInfoList.add(appInfo);
            }

            Set<String> appsBypassPackageName = TahitiCoreServiceAppsBypassUtils.getAppsBypassPackageName(getApplication());
            Iterator<String> appsBypassIterator = appsBypassPackageName.iterator();
            while (appsBypassIterator.hasNext()) {
                String appBypassPackageName = appsBypassIterator.next();
                for (int i = 0; i < appInfoList.size(); i++) {
                    if (TextUtils.equals(appBypassPackageName, appInfoList.get(i).getPackageName())) {
                        appInfoList.get(i).setBypass(true);
                    }
                }
            }

            mAppsInfoAsLiveData.postValue(appInfoList);
        }).start();
    }

    public LiveData<List<AppInfo>> getAppsInfo() {
        return mAppsInfoAsLiveData;
    }

    public void setAppBypass(@NonNull String pkgName, boolean bypass) {
        List<AppInfo> appInfoList = mAppsInfoAsLiveData.getValue();
        if (appInfoList == null) {
            return;
        }
        for (int i = 0; i < appInfoList.size(); i++) {
            AppInfo appInfo = appInfoList.get(i);
            if (TextUtils.equals(appInfo.getPackageName(), pkgName)) {
                appInfo.setBypass(bypass);
            }
        }
    }

    public void syncAppsBypassData() {
        List<AppInfo> appInfoList = mAppsInfoAsLiveData.getValue();
        if (appInfoList == null) {
            return;
        }
        Set<String> appsBypassPackageName = new HashSet<>();
        for (int i = 0; i < appInfoList.size(); i++) {
            AppInfo appInfo = appInfoList.get(i);
            if (appInfo.getBypass()) {
                appsBypassPackageName.add(appInfo.getPackageName());
            }
        }
        TahitiCoreServiceAppsBypassUtils.setAppsBypassPackageName(getApplication(), appsBypassPackageName);
    }
}
