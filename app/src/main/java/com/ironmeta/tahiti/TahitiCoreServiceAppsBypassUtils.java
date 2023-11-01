package com.ironmeta.tahiti;

import android.content.Context;

import androidx.annotation.NonNull;

import com.ironmeta.base.vstore.VstoreManager;
import com.ironmeta.one.constants.KvStoreConstants;

import java.util.HashSet;
import java.util.Set;

public class TahitiCoreServiceAppsBypassUtils {
    @NonNull
    public static Set<String> getAppsBypassPackageName(@NonNull Context context) {
        return VstoreManager.getInstance(context).decode(false, KvStoreConstants.KEY_CORE_SERVICE_APPS_BYPASS_PACKAGE_NAME, new HashSet<>());
    }

    public static boolean setAppsBypassPackageName(@NonNull Context context, @NonNull Set<String> bypassAppsPackageName) {
        return VstoreManager.getInstance(context).encode(false, KvStoreConstants.KEY_CORE_SERVICE_APPS_BYPASS_PACKAGE_NAME, bypassAppsPackageName);
    }
}
