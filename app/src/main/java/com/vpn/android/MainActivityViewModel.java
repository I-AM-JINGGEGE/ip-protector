package com.vpn.android;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.vpn.android.coreservice.CoreServiceManager;
import com.vpn.android.ui.support.LegalManager;
import com.vpn.tahiti.TahitiCoreServiceStateInfoManager;
import com.sdk.ssmod.beans.TrafficStats;

public class MainActivityViewModel extends AndroidViewModel {
    private LiveData<TrafficStats> mTrafficStats;
    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        mTrafficStats = TahitiCoreServiceStateInfoManager.getInstance(getApplication()).getTrafficStatsAsLiveData();
    }

    @NonNull
    public LiveData<TrafficStats> getTrafficStatsAsLiveData() {
        return mTrafficStats;
    }

    /** toolbar begin **/

    public LiveData<String> getToolbarRegionIdAsLiveData() {
        return CoreServiceManager.getInstance(MainApplication.Companion.getContext()).getSelectedRegionIdLiveData();
    }
    /** toolbar end **/

    /** support begin **/
    public LiveData<Boolean> getInLegalRegionAsLiveData() {
        return LegalManager.getInstance(getApplication()).getInLegalRegionAsLiveData();
    }
    /** support end **/
}
