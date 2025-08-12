package com.vpn.android.config;


import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.vpn.android.constants.RemoteConstants;

import java.util.HashMap;
import java.util.Map;

public class RemoteConfigManager {
    private static final String TAG = RemoteConfigManager.class.getSimpleName();
    private static volatile RemoteConfigManager instance;
    private RemoteConfigManager() {
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .build();
        remoteConfig.setConfigSettingsAsync(configSettings);

        Map<String, Object> defaultValues = generateDefaultValues();
        remoteConfig.setDefaultsAsync(defaultValues);

        remoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "onComplete: Fetch and activate succeeded");
            }else{
                Log.d(TAG, "onComplete: Fetch failed");
            }
        });
    }

    public static RemoteConfigManager getInstance() {
        if (instance == null) {
            synchronized (RemoteConfigManager.class) {
                if (instance == null) {
                    instance = new RemoteConfigManager();
                }
            }
        }
        return instance;
    }

    private Map<String, Object> generateDefaultValues() {
        Map<String, Object> defaultValues = new HashMap<>();
        defaultValues.put(RemoteConstants.KEY_FAKE_CONNECTION_DURATION, RemoteConstants.FAKE_CONNECTION_DURING_DEFAULT);
        defaultValues.put(RemoteConstants.UPDATE_VERSION,RemoteConstants.UPDATE_VERSION_VALUE_DEFAULT);
        defaultValues.put(RemoteConstants.DEFAULT_APP_OPEN_COUNT,RemoteConstants.DEFAULT_APP_OPEN_COUNT_VALUE_DEFAULT);
        defaultValues.put(RemoteConstants.OPEN_AD_LOAD_MAX_DURATION,RemoteConstants.OPEN_AD_LOAD_MAX_DURATION_VALUE_DEFAULT);
        defaultValues.put(RemoteConstants.REPORT_BEAT_DURATION,RemoteConstants.REPORT_BEAT_DURATION_VALUE_DEFAULT);
        defaultValues.put(RemoteConstants.CONNECTED_NATIVE_AD_SWITCH,RemoteConstants.CONNECTED_NATIVE_AD_SWITCH_DEFAULT);
        return defaultValues;
    }

    public long getFakeConnectionDuration() {
        return FirebaseRemoteConfig.getInstance().getLong(RemoteConstants.KEY_FAKE_CONNECTION_DURATION);
    }

    public long getReportBeatDuration() {
        return FirebaseRemoteConfig.getInstance().getLong(RemoteConstants.REPORT_BEAT_DURATION);
    }

    public long getDefaultAppOpenCount() {
        return FirebaseRemoteConfig.getInstance().getLong(RemoteConstants.DEFAULT_APP_OPEN_COUNT);
    }

    public Boolean getConnectedNativeAdSwitch() {
        return FirebaseRemoteConfig.getInstance().getBoolean(RemoteConstants.CONNECTED_NATIVE_AD_SWITCH);
    }
}
