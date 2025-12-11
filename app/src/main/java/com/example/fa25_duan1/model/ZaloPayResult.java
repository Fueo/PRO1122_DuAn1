package com.example.fa25_duan1.model;

import com.google.gson.annotations.SerializedName;

public class ZaloPayResult {
    @SerializedName("zp_trans_token")
    private String zpTransToken;

    @SerializedName("app_trans_id")
    private String appTransId;

    public String getZpTransToken() {
        return zpTransToken;
    }

    public String getAppTransId() {
        return appTransId;
    }
}