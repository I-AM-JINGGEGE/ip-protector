package com.vpn.android.base.utils;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;
import java.util.Locale;

public class DeviceUtils {
    /**
     * 获取mcc
     * @param context
     * @return
     */
    @Deprecated
    public static String getMcc(@NonNull Context context) {
        return getSimMcc(context);
    }

    public static String getSimMcc(@NonNull Context context) {
        String mcc_mnc = getSimOperator(context);

        if (mcc_mnc != null && mcc_mnc.length() >= 3) {
            StringBuilder mcc = new StringBuilder();
            mcc.append(mcc_mnc, 0, 3);

            return mcc.toString();
        }

        return "";
    }

    public static String getNetMcc(@NonNull Context context) {
        String mcc_mnc = getNetworkOperator(context);

        if (mcc_mnc != null && mcc_mnc.length() >= 3) {
            StringBuilder mcc = new StringBuilder();
            mcc.append(mcc_mnc, 0, 3);

            return mcc.toString();
        }

        return "";
    }

    /**
     * 获取mnc
     * @param context
     * @return
     */
    public static String getMnc(@NonNull Context context) {
        String mcc_mnc = getSimOperator(context);

        if (mcc_mnc != null && mcc_mnc.length() >= 5) {
            StringBuilder mnc = new StringBuilder();
            mnc.append(mcc_mnc, 3, 5);

            return mnc.toString();
        }

        return "";
    }

    /**
     * 获取手机型号
     * @return
     */
    @NonNull
    public static String getDeviceModel() {
        String model = android.os.Build.MODEL;
        if (!TextUtils.isEmpty(model)) {
            return model;
        }
        return "";
    }

    /**
     * 获取手机厂商
     * @return
     */
    @NonNull
    public static String getDeviceBrand() {
        String brand = android.os.Build.BRAND;
        if (!TextUtils.isEmpty(brand)) {
            return brand;
        }
        return "";
    }

    /**
     * 获取系统版本号
     * @return
     */
    @NonNull
    public static String getOSVersion() {
        String osVersion = android.os.Build.VERSION.RELEASE;
        if (!TextUtils.isEmpty(osVersion)) {
            return osVersion;
        }
        return "";
    }

    /**
     * 获取系统国家
     */
    @NonNull
    public static String getOSCountry(@NonNull Context context) {
        Locale locale = getLocale(context);
        if (locale == null) {
            return "";
        }

        String country = locale.getCountry();
        if (!TextUtils.isEmpty(country)) {
            return country ;
        }
        return "";
    }

    /**
     * 获取系统语言
     */
    @NonNull
    public static String getOSLang(@NonNull Context context) {
        Locale locale = getLocale(context);
        if (locale == null) {
            return "";
        }

        String lang = locale.getLanguage();
        if (!TextUtils.isEmpty(lang)) {
            return lang ;
        }
        return "";
    }

    private static String getSimOperator(Context context) {
        if (context == null) {
            return null;
        }

        TelephonyManager tm = getTelephonyManager(context);
        if (tm == null) {
            return null;
        }

        return tm.getSimOperator();
    }

    private static String getNetworkOperator(Context context) {
        if (context == null) {
            return null;
        }

        TelephonyManager tm = getTelephonyManager(context);
        if (tm == null) {
            return null;
        }

        return tm.getSimOperator();
    }

    private static TelephonyManager getTelephonyManager(Context context) {
        if (context == null) {
            return null;
        }

        try {
            return (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    private static Locale getLocale(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getResources().getConfiguration().getLocales().get(0);
        }

        return context.getResources().getConfiguration().locale;
    }

    /** advertising id begin **/
    @WorkerThread
    @Nullable
    public static String getAdvertisingId(Context context) {
        Context appContext = context.getApplicationContext();
        AdvertisingIdClient.Info info = null;
        try {
            info = AdvertisingIdClient.getAdvertisingIdInfo(appContext);
        } catch (IOException | GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        }
        if (info != null) {
            return info.getId();
        }
        return null;
    }
    /** advertising id end **/
}
