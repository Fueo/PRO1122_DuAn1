package com.example.fa25_duan1.network;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.auth.AuthResponse;
import com.example.fa25_duan1.model.ChangePasswordRequest;
import com.example.fa25_duan1.model.auth.LoginRequest;
import com.example.fa25_duan1.model.auth.RefreshTokenRequest;
import com.example.fa25_duan1.model.auth.RefreshTokenResponse;
import com.example.fa25_duan1.model.auth.RegisterRequest;
import com.example.fa25_duan1.model.User;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface AuthApi {

    // API đăng nhập thường
    @POST("auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest request);

    // --- [MỚI] API ĐĂNG NHẬP GOOGLE ---
    @FormUrlEncoded
    @POST("auth/login-google")
    Call<ApiResponse<AuthResponse>> loginGoogle(@Field("idToken") String idToken);

    // API đăng ký
    @POST("auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body RegisterRequest request);

    @POST("auth/refresh")
    Call<ApiResponse<RefreshTokenResponse>> refreshToken(@Body RefreshTokenRequest request);

    @POST("auth/logout")
    Call<ApiResponse<RefreshTokenResponse>> logout(@Body RefreshTokenRequest request);

    @GET("auth/me")
    Call<ApiResponse<User>> getMyInfo();

    @Multipart
    @PUT("auth/update")
    Call<ApiResponse<User>> updateProfile(
            @Part("name") RequestBody name,
            @Part("email") RequestBody email,
            @Part MultipartBody.Part avatar
    );

    @PUT("auth/change-password")
    Call<ApiResponse<Void>> changePassword(@Body ChangePasswordRequest request);

    // --- VERIFY EMAIL ---
    @FormUrlEncoded
    @POST("auth/send-verify-email")
    Call<ApiResponse<Object>> sendVerifyEmail(@Field("userId") String userId);

    @FormUrlEncoded
    @POST("auth/verify-email-otp")
    Call<ApiResponse<User>> verifyEmailOTP(@Field("userId") String userId, @Field("otp") String otp);

    // --- FORGOT PASSWORD ---
    @FormUrlEncoded
    @POST("auth/forgot-password")
    Call<ApiResponse<Object>> forgotPassword(
            @Field("username") String username,
            @Field("email") String email
    );

    @FormUrlEncoded
    @POST("auth/check-otp-forgot")
    Call<ApiResponse<Object>> checkOtpForgot(
            @Field("email") String email,
            @Field("otp") String otp
    );

    @FormUrlEncoded
    @POST("auth/reset-password")
    Call<ApiResponse<Object>> resetPassword(
            @Field("email") String email,
            @Field("otp") String otp,
            @Field("newPassword") String newPassword
    );

    @POST("auth/send-update-profile-otp")
    Call<ApiResponse<Object>> sendUpdateProfileOtp();

    @FormUrlEncoded
    @POST("auth/check-otp-valid")
    Call<ApiResponse<Object>> checkOtpValid(@Field("otp") String otp);
}