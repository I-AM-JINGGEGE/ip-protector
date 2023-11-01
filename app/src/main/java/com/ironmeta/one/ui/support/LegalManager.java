package com.ironmeta.one.ui.support;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.ironmeta.base.vstore.VstoreManager;
import com.ironmeta.one.BuildConfig;
import com.ironmeta.one.base.utils.BuildConfigUtils;
import com.ironmeta.one.base.utils.DeviceUtils;
import com.ironmeta.one.constants.KvStoreConstants;

import java.util.HashMap;
import java.util.Map;

public class LegalManager {
    private static LegalManager sLegalManager = null;

    private Context mAppContext;

    private boolean mInLegalRegion;
    private MutableLiveData<Boolean> mInLegalRegionAsLiveData = new MutableLiveData<>();

    private LegalManager(@NonNull Context context) {
        mAppContext = context.getApplicationContext();

        mInLegalRegion = getInLegalRegionStore();
        mInLegalRegionAsLiveData.setValue(mInLegalRegion);

        if (!BuildConfig.DEBUG) {
            final String osCountry = DeviceUtils.getOSCountry(mAppContext).toLowerCase();
            int simMcc = 0, netMcc = 0;
            try {
                simMcc = Integer.parseInt(DeviceUtils.getSimMcc(mAppContext));
                netMcc = Integer.parseInt(DeviceUtils.getNetMcc(mAppContext));
            } catch (NumberFormatException ignore) {
            }
            if (TextUtils.equals(osCountry, "cn") ||
                    MCC_OF_GEORESTRICTED_COUNTRIES.containsValue(simMcc) ||
                    MCC_OF_GEORESTRICTED_COUNTRIES.containsValue(netMcc)) {
                logNotInLegalRegion();
            }
        }
    }

    @MainThread
    public static synchronized LegalManager getInstance(@NonNull Context context) {
        if (sLegalManager == null) {
            sLegalManager = new LegalManager(context);
        }

        return sLegalManager;
    }

    public void logNotInLegalRegion() {
        VstoreManager.getInstance(mAppContext).encode(true, KvStoreConstants.KEY_SUPPORT_IN_LEGAL_REGION, false);
        mInLegalRegion = false;
        mInLegalRegionAsLiveData.postValue(mInLegalRegion);
    }

    public LiveData<Boolean> getInLegalRegionAsLiveData() {
        return Transformations.distinctUntilChanged(mInLegalRegionAsLiveData);
    }

    private boolean getInLegalRegionStore() {
        return VstoreManager.getInstance(mAppContext).decode(true, KvStoreConstants.KEY_SUPPORT_IN_LEGAL_REGION, true);
    }

    private static final Map<String, Integer> MCC_OF_GEORESTRICTED_COUNTRIES = new HashMap<>();

    static {
        MCC_OF_GEORESTRICTED_COUNTRIES.put("cn", 460);
        MCC_OF_GEORESTRICTED_COUNTRIES.put("cu", 368);
        MCC_OF_GEORESTRICTED_COUNTRIES.put("ir", 432);
        MCC_OF_GEORESTRICTED_COUNTRIES.put("kp", 467);
        MCC_OF_GEORESTRICTED_COUNTRIES.put("sy", 417);
    }
}
