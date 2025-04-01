package com.ironmeta.one.report;

import android.text.TextUtils;

import com.ironmeta.base.utils.YoLog;
import com.ironmeta.one.ads.constant.AdFormat;
import com.ironmeta.one.annotation.ConnectionSource;
import com.ironmeta.one.ui.widget.RatingBar;

import java.util.HashMap;
import java.util.Map;

public class AppReport {
    private static final String TAG = AppReport.class.getSimpleName();

    private static String sConnectionSource = null;

    public static void reportClickAddTime1(String source) {
        YoLog.i(TAG, "reportClickAddTime1@ source : " + source);
        Map<String, Object> params = new HashMap<>();
        params.put(ReportConstants.AppReport.KEY_SOURCE, source);
        // ROIQueryAnalytics.track(ReportConstants.AppReport.ACTION_CLICK_ADD_TIME_1, params);
    }

    public static void setConnectionSource(@ConnectionSource String source){
        sConnectionSource = source;
    }

    public static void reportToConnection() {
        if (TextUtils.isEmpty(sConnectionSource)){
            YoLog.i(TAG,"reportToConnection@ source is null");
            return;
        }
        YoLog.i(TAG, "reportToConnection@ source : " + sConnectionSource);
        Map<String, Object> params = new HashMap<>();
        params.put(ReportConstants.AppReport.KEY_SOURCE, sConnectionSource);
        // DTAnalytics.track(ReportConstants.AppReport.ACTION_TO_CONNECT, params);
    }

    public static void reportConnectionStart() {
        if (TextUtils.isEmpty(sConnectionSource)){
            YoLog.i(TAG,"reportConnectionStart@ source is null");
            return;
        }
        YoLog.i(TAG, "reportConnectionStart@ source : " + sConnectionSource);
        Map<String, Object> params = new HashMap<>();
        params.put(ReportConstants.AppReport.KEY_SOURCE, sConnectionSource);
        // DTAnalytics.track(ReportConstants.AppReport.ACTION_CONNECTION_START, params);
    }

    public static void reportConnectionFail(String errorCode, String errorMessage) {
        YoLog.i(TAG, "reportConnectionFail@ errorCode : " + errorCode + " ,errorMessage: " + errorMessage);
        boolean isErrorCodeEmpty = TextUtils.isEmpty(errorCode);
        boolean isErrorMessageEmpty = TextUtils.isEmpty(errorMessage);
        if (isErrorCodeEmpty && isErrorMessageEmpty) {
            // DTAnalytics.track(ReportConstants.AppReport.ACTION_CONNECTION_FAIL);
        } else {
            Map<String, Object> params = new HashMap<>();
            if (!isErrorCodeEmpty) {
                params.put(ReportConstants.AppReport.KEY_ERROR_CODE, errorCode);
            }
            if (!isErrorMessageEmpty) {
                params.put(ReportConstants.AppReport.KEY_ERROR_MSG, errorMessage);
            }
            // ROIQueryAnalytics.track(ReportConstants.AppReport.ACTION_CONNECTION_FAIL, params);
        }
    }

    private static void reportConnectionDisconnected(long duration) {
        YoLog.i(TAG, "reportConnectionDisconnected@ duration to seconds: " + duration /1000 + " ,source :" + sConnectionSource);
        Map<String, Object> params = new HashMap<>();
        params.put(ReportConstants.AppReport.KEY_DURATION, duration /1000);
        // ROIQueryAnalytics.track(ReportConstants.AppReport.ACTION_CONNECTION_DISCONNECTED, params);
        // DTAnalyticsUtils.trackTimerEnd(ACTION_VPN_CONNECT);
    }

    public static void reportClickRate(String source, RatingBar.Type item) {
        YoLog.i(TAG, "reportClickRate@ source : " + source + ", type : " + item);
        Map<String, Object> params = new HashMap<>();
        if (item != null) {
            params.put(ReportConstants.AppReport.KEY_RATE_ITEM, item.toString());
        }
        params.put(ReportConstants.AppReport.KEY_SOURCE, source);
        // ROIQueryAnalytics.track(ReportConstants.AppReport.ACTION_CLICK_RATE, params);
    }

    public static void reportAdIgnoreLoading(boolean vpnConnected, AdFormat adFormat) {
        YoLog.i(TAG, "reportAdIgnoreLoading");
        Map<String, Object> params = new HashMap<>();
        params.put(ReportConstants.AppReport.KEY_AD_TYPE, adFormat.name());
        params.put(ReportConstants.AppReport.KEY_CONNECTED, vpnConnected);
        // DTAnalytics.track(ReportConstants.AppReport.ACTION_AD_IGNORE_LOADING, params);
    }

    public static void reportAdInitBegin(boolean vpnConnected) {
        YoLog.i(TAG, "reportAdInitBegin");
        Map<String, Object> params = new HashMap<>();
        params.put(ReportConstants.AppReport.KEY_CONNECTED, vpnConnected);
        // DTAnalytics.track(ReportConstants.AppReport.ACTION_AD_INIT_BEGIN, params);
    }

    public static void reportAdInitEnd(boolean vpnConnected, boolean result, int errorCode, String errorMsg) {
        YoLog.i(TAG, "reportAdInitEnd. vpnConnected = " + vpnConnected + ", errorCode = " + errorCode + ", error msg = " + errorMsg);
        Map<String, Object> params = new HashMap<>();
        params.put(ReportConstants.AppReport.KEY_CONNECTED, vpnConnected);
        params.put(ReportConstants.AppReport.KEY_RESULT, result);
        params.put(ReportConstants.AppReport.KEY_ERROR_CODE, errorCode);
        params.put(ReportConstants.AppReport.KEY_ERROR_MSG, errorMsg);
        // DTAnalytics.track(ReportConstants.AppReport.ACTION_AD_INIT_END, params);
    }

    public static void reportOpenNetworkSettings() {
        YoLog.i(TAG, "reportOpenNetworkSettings");
        // DTAnalytics.track(ReportConstants.AppReport.ACTION_OPEN_NETWORK_SETTINGS);
    }

    public static void reportNetworkSettingsDialogShow() {
        YoLog.i(TAG, "reportNetworkSettingsDialogShow");
        // DTAnalytics.track(ReportConstants.AppReport.ACTION_NETWORK_SETTINGS_DIALOG_SHOW);
    }

    public static void reportCloseNetworkSettings() {
        YoLog.i(TAG, "reportCloseNetworkSettings");
        // DTAnalytics.track(ReportConstants.AppReport.ACTION_CLOSE_NETWORK_SETTINGS);
    }
}
