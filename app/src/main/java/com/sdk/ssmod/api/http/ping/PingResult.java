package com.sdk.ssmod.api.http.ping;

import com.sdk.ssmod.api.http.beans.FetchResponse;

public class PingResult {
    private FetchResponse.Host mVPNServer = null;
    private long mCost = 0L;

    private boolean mResult = false;

    public boolean isResult() {
        return mResult;
    }

    public void setResult(boolean mResult) {
        this.mResult = mResult;
    }

    public FetchResponse.Host getVPNServer() {
        return mVPNServer;
    }

    public void setVPNServer(FetchResponse.Host vpnServer) {
        mVPNServer = vpnServer;
    }

    public long getCost() {
        return mCost;
    }

    public void setCost(long cost) {
        mCost = cost;
    }

    public boolean isAvailable() {
        if (mVPNServer != null
                && mResult) {
            return true;
        }

        return false;
    }
}
