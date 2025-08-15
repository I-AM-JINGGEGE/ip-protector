package com.vpn.android.region;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vpn.android.R;
import com.vpn.android.base.utils.BuildConfigUtils;

import java.util.Locale;

public class RegionUtils {
    public static String getRegionName(@NonNull Context context, String regionCode) {
        if (TextUtils.isEmpty(regionCode)) {
            return context.getString(R.string.vs_common_unknown);
        }
        if (TextUtils.equals(regionCode, RegionConstants.REGION_CODE_DEFAULT)) {
            return context.getString(R.string.vs_common_auto_choose);
        }
        try {
            Locale locale = new Locale("", regionCode.toLowerCase());
            return locale.getDisplayCountry(locale);
        } catch (Exception e) {
            return context.getString(R.string.vs_common_unknown);
        }
    }

    public static String getRegionDesc(@NonNull Context context, String regionCode, @Nullable String regionName) {
        if (TextUtils.isEmpty(regionCode)) {
            return context.getString(R.string.vs_common_default);
        }
        if (TextUtils.equals(regionCode, RegionConstants.REGION_CODE_DEFAULT)) {
            return context.getString(R.string.vs_region_default_desc);
        }
        return regionName;
    }

    public static int getRegionFlagImageResource(@NonNull Context context, @Nullable String regionCode) {
        // try to fix bug that regionCode may be null, but still not know that why regionCode may be null
        if (regionCode == null || regionCode == RegionConstants.REGION_CODE_DEFAULT) {
            return R.mipmap.region_flag_default;
        }

        Context appContext = context.getApplicationContext();
        int resId = appContext.getResources().getIdentifier("region_flag_" + regionCode.toLowerCase(), "mipmap", BuildConfigUtils.getPackageName(appContext));
        if (resId == 0) {
            return R.mipmap.region_flag_default;
        }
        return resId;
    }
}
