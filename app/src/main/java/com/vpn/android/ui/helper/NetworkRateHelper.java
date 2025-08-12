package com.vpn.android.ui.helper;

import android.content.Context;

import com.vpn.android.base.utils.LogUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkRateHelper {
    private static final String TAG = NetworkRateHelper.class.getSimpleName();

    private static NetworkRateHelper sNetworkRateHelper = null;
    private Context context;

    private NetworkRateHelper(Context context) {
        this.context = context;
    }

    public static NetworkRateHelper getInstance(Context context) {

        if (sNetworkRateHelper == null) {
            synchronized (NavHelper.class) {
                if (sNetworkRateHelper == null) {
                    sNetworkRateHelper = new NetworkRateHelper(context);
                }
            }
        }
        return sNetworkRateHelper;
    }

    public String matchRateValue(String line) {
        try {
            String pattern = "([1-9]\\d*\\.?\\d*)|(0\\.\\d*[1-9])";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(line);

            if (m.find()) {
                return m.group(0);
            } else {
                return "0";
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "matchRateValue@appears an exception.");
            return "0";
        }
    }

    public String matchRateUnit(String line) {
        try {
            String pattern = "([kKmMgGtTpPeEzZyY]?[bB])";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(line);
            if (m.find()) {
                return m.group(0);
            } else {
                return "B";
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "matchRateUnit@appears an exception.");
            return "B";
        }
    }
}
