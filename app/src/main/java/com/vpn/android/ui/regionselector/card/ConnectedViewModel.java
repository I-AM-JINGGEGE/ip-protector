package com.vpn.android.ui.regionselector.card;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.vpn.android.server.UpTimeHelper;

import java.util.Timer;

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
