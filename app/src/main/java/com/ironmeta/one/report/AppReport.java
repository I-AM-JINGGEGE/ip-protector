package com.ironmeta.one.report;

import static com.ironmeta.one.report.ReportConstants.AppReport.ACTION_VPN_CONNECT;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;

import com.ironmeta.base.utils.YoLog;
import com.ironmeta.one.MainApplication;
import com.ironmeta.one.ads.constant.AdFormat;
import com.ironmeta.one.ads.network.IpUtil;
import com.ironmeta.one.annotation.ConnectionSource;
import com.ironmeta.one.annotation.ServerSource;
import com.ironmeta.one.base.net.NetworkManager;
import com.ironmeta.one.ui.widget.RatingBar;
import com.ironmeta.tahiti.TahitiCoreServiceStateInfoManager;
import com.ironmeta.tahiti.constants.CoreServiceStateConstants;
import com.roiquery.analytics.DTAnalytics;
import com.roiquery.analytics.DTAnalyticsUtils;
import com.roiquery.analytics.ROIQueryAnalytics;
import com.sdk.ssmod.IMSDK;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tom J
 * @package com.ironmeta.one.report
 * @description <p>上报服务器和用户行为相关信息</p>
 * @date 2021/8/25 9:33 上午
 */
public class AppReport {
    private static final String TAG = AppReport.class.getSimpleName();

    private static String sConnectionSource = null;

    private static long mConnectStartTime = 0;

    public static void init(){
        IMSDK.INSTANCE.getVpnState().observeForever(state -> {
            reportConnectionSuccess(IpUtil.INSTANCE.getConnectedIdAddress());
        });
    }

    public static void reportClickAddTime1(String source) {
        YoLog.i(TAG, "reportClickAddTime1@ source : " + source);
        Map<String, Object> params = new HashMap<>();
        params.put(ReportConstants.AppReport.KEY_SOURCE, source);
        // ROIQueryAnalytics.track(ReportConstants.AppReport.ACTION_CLICK_ADD_TIME_1, params);
    }

    public static void setConnectionSource(@ConnectionSource String source){
        sConnectionSource = source;
    }

    public static void reportServerRefreshStart(String host, @ServerSource String source) {
        YoLog.i(TAG, "reportServerRefreshStart@ host : " + host + ", source = " + source);
        Map<String, Object> params = new HashMap<>();
        params.put(ReportConstants.AppReport.KEY_SOURCE, source);
        params.put(ReportConstants.AppReport.KEY_HOST, host);
        // ROIQueryAnalytics.track(ReportConstants.AppReport.ACTION_SERVERS_REFRESH_START, params);
    }

    public static void reportServerRefreshFinish(String host, @ServerSource String source,String result, String errorCode, String errorMessage, long durationMills) {
        if (source == null) {
            YoLog.i(TAG, "reportServerRefreshFinish@ source is null");
            return;
        }
        boolean networkEnabled = NetworkManager.getInstance(MainApplication.Companion.getContext()).getConnected();
        String connectedIp = IpUtil.INSTANCE.getConnectedIdAddress();
        YoLog.i(TAG, "reportServerRefreshFinish@ host: " + host + " ,result : " + result + ", source: " + source + ", errorCode:" + errorCode + " ,errorMessage: " + errorMessage + ", network enabled: " + networkEnabled + ", connected ip: " + connectedIp + ", duration: " + durationMills);
        Map<String, Object> params = new HashMap<>();
        params.put(ReportConstants.AppReport.KEY_SOURCE, source);
        params.put(ReportConstants.AppReport.KEY_RESULT, result);
        params.put(ReportConstants.AppReport.KEY_HOST, host);
        if (!TextUtils.isEmpty(errorCode)) {
            params.put(ReportConstants.AppReport.KEY_ERROR_CODE, errorCode);
        }
        if (!TextUtils.isEmpty(errorMessage)) {
            params.put(ReportConstants.AppReport.KEY_ERROR_MSG, errorMessage);
        }
        if (!TextUtils.isEmpty(connectedIp)) {
            params.put(ReportConstants.AppReport.KEY_IP_ADDRESS, connectedIp);
        }
        params.put(ReportConstants.AppReport.KEY_DURATION, durationMills);
        params.put(ReportConstants.AppReport.KEY_NETWORK_ENABLED, networkEnabled);
        // ROIQueryAnalytics.track(ReportConstants.AppReport.ACTION_SERVERS_REFRESH_FINISH, params);
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

    private static void reportConnectionSuccess(String ip) {
        YoLog.i(TAG, "reportConnectionSuccess@  " + sConnectionSource + ", ip:" + ip);
        Map<String, Object> params = new HashMap<>();
        params.put(ReportConstants.AppReport.KEY_SOURCE, sConnectionSource);
        params.put(ReportConstants.AppReport.KEY_IP_ADDRESS, ip);
        // DTAnalytics.track(ReportConstants.AppReport.ACTION_CONNECTION_SUCCESS, params);
        // DTAnalyticsUtils.trackTimerStart(ACTION_VPN_CONNECT);
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
