package com.example.fa25_duan1.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.ChangePasswordRequest;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.model.auth.AuthResponse;
import com.example.fa25_duan1.model.auth.LoginRequest;
import com.example.fa25_duan1.model.auth.RefreshTokenRequest;
import com.example.fa25_duan1.model.auth.RefreshTokenResponse;
import com.example.fa25_duan1.model.auth.RegisterRequest;
import com.example.fa25_duan1.network.AuthApi;
import com.example.fa25_duan1.network.RetrofitClient;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

// 1. Extend BaseRepository to inherit performRequest and error handling logic
public class AuthRepository extends BaseRepository {

    private final AuthApi authApi;

    public AuthRepository(Context context) {
        authApi = RetrofitClient.getInstance(context).getAuthApi();
        // Gson initialization is now handled in BaseRepository
    }

    // =========================================================================
    // 1. LOGIN & REGISTER & LOGOUT
    // =========================================================================

    public LiveData<ApiResponse<AuthResponse>> login(String username, String password) {
        return performRequest(authApi.login(new LoginRequest(username, password)));
    }

    public LiveData<ApiResponse<AuthResponse>> loginGoogle(String idToken) {
        return performRequest(authApi.loginGoogle(idToken));
    }

    public LiveData<ApiResponse<AuthResponse>> register(String username, String password, String name, String email) {
        return performRequest(authApi.register(new RegisterRequest(username, password, name, email)));
    }

    public LiveData<ApiResponse<RefreshTokenResponse>> logout(String refreshToken) {
        return performRequest(authApi.logout(new RefreshTokenRequest(refreshToken)));
    }

    // =========================================================================
    // 2. TOKEN & USER INFO
    // =========================================================================

    public LiveData<ApiResponse<RefreshTokenResponse>> refreshToken(String refreshToken) {
        return performRequest(authApi.refreshToken(new RefreshTokenRequest(refreshToken)));
    }

    public LiveData<ApiResponse<User>> getMyInfo() {
        return performRequest(authApi.getMyInfo());
    }

    // =========================================================================
    // 3. PROFILE & PASSWORD
    // =========================================================================

    public LiveData<ApiResponse<User>> updateProfile(RequestBody name, RequestBody email, MultipartBody.Part avatarPart) {
        return performRequest(authApi.updateProfile(name, email, avatarPart));
    }

    public LiveData<ApiResponse<Void>> changePassword(String currentPassword, String newPassword) {
        return performRequest(authApi.changePassword(new ChangePasswordRequest(currentPassword, newPassword)));
    }

    // =========================================================================
    // 4. VERIFY EMAIL & OTP
    // =========================================================================

    public LiveData<ApiResponse<Object>> sendVerifyEmail(String userId) {
        return performRequest(authApi.sendVerifyEmail(userId));
    }

    public LiveData<ApiResponse<User>> verifyEmailOTP(String userId, String otp) {
        return performRequest(authApi.verifyEmailOTP(userId, otp));
    }

    // =========================================================================
    // 5. FORGOT PASSWORD & OTP ACTIONS
    // =========================================================================

    public LiveData<ApiResponse<Object>> forgotPassword(String username, String email) {
        return performRequest(authApi.forgotPassword(username, email));
    }

    public LiveData<ApiResponse<Object>> checkOtpForgot(String email, String otp) {
        return performRequest(authApi.checkOtpForgot(email, otp));
    }

    public LiveData<ApiResponse<Object>> resetPassword(String email, String otp, String newPassword) {
        return performRequest(authApi.resetPassword(email, otp, newPassword));
    }

    public LiveData<ApiResponse<Object>> sendUpdateProfileOtp() {
        return performRequest(authApi.sendUpdateProfileOtp());
    }

    public LiveData<ApiResponse<Object>> checkOtpValid(String otp) {
        return performRequest(authApi.checkOtpValid(otp));
    }

    // Removed: callApiObject and parseError because BaseRepository handles them now.
}