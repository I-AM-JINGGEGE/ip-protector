package com.ironmeta.one.base.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.ironmeta.one.base.utils.LogUtils;
import com.ironmeta.one.base.utils.ThreadUtils;

public class NetworkManager {
    private static final String TAG = NetworkManager.class.getSimpleName();

    private static NetworkManager sNetworkManager = null;

    @MainThread
    public static synchronized NetworkManager getInstance(@NonNull Context context) {
        if (sNetworkManager == null) {
            sNetworkManager = new NetworkManager(context.getApplicationContext());
        }
        return sNetworkManager;
    }

    private boolean mConnected;
    private MutableLiveData<Boolean> mConnectedAsMutableLiveData = new MutableLiveData<>();

    private NetworkManager(@NonNull Context appContext) {
        updateConnectivity(appContext);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            ConnectivityManager connMgr = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connMgr != null) {
                try {
                    connMgr.registerNetworkCallback(new NetworkRequest.Builder().build(), new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(Network network) {
                            ThreadUtils.runOnMainThread(() -> updateConnectivity(appContext));
                        }

                        @Override
                        public void onLost(Network network) {
                            ThreadUtils.runOnMainThread(() -> updateConnectivity(appContext));
                        }
                    });
                } catch (SecurityException e) {
                    LogUtils.logException(e);
                    // google's bug, just catch it. See: https://issuetracker.google.com/issues/175055271
                } catch (Exception e) {
                    LogUtils.logException(e);
                }
                return;
            }
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        appContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || intent.getAction() == null) {
                    return;
                }
                if (!TextUtils.equals(intent.getAction().toLowerCase(), "android.net.conn.CONNECTIVITY_CHANGE".toLowerCase())) {
                    return;
                }
                updateConnectivity(context);
            }
        }, filter);
    }

    @UiThread
    private void updateConnectivity(@NonNull Context context) {
        boolean isConnected = isConnected(context);
        mConnected = isConnected;
        mConnectedAsMutableLiveData.setValue(isConnected(context));
    }

    private static boolean isConnected(@NonNull Context context){
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr == null) {
            return false;
        }
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        }
        return networkInfo.isConnected();
    }

    public boolean getConnected() {
        return mConnected;
    }

    @NonNull
    public LiveData<Boolean> getConnectedAsLiveData() {
        return Transformations.distinctUntilChanged(mConnectedAsMutableLiveData);
    }
}
