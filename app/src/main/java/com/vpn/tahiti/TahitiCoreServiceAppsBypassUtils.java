package com.vpn.tahiti;

import android.content.Context;
import android.util.ArraySet;

import androidx.annotation.NonNull;

import com.vpn.base.vstore.VstoreManager;
import com.vpn.android.constants.KvStoreConstants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TahitiCoreServiceAppsBypassUtils {
    @NonNull
    public static Set<String> getAppsBypassPackageName(@NonNull Context context) {
        return VstoreManager.getInstance(context).decode(false, KvStoreConstants.KEY_CORE_SERVICE_APPS_BYPASS_PACKAGE_NAME, new HashSet<>());
    }

    public static boolean setAppsBypassPackageName(@NonNull Context context, @NonNull Set<String> bypassAppsPackageName) {
        return VstoreManager.getInstance(context).encode(false, KvStoreConstants.KEY_CORE_SERVICE_APPS_BYPASS_PACKAGE_NAME, bypassAppsPackageName);
    }

    public static List<String> getDomainsBypass() {
        List<String> domains = new ArrayList<>();
        domains.add("api.ironmeta.com");
        return domains;
    }
}
