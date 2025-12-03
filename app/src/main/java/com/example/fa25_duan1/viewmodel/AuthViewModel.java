package com.example.fa25_duan1.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.Auth.AuthResponse;
import com.example.fa25_duan1.model.Auth.RefreshTokenResponse;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.repository.AuthRepository;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository repository;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository(application.getApplicationContext());
    }

    public LiveData<AuthResponse> login(String username, String password) {
        return repository.login(username, password);
    }

    public LiveData<AuthResponse> register(String username, String password, String name, String email) {
        return repository.register(username, password, name, email);
    }

    public LiveData<RefreshTokenResponse> refreshToken(String refreshToken) {
        return repository.refreshToken(refreshToken);
    }

    public LiveData<ApiResponse<User>> getMyInfo() {
        return repository.getMyInfo();
    }

    public LiveData<RefreshTokenResponse> logout(String refreshToken) {
        return repository.logout(refreshToken);
    }

    public LiveData<ApiResponse<User>> updateProfile(RequestBody name, RequestBody email, MultipartBody.Part avatarPart) {
        return repository.updateProfile(name, email, avatarPart);
    }

    public LiveData<ApiResponse<Void>> changePassword(String currentPassword, String newPassword) {
        return repository.changePassword(currentPassword, newPassword);
    }
    // --- 3 HÀM MỚI ---
    public LiveData<String> forgotPassword(String username, String email) {
        return repository.forgotPassword(username, email);
    }
    public LiveData<String> sendVerifyEmail(String userId) {
        return repository.sendVerifyEmail(userId);
    }
    public LiveData<String> verifyEmailOTP(String userId, String otp) {
        return repository.verifyEmailOTP(userId, otp);
    }

    public LiveData<String> resetPassword(String email, String otp, String newPassword) {
        return repository.resetPassword(email, otp, newPassword);
    }

    public LiveData<String> checkOtpForgot(String email, String otp) {
        return repository.checkOtpForgot(email, otp);
    }
}