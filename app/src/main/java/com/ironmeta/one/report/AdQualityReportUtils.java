package com.ironmeta.one.report;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.ironmeta.one.base.utils.LogUtils;
import com.ironmeta.one.vlog.VlogManager;

public class AdQualityReportUtils {
    private static final String TAG = AdQualityReportUtils.class.getSimpleName();

    public static void reportShow(@NonNull Context context, @NonNull String location, int adType, int adPlatform, String adId, String seq, long showTS) {
        LogUtils.i(TAG, "reportShow@location: " + location + ", adType: " + adType + ", adPlatform: " + adPlatform + ", adId: " + adId + ", seq: " + seq + ", showTS: " + showTS);

        Bundle bundle = new Bundle();
        bundle.putString(ReportConstants.Param.LOCATION, location);
        bundle.putInt(ReportConstants.Param.V_TYPE, adType);
        bundle.putInt(ReportConstants.Param.V_PLATFORM, adPlatform);
        bundle.putString(ReportConstants.Param.V_UUID, adId);
        bundle.putString(ReportConstants.Param.V_SEQ, seq);
        bundle.putLong(ReportConstants.Param.V_AD_QUALITY_SHOW_TS, showTS);
        VlogManager.getInstance(context).logEvent(ReportConstants.Event.C_AD_QUALITY_SHOW, bundle);
    }

    public static void reportClick(@NonNull Context context, @NonNull String location, int adType, int adPlatform, String adId, String seq, long clickTS) {
        LogUtils.i(TAG, "reportClick@location: " + location + ", adType: " + adType + ", adPlatform: " + adPlatform + ", adId: " + adId + ", seq: " + seq + ", clickTS: " + clickTS);

        Bundle bundle = new Bundle();
        bundle.putString(ReportConstants.Param.LOCATION, location);
        bundle.putInt(ReportConstants.Param.V_TYPE, adType);
        bundle.putInt(ReportConstants.Param.V_PLATFORM, adPlatform);
        bundle.putString(ReportConstants.Param.V_UUID, adId);
        bundle.putString(ReportConstants.Param.V_SEQ, seq);
        bundle.putLong(ReportConstants.Param.V_AD_QUALITY_CLICK_TS, clickTS);
        VlogManager.getInstance(context).logEvent(ReportConstants.Event.C_AD_QUALITY_CLICK, bundle);
    }

    public static void reportLeftApplication(@NonNull Context context, @NonNull String location, int adType, int adPlatform, String adId, String seq, long leftApplicationTS) {
        LogUtils.i(TAG, "reportLeftApplication@location: " + location + ", adType: " + adType + ", adPlatform: " + adPlatform + ", adId: " + adId + ", seq: " + seq +", leftApplicationTS: " + leftApplicationTS);

        Bundle bundle = new Bundle();
        bundle.putString(ReportConstants.Param.LOCATION, location);
        bundle.putInt(ReportConstants.Param.V_TYPE, adType);
        bundle.putInt(ReportConstants.Param.V_PLATFORM, adPlatform);
        bundle.putString(ReportConstants.Param.V_UUID, adId);
        bundle.putString(ReportConstants.Param.V_SEQ, seq);
        bundle.putLong(ReportConstants.Param.V_AD_QUALITY_LEFT_APPLICATION_TS, leftApplicationTS);
        VlogManager.getInstance(context).logEvent(ReportConstants.Event.C_AD_QUALITY_LEFT_APPLICATION, bundle);
    }

    public static void reportQuality(@NonNull Context context, @NonNull String location, int adType, int adPlatform, String adId, String seq, long showTS, long clickTS, long leftApplicationTS, long appForegroundedTS) {
        LogUtils.i(TAG, "reportQuality@location: " + location + ", adType: " + adType + ", adPlatform: " + adPlatform + ", adId: " + adId + ", seq: " + seq
                + ", showTS: " + showTS
                + ", clickTS: " + clickTS
                + ", leftApplicationTS: " + leftApplicationTS
                + ", appForegroundedTS: " + appForegroundedTS
                + ", click gap: " + (clickTS - showTS)
                + ", return gap: " + (appForegroundedTS - leftApplicationTS));

        Bundle bundle = new Bundle();
        bundle.putString(ReportConstants.Param.LOCATION, location);
        bundle.putInt(ReportConstants.Param.V_TYPE, adType);
        bundle.putInt(ReportConstants.Param.V_PLATFORM, adPlatform);
        bundle.putString(ReportConstants.Param.V_UUID, adId);
        bundle.putString(ReportConstants.Param.V_SEQ, seq);
        bundle.putLong(ReportConstants.Param.V_AD_QUALITY_SHOW_TS, showTS);
        bundle.putLong(ReportConstants.Param.V_AD_QUALITY_CLICK_TS, clickTS);
        bundle.putLong(ReportConstants.Param.V_AD_QUALITY_LEFT_APPLICATION_TS, leftApplicationTS);
        bundle.putLong(ReportConstants.Param.V_AD_QUALITY_APP_FOREGROUNDED_TS, appForegroundedTS);
        bundle.putLong(ReportConstants.Param.V_AD_QUALITY_CLICK_GAP, (clickTS - showTS));
        bundle.putLong(ReportConstants.Param.V_AD_QUALITY_RETURN_GAP, (appForegroundedTS - leftApplicationTS));
        VlogManager.getInstance(context).logEvent(ReportConstants.Event.C_AD_QUALITY, bundle);
    }
}
