package com.example.fa25_duan1.network;

import android.content.Context;
import android.content.SharedPreferences;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class AuthInterceptor implements Interceptor {

    private final SharedPreferences sharedPreferences;

    public AuthInterceptor(Context context) {
        sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String token = sharedPreferences.getString("accessToken", null);

        Request original = chain.request();
        Request.Builder requestBuilder = original.newBuilder();

        if (token != null) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        Request request = requestBuilder.build();
        return chain.proceed(request);
    }
}
