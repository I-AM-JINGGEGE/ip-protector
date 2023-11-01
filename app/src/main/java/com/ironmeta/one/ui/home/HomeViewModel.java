package com.ironmeta.one.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import com.ironmeta.tahiti.TahitiCoreServiceStateInfoManager;
import com.sdk.ssmod.beans.TrafficStats;

public class HomeViewModel extends AndroidViewModel {
    private LiveData<TrafficStats> mTrafficStats;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        mTrafficStats = TahitiCoreServiceStateInfoManager.getInstance(getApplication()).getTrafficStatsAsLiveData();
        initAddTime();
    }

    @NonNull
    public LiveData<TrafficStats> getTrafficStatsAsLiveData() {
        return mTrafficStats;
    }

    /**
     * add time begin
     **/
    private MutableLiveData<Boolean> mShowAddTimeLoadingAsLiveData;

    private void initAddTime() {
        mShowAddTimeLoadingAsLiveData = new MutableLiveData<>(false);
    }

    public LiveData<Boolean> addTimeA(int addedMinutes) {
        MediatorLiveData<Boolean> resultAsLiveData = new MediatorLiveData<>();
        TahitiCoreServiceStateInfoManager.getInstance(getApplication()).addUsedUpMinutes(addedMinutes);
        resultAsLiveData.setValue(true);
        return resultAsLiveData;
    }

    public void startShowLoadingAd() {
        mShowAddTimeLoadingAsLiveData.setValue(true);
    }

    public void stopShowLoadingAd() {
        mShowAddTimeLoadingAsLiveData.setValue(false);
    }

    public LiveData<Boolean> getShowAddTimeLoadingAsLiveData() {
        return mShowAddTimeLoadingAsLiveData;
    }
    /** add time end **/
}