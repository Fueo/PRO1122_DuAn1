package com.example.fa25_duan1.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.model.auth.AuthResponse;
import com.example.fa25_duan1.model.auth.RefreshTokenResponse;
import com.example.fa25_duan1.repository.AuthRepository;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository repository;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository(application.getApplicationContext());
    }

    // =========================================================================
    // 1. ĐĂNG NHẬP / ĐĂNG KÝ
    // =========================================================================

    public LiveData<ApiResponse<AuthResponse>> login(String username, String password) {
        return repository.login(username, password);
    }

    public LiveData<ApiResponse<AuthResponse>> loginGoogle(String idToken) {
        return repository.loginGoogle(idToken);
    }

    public LiveData<ApiResponse<AuthResponse>> register(String username, String password, String name, String email) {
        return repository.register(username, password, name, email);
    }

    // =========================================================================
    // 2. TOKEN & LOGOUT
    // =========================================================================

    public LiveData<ApiResponse<RefreshTokenResponse>> refreshToken(String refreshToken) {
        return repository.refreshToken(refreshToken);
    }

    // Lưu ý: Cần đảm bảo bên Repository hàm logout cũng trả về ApiResponse
    public LiveData<ApiResponse<RefreshTokenResponse>> logout(String refreshToken) {
        return repository.logout(refreshToken);
    }

    // =========================================================================
    // 3. THÔNG TIN USER & CẬP NHẬT
    // =========================================================================

    public LiveData<ApiResponse<User>> getMyInfo() {
        return repository.getMyInfo();
    }

    public LiveData<ApiResponse<User>> updateProfile(RequestBody name, RequestBody email, MultipartBody.Part avatarPart) {
        return repository.updateProfile(name, email, avatarPart);
    }

    public LiveData<ApiResponse<Void>> changePassword(String currentPassword, String newPassword) {
        return repository.changePassword(currentPassword, newPassword);
    }

    // =========================================================================
    // 4. QUÊN MẬT KHẨU & OTP & VERIFY
    // (Kiểu trả về đổi từ String -> ApiResponse<Object>)
    // =========================================================================

    public LiveData<ApiResponse<Object>> forgotPassword(String username, String email) {
        return repository.forgotPassword(username, email);
    }

    public LiveData<ApiResponse<Object>> checkOtpForgot(String email, String otp) {
        return repository.checkOtpForgot(email, otp);
    }

    public LiveData<ApiResponse<Object>> resetPassword(String email, String otp, String newPassword) {
        return repository.resetPassword(email, otp, newPassword);
    }

    public LiveData<ApiResponse<Object>> sendVerifyEmail(String userId) {
        return repository.sendVerifyEmail(userId);
    }

    public LiveData<ApiResponse<User>> verifyEmailOTP(String userId, String otp) {
        return repository.verifyEmailOTP(userId, otp);
    }

    public LiveData<ApiResponse<Object>> sendUpdateProfileOtp() {
        return repository.sendUpdateProfileOtp();
    }

    public LiveData<ApiResponse<Object>> checkOtpValid(String otp) {
        return repository.checkOtpValid(otp);
    }
}