package com.vpn.android.vlog;

import com.google.firebase.analytics.FirebaseAnalytics;

public class VlogConstants {
    public static class Param extends FirebaseAnalytics.Param {
        public static final String V_NET_CONNECTED = "v_net_connected";

        public static final String V_CODE = "v_code";
        public static final String V_UUID = "v_uuid";
        public static final String V_IP = "v_ip";
        public static final String V_PORT = "v_port";
        public static final String V_RESULT = "v_result";
        public static final String V_TIME = "v_time";
        public static final String V_TYPE = "v_type";
        public static final String V_PLATFORM = "v_platform";
        public static final String V_SEQ = "v_seq";
        public static final String V_FILLED = "v_filled";
        public static final String V_ERROR_MSG = "v_error_msg";
        public static final String DOMAIN = "domain";
        public static final String LOAD_TIME = "load_time";

        public static final String V_TO_CONNECT_REGION_UUID = "v_to_connect_region_uuid";
        public static final String V_TO_CONNECT_VIP = "v_to_connect_vip";

        public static final String V_AD_QUALITY_SHOW_TS = "v_ad_q_show_ts";
        public static final String V_AD_QUALITY_CLICK_TS = "v_ad_q_click_ts";
        public static final String V_AD_QUALITY_LEFT_APPLICATION_TS = "v_ad_q_left_app_ts";
        public static final String V_AD_QUALITY_APP_FOREGROUNDED_TS = "v_ad_q_app_fg_ts";
        public static final String V_AD_QUALITY_CLICK_GAP = "v_ad_q_click_gap";
        public static final String V_AD_QUALITY_RETURN_GAP = "v_ad_q_return_gap";
    }

    public static class Event extends FirebaseAnalytics.Event {

    }

    public static class UserProperty extends FirebaseAnalytics.UserProperty {
        public static final String U_MCC = "u_mcc";
        public static final String U_MNC = "u_mnc";
        public static final String U_OS_COUNTRY = "u_os_country";
        public static final String U_OS_LANG = "u_os_lang";
    }
}
