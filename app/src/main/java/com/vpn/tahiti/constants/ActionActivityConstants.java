package com.vpn.tahiti.constants;

public class ActionActivityConstants {
    public static final String KEY_EXTRA_ACTION = "key_extra_action";
    public static final int KEY_EXTRA_ACTION_VALUE_IDLE = -1;
    public static final int KEY_EXTRA_ACTION_VALUE_CORE_SERVICE_DETERMINE = 0X000001;
    public static final int KEY_EXTRA_ACTION_VALUE_CORE_SERVICE_CONNECTED = 0X000002;
    public static final int KEY_EXTRA_ACTION_VALUE_CORE_SERVICE_DISCONNECTED = 0X000003;
    public static final int KEY_EXTRA_ACTION_VALUE_CONNECTION_INFO_NOTIFICATION = 0X000004;
    public static final int KEY_EXTRA_ACTION_VALUE_CONFIGURE = 0X000005;
    public static final int KEY_EXTRA_ACTION_VALUE_TO_CONNECT = 0X000006;
    public static final int KEY_EXTRA_ACTION_VALUE_ADD_TIME = 0X000007;

    public static final String KEY_EXTRA_ERROR_CODE = "key_extra_error_code";
    public static final String KEY_EXTRA_MSG = "key_extra_msg"; // connected region code|connected ip|connected seconds|connected upload total|connected download total

    public static final int PENDING_INTENT_REQUEST_CODE_CONNECTION_INFO_NOTIFICATION = 1;
    public static final int PENDING_INTENT_REQUEST_CODE_CONFIGURE = 2;
    public static final int PENDING_INTENT_REQUEST_CODE_NOTIFICATION_NETWORK = 3;
}
