package com.example.fa25_duan1; // <-- Đổi thành package của bạn

import android.app.Application;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPaySDK;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ZaloPaySDK.init(554, Environment.SANDBOX); 
    }
}