package com.ironmeta.one.vlog;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.ironmeta.one.base.net.NetworkManager;
import com.ironmeta.one.base.utils.DeviceUtils;
import com.ironmeta.one.base.utils.ThreadUtils;
import com.ironmeta.tahiti.TahitiCoreServiceUserUtils;

import ai.datatower.analytics.DTAnalytics;

public class VlogManager {
    private static VlogManager sVlogManager = null;

    public static synchronized VlogManager getInstance(@NonNull Context context) {
        if (sVlogManager == null) {
            sVlogManager = new VlogManager(context.getApplicationContext());
        }
        return sVlogManager;
    }

    private Context mAppContext;
    private FirebaseAnalytics mFirebaseAnalytics;

    private VlogManager(@NonNull Context appContext) {
        mAppContext = appContext;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(appContext);

        mFirebaseAnalytics.setUserId(TahitiCoreServiceUserUtils.getUid(mAppContext));
        mFirebaseAnalytics.setUserProperty(VlogConstants.UserProperty.U_MCC, DeviceUtils.getMcc(mAppContext));
        mFirebaseAnalytics.setUserProperty(VlogConstants.UserProperty.U_MNC, DeviceUtils.getMnc(mAppContext));
        mFirebaseAnalytics.setUserProperty(VlogConstants.UserProperty.U_OS_COUNTRY, DeviceUtils.getOSCountry(mAppContext));
        mFirebaseAnalytics.setUserProperty(VlogConstants.UserProperty.U_OS_LANG, DeviceUtils.getOSLang(mAppContext));

        mFirebaseAnalytics.getAppInstanceId().addOnSuccessListener(s -> DTAnalytics.setFirebaseAppInstanceId(s));
    }

    public void logEvent(@NonNull String event, @Nullable Bundle params) {
        mFirebaseAnalytics.logEvent(event, params == null ? new Bundle() : params);
    }

    public void logEventWithNetState(@NonNull String event, @Nullable Bundle params) {
        ThreadUtils.runOnMainThread(() -> {
            Bundle myParams = params;
            if (myParams == null) {
                myParams = new Bundle();
            }
            myParams.putBoolean(VlogConstants.Param.V_NET_CONNECTED, NetworkManager.getInstance(mAppContext).getConnected());
            mFirebaseAnalytics.logEvent(event, myParams);
        });
    }
}
