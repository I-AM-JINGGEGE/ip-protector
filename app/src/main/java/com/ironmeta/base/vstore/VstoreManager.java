package com.ironmeta.base.vstore;

import android.content.Context;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import com.tencent.mmkv.MMKV;

import org.extra.relinker.ReLinker;

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

    private Map<String, MMKV> mStoreMap = new HashMap<>();

    private VstoreManager(@NonNull Context appContext) {
        MMKV.initialize(appContext, appContext.getFilesDir().getAbsolutePath() + "/mmkv", libName -> ReLinker.loadLibrary(appContext, libName));
        createMMKV(STORE_ID_FOR_MAIN_PROCESS, MMKV.SINGLE_PROCESS_MODE);
        createMMKV(STORE_ID_FOR_MULTI_PROCESS, MMKV.MULTI_PROCESS_MODE);
    }

    private MMKV createMMKV(String storeId, int mode) {
        MMKV mmkv = MMKV.mmkvWithID(storeId, mode);
        mStoreMap.put(storeId, mmkv);
        return mmkv;
    }

    @NonNull
    private MMKV getMmkv(boolean usedByMainProcessOnly) {
        return Objects.requireNonNull(usedByMainProcessOnly ? mStoreMap.get(STORE_ID_FOR_MAIN_PROCESS) : mStoreMap.get(STORE_ID_FOR_MULTI_PROCESS));
    }

    public boolean encode(boolean usedByMainProcessOnly, String key, boolean value) {
        return getMmkv(usedByMainProcessOnly).encode(key, value);
    }

    public boolean decode(boolean usedByMainProcessOnly, String key, boolean defaultValue) {
        return getMmkv(usedByMainProcessOnly).decodeBool(key, defaultValue);
    }

    public boolean encode(boolean usedByMainProcessOnly, String key, int value) {
        return getMmkv(usedByMainProcessOnly).encode(key, value);
    }

    public int decode(boolean usedByMainProcessOnly, String key, int defaultValue) {
        return getMmkv(usedByMainProcessOnly).decodeInt(key, defaultValue);
    }

    public boolean encode(boolean usedByMainProcessOnly, String key, long value) {
        return getMmkv(usedByMainProcessOnly).encode(key, value);
    }

    public long decode(boolean usedByMainProcessOnly, String key, long defaultValue) {
        return getMmkv(usedByMainProcessOnly).decodeLong(key, defaultValue);
    }

    public boolean encode(boolean usedByMainProcessOnly, String key, double value) {
        return getMmkv(usedByMainProcessOnly).encode(key, value);
    }

    public double decode(boolean usedByMainProcessOnly, String key, double defaultValue) {
        return getMmkv(usedByMainProcessOnly).decodeDouble(key, defaultValue);
    }

    public boolean encode(boolean usedByMainProcessOnly, String key, String value) {
        return getMmkv(usedByMainProcessOnly).encode(key, value);
    }

    public String decode(boolean usedByMainProcessOnly, String key, String defaultValue) {
        return getMmkv(usedByMainProcessOnly).decodeString(key, defaultValue);
    }

    public boolean encode(boolean usedByMainProcessOnly, String key, Parcelable value) {
        return getMmkv(usedByMainProcessOnly).encode(key, value);
    }

    public <T extends Parcelable> T decode(boolean usedByMainProcessOnly, String key, Class<T> tClass, T defaultValue) {
        return getMmkv(usedByMainProcessOnly).decodeParcelable(key, tClass, defaultValue);
    }

    public boolean encode(boolean usedByMainProcessOnly, String key, Set<String> value) {
        return getMmkv(usedByMainProcessOnly).encode(key, value);
    }

    public Set<String> decode(boolean usedByMainProcessOnly, String key, Set<String> defaultValue) {
        return getMmkv(usedByMainProcessOnly).decodeStringSet(key, defaultValue);
    }

    public boolean contains(boolean usedByMainProcessOnly, String key) {
        return getMmkv(usedByMainProcessOnly).containsKey(key);
    }

    public void remove(boolean usedByMainProcessOnly, String key) {
        getMmkv(usedByMainProcessOnly).removeValueForKey(key);
    }
}
