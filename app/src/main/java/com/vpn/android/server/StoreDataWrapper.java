package com.vpn.android.server;

import com.google.gson.annotations.SerializedName;

public class StoreDataWrapper {
    @SerializedName("raw_data")
    private String mRawData;

    @SerializedName("raw_data_iv")
    private String mRawDataIv;

    public StoreDataWrapper(String rawData, String rawDataIv) {
        this.mRawData = rawData;
        this.mRawDataIv = rawDataIv;
    }

    public String getRawData() {
        return mRawData;
    }

    public void setRawData(String rawData) {
        this.mRawData = rawData;
    }

    public String getRawDataIv() {
        return mRawDataIv;
    }

    public void setRawDataIv(String rawDataIv) {
        this.mRawDataIv = rawDataIv;
    }
}
