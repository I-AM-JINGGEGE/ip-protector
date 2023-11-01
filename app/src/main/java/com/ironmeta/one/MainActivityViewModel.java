package com.ironmeta.one;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ironmeta.one.coreservice.CoreServiceManager;
import com.ironmeta.one.ui.support.LegalManager;
import com.ironmeta.tahiti.TahitiCoreServiceStateInfoManager;
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
