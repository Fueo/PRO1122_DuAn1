package com.example.fa25_duan1.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.Auth.AuthResponse;
import com.example.fa25_duan1.model.Auth.RefreshTokenResponse;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.repository.AuthRepository;

public class AuthViewModel extends AndroidViewModel {

    private AuthRepository repository;
    private MutableLiveData<ApiResponse<User>> userInfoLiveData = new MutableLiveData<>();

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

    public LiveData<ApiResponse<User>> updateProfile(String name, String email, String phone, String address, String avatar) {
        // Truyền avatar sang repository
        return repository.updateProfile(name, email, phone, address, avatar);
    }


    public LiveData<ApiResponse<Void>> changePassword(String currentPassword, String newPassword) {
        return repository.changePassword(currentPassword, newPassword);
    }
}
