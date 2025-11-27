package com.example.fa25_duan1.network;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.Auth.AuthResponse;
import com.example.fa25_duan1.model.ChangePasswordRequest;
import com.example.fa25_duan1.model.Auth.LoginRequest;
import com.example.fa25_duan1.model.Auth.RefreshTokenRequest;
import com.example.fa25_duan1.model.Auth.RefreshTokenResponse;
import com.example.fa25_duan1.model.Auth.RegisterRequest;
import com.example.fa25_duan1.model.User;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface AuthApi {

    // API đăng nhập
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    // API đăng ký
    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("auth/refresh")
    Call<RefreshTokenResponse> refreshToken(@Body RefreshTokenRequest request);

    @POST("auth/logout")
    Call<RefreshTokenResponse> logout(@Body RefreshTokenRequest request);

    @POST("auth/me")
    Call<ApiResponse<User>> getMyInfo();

    @PUT("auth/update")
    Call<ApiResponse<User>> updateProfile(@Body Map<String, String> body);


    @PUT("auth/change-password")
    Call<ApiResponse<Void>> changePassword(@Body ChangePasswordRequest request);
}