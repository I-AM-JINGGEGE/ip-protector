package com.sdk.ssmod.api.http.ping;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.sdk.ssmod.api.http.beans.FetchResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerPing {
    private static final String TAG = ServerPing.class.getSimpleName();

    private List<FetchResponse.Host> mVpnServerList;
    private String mRegionUUID;

    private FetchResponse.Host mBestServer;

    private IBestServerCallback mBestServerCallback;

    private ExecutorService mThreadPool;
    private CompletionService<PingResult> mPool;

    public ServerPing(@Nullable List<FetchResponse.Host> vpnServerList, @Nullable String regionUUID) {
        mVpnServerList = vpnServerList == null ? new ArrayList<>() : vpnServerList;
        mRegionUUID = regionUUID;
    }

    @WorkerThread
    public FetchResponse.Host startTest(@Nullable IBestServerCallback bestServerCallback) {
        mBestServerCallback = bestServerCallback;
        mBestServer = null;

        stopTest();

        mThreadPool = Executors.newFixedThreadPool(10);
        mPool = new ExecutorCompletionService<>(mThreadPool);
        mBestServer = doTest();

        stopTest();

        return mBestServer;
    }

    @WorkerThread
    public void stopTest() {
        if (mThreadPool == null) {
            return;
        }

        try {
            mThreadPool.shutdownNow();
        } catch (SecurityException e) {
            e.printStackTrace();
        } finally {
            mThreadPool = null;
        }
    }

    @Nullable
    public FetchResponse.Host getBestServer() {
        return mBestServer;
    }

    @Nullable
    public String getRegionUUID() {
        return mRegionUUID;
    }

    private FetchResponse.Host doTest() {
        if (mBestServerCallback != null) {
            mBestServerCallback.onProgress(3);
        }

        FetchResponse.Host bestServer = null;

        HashMap<Integer, List<FetchResponse.Host>> rank2List = genRank2List();
        ArrayList<Integer> sortedKeys = new ArrayList<>(rank2List.keySet());
        Collections.sort(sortedKeys);

        for (int key : sortedKeys) {
            List<FetchResponse.Host> servers = rank2List.get(key);
            // servers random sort
            Collections.shuffle(servers);
            for (FetchResponse.Host server : servers) {
                if (isThreadShutdown()) {
                    break;
                }
                mPool.submit(new GetConnectionResultTask(server));
            }
            bestServer = takeTestingResult(servers.size());
            if (bestServer != null) {
                break;
            }
        }

        return bestServer;
    }

    private HashMap<Integer, List<FetchResponse.Host>> genRank2List() {
        HashMap<Integer, List<FetchResponse.Host>> result = new HashMap<>();

        for (int i = 0; i < mVpnServerList.size(); i++) {
            FetchResponse.Host item = mVpnServerList.get(i);
            int key = item.getRankingFactor();
            List<FetchResponse.Host> value = result.get(key);
            if (value == null) {
                value = new ArrayList();
                result.put(key, value);
            }

            value.add(item);
        }

        return result;
    }

    private FetchResponse.Host takeTestingResult(int max) {
        FetchResponse.Host bestVPNServer = null;

        for (int i = 0; i < max; i++) {
            if (isThreadShutdown()) {
                break;
            }
            try {
                PingResult connectionResult = mPool.take().get();
                Log.v(TAG, "testing result@cost: " + connectionResult.getCost() + ", ip: " + connectionResult.getVPNServer().getHost());

                if (connectionResult.isAvailable()) {
                    bestVPNServer = connectionResult.getVPNServer();
                    break;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } finally {
                if (mBestServerCallback != null) {
                    mBestServerCallback.onProgress(((i + 1) * 20) / (max + 1));
                }
            }
        }
        Log.v(TAG, "testing result @best server: " + (bestVPNServer == null ? "null" : bestVPNServer.getHost()));
        return bestVPNServer;
    }

    private boolean isThreadShutdown() {
        if (Thread.interrupted() || mThreadPool.isShutdown()) {
            return true;
        }
        return false;
    }

    private class GetConnectionResultTask implements Callable<PingResult> {
        private FetchResponse.Host mVPNServer;

        GetConnectionResultTask(FetchResponse.Host host) {
            mVPNServer = host;
        }

        @Override
        public PingResult call() {
            return PingUtils.getConnectionResult(mVPNServer, 3000, 10000);
        }
    }

    interface IBestServerCallback {
        void onProgress(int progress);
    }
}
