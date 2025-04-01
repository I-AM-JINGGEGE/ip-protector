package com.ironmeta.one.report;

import com.ironmeta.one.vlog.VlogConstants;

public class ReportConstants {

    public static class Param extends VlogConstants.Param {
        public static final String IP_ADDRESS = "ip_address";
    }

    public static class AppReport{

        public static final String KEY_RATE_ITEM = "rate_item";
        public static final String KEY_SOURCE = "source";
        public static final String KEY_HOST = "host";
        public static final String KEY_RESULT = "result";
        public static final String KEY_ERROR_CODE = "error_code";
        public static final String KEY_ERROR_MSG = "error_msg";
        public static final String KEY_DURATION = "duration";
        public static final String KEY_NETWORK_ENABLED = "network_enabled";
        public static final String KEY_AD_TYPE = "ad_type";
        public static final String KEY_CONNECTED = "connected";

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

}
