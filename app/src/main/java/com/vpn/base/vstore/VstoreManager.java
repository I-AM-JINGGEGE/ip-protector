package com.vpn.base.vstore;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class VstoreManager {
    private static VstoreManager sVstoreManager = null;

    private static final String STORE_ID_FOR_MAIN_PROCESS = "main";
    private static final String STORE_ID_FOR_MULTI_PROCESS = "multi";

    public static synchronized VstoreManager getInstance(@NonNull Context context) {
        if (sVstoreManager == null) {
            sVstoreManager = new VstoreManager(context.getApplicationContext());
        }
        return sVstoreManager;
    }

    private Map<String, SharedPreferences> mStoreMap = new HashMap<>();
    private Context mAppContext;

    private VstoreManager(@NonNull Context appContext) {
        mAppContext = appContext;
        // 主进程使用 MODE_PRIVATE，多进程使用 MODE_MULTI_PROCESS
        createSharedPreferences(STORE_ID_FOR_MAIN_PROCESS, Context.MODE_PRIVATE);
        createSharedPreferences(STORE_ID_FOR_MULTI_PROCESS, Context.MODE_MULTI_PROCESS);
    }

    private SharedPreferences createSharedPreferences(String storeId, int mode) {
        SharedPreferences sharedPreferences = mAppContext.getSharedPreferences(storeId, mode);
        mStoreMap.put(storeId, sharedPreferences);
        return sharedPreferences;
    }

    @NonNull
    private SharedPreferences getSharedPreferences(boolean usedByMainProcessOnly) {
        return Objects.requireNonNull(usedByMainProcessOnly ? mStoreMap.get(STORE_ID_FOR_MAIN_PROCESS) : mStoreMap.get(STORE_ID_FOR_MULTI_PROCESS));
    }

    public boolean encode(boolean usedByMainProcessOnly, String key, boolean value) {
        return getSharedPreferences(usedByMainProcessOnly).edit().putBoolean(key, value).commit();
    }

    public boolean decode(boolean usedByMainProcessOnly, String key, boolean defaultValue) {
        return getSharedPreferences(usedByMainProcessOnly).getBoolean(key, defaultValue);
    }

    public boolean encode(boolean usedByMainProcessOnly, String key, int value) {
        return getSharedPreferences(usedByMainProcessOnly).edit().putInt(key, value).commit();
    }

    public int decode(boolean usedByMainProcessOnly, String key, int defaultValue) {
        return getSharedPreferences(usedByMainProcessOnly).getInt(key, defaultValue);
    }

    public boolean encode(boolean usedByMainProcessOnly, String key, long value) {
        return getSharedPreferences(usedByMainProcessOnly).edit().putLong(key, value).commit();
    }

    public long decode(boolean usedByMainProcessOnly, String key, long defaultValue) {
        return getSharedPreferences(usedByMainProcessOnly).getLong(key, defaultValue);
    }

    public boolean encode(boolean usedByMainProcessOnly, String key, double value) {
        return getSharedPreferences(usedByMainProcessOnly).edit().putFloat(key, (float) value).commit();
    }

    public double decode(boolean usedByMainProcessOnly, String key, double defaultValue) {
        return getSharedPreferences(usedByMainProcessOnly).getFloat(key, (float) defaultValue);
    }

    public boolean encode(boolean usedByMainProcessOnly, String key, String value) {
        return getSharedPreferences(usedByMainProcessOnly).edit().putString(key, value).commit();
    }

    public String decode(boolean usedByMainProcessOnly, String key, String defaultValue) {
        return getSharedPreferences(usedByMainProcessOnly).getString(key, defaultValue);
    }

    public boolean encode(boolean usedByMainProcessOnly, String key, Set<String> value) {
        return getSharedPreferences(usedByMainProcessOnly).edit().putStringSet(key, value).commit();
    }

    public Set<String> decode(boolean usedByMainProcessOnly, String key, Set<String> defaultValue) {
        return getSharedPreferences(usedByMainProcessOnly).getStringSet(key, defaultValue);
    }

    public void remove(boolean usedByMainProcessOnly, String key) {
        getSharedPreferences(usedByMainProcessOnly).edit().remove(key).commit();
    }
}
