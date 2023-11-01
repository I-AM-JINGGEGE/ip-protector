package com.ironmeta.one.report;

import com.ironmeta.one.vlog.VlogConstants;

public class ReportConstants {
    public static class Event extends VlogConstants.Event {
        public static final String C_AD_QUALITY_SHOW = "c_ad_q_show";
        public static final String C_AD_QUALITY_CLICK = "c_ad_q_click";
        public static final String C_AD_QUALITY_LEFT_APPLICATION = "c_ad_q_left_app";
        public static final String C_AD_QUALITY = "c_ad_q";
    }

    public static class Param extends VlogConstants.Param {
        public static final String IP_ADDRESS = "ip_address";
    }

    public static class AppReport{

        //report action
        public static final String ACTION_SERVERS_REFRESH_START = "servers_refresh_start";
        public static final String ACTION_CLICK_ADD_TIME_1 = "click_add_time_1";
        public static final String ACTION_SERVERS_REFRESH_FINISH = "servers_refresh_finish";
        public static final String ACTION_TO_CONNECT = "to_connect";
        public static final String ACTION_CONNECTION_START = "connection_start";
        public static final String ACTION_CONNECTION_SUCCESS = "connection_success";
        public static final String ACTION_CONNECTION_FAIL = "connection_fail";
        public static final String ACTION_CONNECTION_DISCONNECTED = "connection_disconnected";

        //Test DT SDK function
        public static final String ACTION_VPN_CONNECT = "vpn_connection";
        public static final String ACTION_NETWORK_SETTINGS_DIALOG_SHOW = "network_settings_dialog_show";
        public static final String ACTION_OPEN_NETWORK_SETTINGS = "open_network_settings";
        public static final String ACTION_CLOSE_NETWORK_SETTINGS = "close_network_settings";

        public static final String ACTION_CLICK_RATE = "click_rate";
        public static final String ACTION_AD_IGNORE_LOADING = "ad_ignore_loading";
        public static final String ACTION_AD_INIT_BEGIN = "ad_init_begin";
        public static final String ACTION_AD_INIT_END = "ad_init_end";

        public static final String KEY_RATE_ITEM = "rate_item";
        public static final String KEY_SOURCE = "source";
        public static final String KEY_HOST = "host";
        public static final String KEY_IP_ADDRESS = "ip_address";
        public static final String KEY_RESULT = "result";
        public static final String KEY_ERROR_CODE = "error_code";
        public static final String KEY_ERROR_MSG = "error_msg";
        public static final String KEY_DURATION = "duration";
        public static final String KEY_NETWORK_ENABLED = "network_enabled";
        public static final String KEY_AD_TYPE = "ad_type";
        public static final String KEY_CONNECTED = "connected";

        public static final String RESULT_SUCCESS = "1";
        public static final String RESULT_FAILED = "0";

        //server
        public static final String SOURCE_SERVER_PAGE_MAIN = "page_main";
        public static final String SOURCE_SERVER_PROXY_CONNECTED = "proxy_connected";
        public static final String SOURCE_SERVER_NETWORK_CONNECTED = "network_connected";
        public static final String SOURCE_SERVER_PAGE_SERVER = "page_server";
        public static final String SOURCE_SERVER_COLD_START = "cold_start";

        //connect
        public static final String SOURCE_CONNECTION_PAGE_MAIN = "page_main_click";
        public static final String SOURCE_CONNECTION_NOTIFICATION = "notification";
        public static final String SOURCE_CONNECTION_NOTIFICATION_NETWORK = "notification_network";
        public static final String SOURCE_CONNECTION_PAGE_SERVER = "page_server_item_click";

        //rate
        public static final String SOURCE_CONNECTED_REPORT_PAGE = "connected_report";
        public static final String SOURCE_DISCONNECTED_REPORT_PAGE = "disconnected_report";
        public static final String SOURCE_RATE_DIALOG = "rate_dialog";
        public static final String SOURCE_ADD_TIME_NOTIFICATION = "notification";
        public static final String SOURCE_ADD_TIME_MAIN_PAGE_1 = "main_page_1";
        public static final String SOURCE_ADD_TIME_MAIN_PAGE_2 = "main_page_2";
        public static final String SOURCE_ADD_TIME_REPORT_PAGE_1 = "report_page_1";
        public static final String SOURCE_ADD_TIME_REPORT_PAGE_2 = "report_page_2";
    }

    public static class ErrorCode {
        public static final String NOT_AD_LOADED = "10001";

        public static final int AD_INIT_END_CODE_USER_PROFILE_EXIST = 101;
        public static final int AD_INIT_END_CODE_OLD_USER = 102;
        public static final int AD_INIT_END_CODE_REQUEST_SUCCESS = 103;
        public static final int AD_INIT_END_CODE_REQUEST_FAIL = 104;
    }

    public static class ErrorMessage {

        public static final String AD_INIT_END_USER_PROFILE_EXIST = "user profile exist";
        public static final String AD_INIT_END_OLD_USER = "is old user";
        public static final String AD_INIT_END_REQUEST_SUCCESS = "request success";
        public static final String AD_INIT_END_REQUEST_FAIL = "request fail";
    }
}
