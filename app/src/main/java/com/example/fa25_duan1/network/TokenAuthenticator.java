package com.example.fa25_duan1.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.fa25_duan1.BuildConfig;
import com.example.fa25_duan1.model.Auth.RefreshTokenRequest;
import com.example.fa25_duan1.model.Auth.RefreshTokenResponse;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TokenAuthenticator implements Authenticator {

    private final Context context;

    public TokenAuthenticator(Context context) {
        this.context = context;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        // Lấy refreshToken từ SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String refreshToken = prefs.getString("refreshToken", null);
        if (refreshToken == null) {
            return null; // không có refresh token → user phải login lại
        }

        // Gọi API refresh token đồng bộ
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL_ATHOME)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        AuthApi authApi = retrofit.create(AuthApi.class);
        retrofit2.Response<RefreshTokenResponse> refreshResponse =
                authApi.refreshToken(new RefreshTokenRequest(refreshToken)).execute();

        if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
            String newAccessToken = refreshResponse.body().getAccessToken();

            // Lưu access token mới vào SharedPreferences
            prefs.edit().putString("accessToken", newAccessToken).apply();

            // Retry request cũ với token mới
            return response.request().newBuilder()
                    .header("Authorization", "Bearer " + newAccessToken)
                    .build();
        }

        return null; // refresh token lỗi → user phải login lại
    }
}
