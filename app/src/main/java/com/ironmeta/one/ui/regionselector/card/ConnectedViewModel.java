package com.ironmeta.one.ui.regionselector.card;

import android.app.Application;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.ironmeta.one.MainApplication;
import com.ironmeta.one.coreservice.CoreServiceManager;
import com.ironmeta.one.server.UpTimeHelper;
import com.ironmeta.tahiti.TahitiCoreServiceStateInfoManager;

import java.util.Timer;
import java.util.TimerTask;

public class ConnectedViewModel extends AndroidViewModel {
    public ConnectedViewModel(@NonNull Application application) {
        super(application);
        initUsedUp();
    }

    @Override
    protected void onCleared() {
        unInitUsedUp();
    }

    /**
     * used up begin
     **/
    private MediatorLiveData<Long> mUsedUpRemainSecondsAsLiveData;

    private Timer mDisconnectedTimer;

    private void initUsedUp() {
        mUsedUpRemainSecondsAsLiveData = new MediatorLiveData<>();
        mUsedUpRemainSecondsAsLiveData.addSource(UpTimeHelper.INSTANCE.getUpTimeRemainingTimeAsLiveData(), aLong ->
                mUsedUpRemainSecondsAsLiveData.postValue(aLong)
        );
    }

    private void unInitUsedUp() {
        stopUsedUpTimer();
    }

    private void stopUsedUpTimer() {
        if (mDisconnectedTimer == null) {
            return;
        }
        mDisconnectedTimer.cancel();
        mDisconnectedTimer = null;
    }

    public LiveData<Long> getUsedUpRemainSecondsAsLiveData() {
        return mUsedUpRemainSecondsAsLiveData;
    }
}
