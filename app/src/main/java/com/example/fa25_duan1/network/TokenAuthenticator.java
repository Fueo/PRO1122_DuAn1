package com.example.fa25_duan1.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.fa25_duan1.BuildConfig;
import com.example.fa25_duan1.model.Auth.RefreshTokenRequest;
import com.example.fa25_duan1.model.Auth.RefreshTokenResponse;
import android.util.Log;
import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TokenAuthenticator implements Authenticator {

    private final Context context;
    private static final String TAG = "AuthDebug"; // <-- Thẻ TAG để lọc log

    public TokenAuthenticator(Context context) {
        this.context = context;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        // --- LOG 1: Bắt đầu quá trình Authenticator ---
        Log.d(TAG, "Authenticator được kích hoạt! Mã lỗi: " + response.code() + ". Đang cố gắng làm mới token...");

        // Lấy refreshToken từ SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String refreshToken = prefs.getString("refreshToken", null);

        if (refreshToken == null) {
            // --- LOG 2: Không có Refresh Token ---
            Log.e(TAG, "Không tìm thấy Refresh Token. Quay về màn hình đăng nhập.");
            // Thêm logic chuyển hướng về màn hình đăng nhập nếu cần
            return null;
        }

        // Gọi API refresh token đồng bộ
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL_ATSCHOOL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AuthApi authApi = retrofit.create(AuthApi.class);
        retrofit2.Response<RefreshTokenResponse> refreshResponse =
                authApi.refreshToken(new RefreshTokenRequest(refreshToken)).execute();

        if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
            String newAccessToken = refreshResponse.body().getAccessToken();

            // Lưu access token mới vào SharedPreferences
            prefs.edit().putString("accessToken", newAccessToken).apply();

            // --- LOG 3: Làm mới Token Thành công ---
            Log.i(TAG, "Token làm mới thành công! Chuẩn bị thử lại yêu cầu cũ.");

            // Retry request cũ với token mới
            return response.request().newBuilder()
                    .header("Authorization", "Bearer " + newAccessToken)
                    .build();
        } else {
            // --- LOG 4: Làm mới Token Thất bại ---
            Log.e(TAG, "Làm mới Token thất bại! Mã lỗi Refresh API: " + refreshResponse.code());
            Log.e(TAG, "Refresh Token Response Body: " + refreshResponse.errorBody().string());
        }

        return null; // refresh token lỗi → user phải login lại
    }
}
