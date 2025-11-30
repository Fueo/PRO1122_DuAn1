package com.example.fa25_duan1.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.Auth.AuthResponse;
import com.example.fa25_duan1.model.ChangePasswordRequest;
import com.example.fa25_duan1.model.Auth.LoginRequest;
import com.example.fa25_duan1.model.Auth.RefreshTokenRequest;
import com.example.fa25_duan1.model.Auth.RefreshTokenResponse;
import com.example.fa25_duan1.model.Auth.RegisterRequest;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.network.AuthApi;
import com.example.fa25_duan1.network.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final AuthApi authApi;

    public AuthRepository(Context context) {
        authApi = RetrofitClient.getInstance(context).getAuthApi();
    }

    public LiveData<AuthResponse> login(String username, String password) {
        MutableLiveData<AuthResponse> liveData = new MutableLiveData<>();
        authApi.login(new LoginRequest(username, password)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful()) {
                    liveData.setValue(response.body());
                } else {
                    liveData.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                liveData.setValue(null);
            }
        });
        return liveData;
    }

    public LiveData<AuthResponse> register(String username, String password, String name, String email) {
        MutableLiveData<AuthResponse> liveData = new MutableLiveData<>();
        authApi.register(new RegisterRequest(username, password, name, email)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful()) {
                    liveData.setValue(response.body());
                } else {
                    liveData.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                liveData.setValue(null);
            }
        });
        return liveData;
    }

    public LiveData<RefreshTokenResponse> refreshToken(String refreshToken) {
        MutableLiveData<RefreshTokenResponse> liveData = new MutableLiveData<>();
        authApi.refreshToken(new RefreshTokenRequest(refreshToken)).enqueue(new Callback<RefreshTokenResponse>() {
            @Override
            public void onResponse(@NonNull Call<RefreshTokenResponse> call, @NonNull Response<RefreshTokenResponse> response) {
                if (response.isSuccessful()) {
                    liveData.setValue(response.body());
                } else {
                    liveData.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<RefreshTokenResponse> call, @NonNull Throwable t) {
                liveData.setValue(null);
            }
        });
        return liveData;
    }

    public LiveData<ApiResponse<User>> getMyInfo() {
        MutableLiveData<ApiResponse<User>> liveData = new MutableLiveData<>();

        authApi.getMyInfo().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                if (response.isSuccessful()) {
                    liveData.setValue(response.body());
                } else {
                    liveData.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                liveData.setValue(null);
            }
        });

        return liveData;
    }

    public LiveData<RefreshTokenResponse> logout(String refreshToken) {
        MutableLiveData<RefreshTokenResponse> liveData = new MutableLiveData<>();
        authApi.logout(new RefreshTokenRequest(refreshToken)).enqueue(new Callback<RefreshTokenResponse>() {
            @Override
            public void onResponse(@NonNull Call<RefreshTokenResponse> call, @NonNull Response<RefreshTokenResponse> response) {
                if (response.isSuccessful()) {
                    liveData.setValue(response.body());
                } else {
                    liveData.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<RefreshTokenResponse> call, @NonNull Throwable t) {
                liveData.setValue(null);
            }
        });
        return liveData;
    }

    // --- ĐÃ CẬP NHẬT: Chỉ còn name, email, avatar ---
    public LiveData<ApiResponse<User>> updateProfile(RequestBody name, RequestBody email, MultipartBody.Part avatarPart) {
        MutableLiveData<ApiResponse<User>> liveData = new MutableLiveData<>();

        authApi.updateProfile(name, email, avatarPart).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                if (response.isSuccessful()) {
                    liveData.setValue(response.body());
                } else {
                    liveData.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                liveData.setValue(null);
            }
        });
        return liveData;
    }

    public LiveData<ApiResponse<Void>> changePassword(String currentPassword, String newPassword) {
        MutableLiveData<ApiResponse<Void>> liveData = new MutableLiveData<>();
        ChangePasswordRequest request = new ChangePasswordRequest(currentPassword, newPassword);

        authApi.changePassword(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    liveData.setValue(response.body());
                } else {
                    liveData.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                liveData.setValue(null);
            }
        });
        return liveData;
    }
}