package com.example.fa25_duan1.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.Auth.AuthResponse;
import com.example.fa25_duan1.model.Auth.LoginRequest;
import com.example.fa25_duan1.model.Auth.RefreshTokenRequest;
import com.example.fa25_duan1.model.Auth.RefreshTokenResponse;
import com.example.fa25_duan1.model.Auth.RegisterRequest;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.network.AuthApi;
import com.example.fa25_duan1.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private AuthApi authApi;

    public AuthRepository(Context context) {
        authApi = RetrofitClient.getInstance(context).getAuthApi();
    }

    public LiveData<AuthResponse> login(String username, String password) {
        MutableLiveData<AuthResponse> liveData = new MutableLiveData<>();
        authApi.login(new LoginRequest(username, password)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful()) {
                    liveData.setValue(response.body());
                } else {
                    liveData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                liveData.setValue(null);
            }
        });
        return liveData;
    }

    public LiveData<AuthResponse> register(String username, String password, String name, String email) {
        MutableLiveData<AuthResponse> liveData = new MutableLiveData<>();
        authApi.register(new RegisterRequest(username, password, name, email)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful()) {
                    liveData.setValue(response.body());
                } else {
                    liveData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                liveData.setValue(null);
            }
        });
        return liveData;
    }

    public LiveData<RefreshTokenResponse> refreshToken(String refreshToken) {
        MutableLiveData<RefreshTokenResponse> liveData = new MutableLiveData<>();
        authApi.refreshToken(new RefreshTokenRequest(refreshToken)).enqueue(new Callback<RefreshTokenResponse>() {
            @Override
            public void onResponse(Call<RefreshTokenResponse> call, Response<RefreshTokenResponse> response) {
                if (response.isSuccessful()) {
                    liveData.setValue(response.body());
                } else {
                    liveData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<RefreshTokenResponse> call, Throwable t) {
                liveData.setValue(null);
            }
        });
        return liveData;
    }


    public LiveData<ApiResponse<User>> getMyInfo() {
        MutableLiveData<ApiResponse<User>> liveData = new MutableLiveData<>();

        authApi.getMyInfo()
                .enqueue(new Callback<ApiResponse<User>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                        if (response.isSuccessful()) {
                            liveData.setValue(response.body());
                        } else {
                            liveData.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                        liveData.setValue(null);
                    }
                });

        return liveData;
    }

    public LiveData<RefreshTokenResponse> logout(String refreshToken) {
        MutableLiveData<RefreshTokenResponse> liveData = new MutableLiveData<>();
        authApi.logout(new RefreshTokenRequest(refreshToken)).enqueue(new Callback<RefreshTokenResponse>() {
            @Override
            public void onResponse(Call<RefreshTokenResponse> call, Response<RefreshTokenResponse> response) {
                if (response.isSuccessful()) {
                    liveData.setValue(response.body());
                } else {
                    // Xử lý khi đăng xuất không thành công (ví dụ: token không hợp lệ)
                    liveData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<RefreshTokenResponse> call, Throwable t) {
                // Xử lý lỗi mạng, kết nối...
                liveData.setValue(null);
            }
        });
        return liveData;
    }
}
