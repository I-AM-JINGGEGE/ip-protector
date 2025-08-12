package com.sdk.ssmod.api.http.ping;

import com.sdk.ssmod.api.http.beans.FetchResponse;

public class PingResult {
    private FetchResponse.Host mVPNServer = null;
    private long mCost = 0L;
    private long mReplyLength = 0L;

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

    public long getReplyLength() {
        return mReplyLength;
    }

    public void setReplyLength(long mReplyLength) {
        this.mReplyLength = mReplyLength;
    }

    public boolean isAvailable() {
        if (mVPNServer != null
//                && mReplyLength > 0L
                && mCost > 0L) {
            return true;
        }

        return false;
    }
}
