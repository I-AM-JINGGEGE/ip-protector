package com.vpn.android.constants;

/**
 * @author Tom J
 * @description <p></p>
 * @date 2021/11/15 2:32 下午
 */
public class RemoteConstants {
    //remote config key
    public static final String KEY_FAKE_CONNECTION_DURATION = "fake_connection_duration";
    public static final long FAKE_CONNECTION_DURING_DEFAULT = 20000;

    //原有配置
    public static final String UPDATE_VERSION = "vcfg_update_version";
    public static final int UPDATE_VERSION_VALUE_DEFAULT = 12205;

    public static final String DEFAULT_APP_OPEN_COUNT = "default_app_open_count";
    public static final int DEFAULT_APP_OPEN_COUNT_VALUE_DEFAULT = 2;

    public static final String OPEN_AD_LOAD_MAX_DURATION = "open_ad_load_max_duration";
    public static final long OPEN_AD_LOAD_MAX_DURATION_VALUE_DEFAULT = 15000;
    public static final long COLD_START_MAX_DURATION_VALUE_DEFAULT = 12000;
    public static final long ADD_TIME_MAX_DURATION_VALUE_DEFAULT = 15000;

    public static final String REPORT_BEAT_DURATION = "report_beat_duration";
    public static final long REPORT_BEAT_DURATION_VALUE_DEFAULT = 1000 * 60 * 5;
    public static final String CONNECTED_NATIVE_AD_SWITCH = "connected_native_ad_switch";
    public static final boolean CONNECTED_NATIVE_AD_SWITCH_DEFAULT = false;
}
