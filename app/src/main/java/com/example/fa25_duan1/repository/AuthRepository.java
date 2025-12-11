package com.example.fa25_duan1.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final AuthApi authApi;
    private final Gson gson;

    public AuthRepository(Context context) {
        authApi = RetrofitClient.getInstance(context).getAuthApi();
        gson = new Gson();
    }

    // =========================================================================
    // 1. LOGIN & REGISTER & LOGOUT
    // =========================================================================

    public LiveData<ApiResponse<AuthResponse>> login(String username, String password) {
        MutableLiveData<ApiResponse<AuthResponse>> result = new MutableLiveData<>();
        authApi.login(new LoginRequest(username, password)).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<AuthResponse>> call, @NonNull Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    Type type = new TypeToken<ApiResponse<AuthResponse>>(){}.getType();
                    result.setValue(parseError(response, type));
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<AuthResponse>> call, @NonNull Throwable t) {
                result.setValue(new ApiResponse<>(false, "Lỗi kết nối: " + t.getMessage(), null));
            }
        });
        return result;
    }

    public LiveData<ApiResponse<AuthResponse>> loginGoogle(String idToken) {
        MutableLiveData<ApiResponse<AuthResponse>> result = new MutableLiveData<>();
        authApi.loginGoogle(idToken).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<AuthResponse>> call, @NonNull Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    Type type = new TypeToken<ApiResponse<AuthResponse>>(){}.getType();
                    result.setValue(parseError(response, type));
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<AuthResponse>> call, @NonNull Throwable t) {
                result.setValue(new ApiResponse<>(false, "Lỗi kết nối: " + t.getMessage(), null));
            }
        });
        return result;
    }

    public LiveData<ApiResponse<AuthResponse>> register(String username, String password, String name, String email) {
        MutableLiveData<ApiResponse<AuthResponse>> result = new MutableLiveData<>();
        authApi.register(new RegisterRequest(username, password, name, email)).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<AuthResponse>> call, @NonNull Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    Type type = new TypeToken<ApiResponse<AuthResponse>>(){}.getType();
                    result.setValue(parseError(response, type));
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<AuthResponse>> call, @NonNull Throwable t) {
                result.setValue(new ApiResponse<>(false, "Lỗi mạng: " + t.getMessage(), null));
            }
        });
        return result;
    }

    public LiveData<ApiResponse<RefreshTokenResponse>> logout(String refreshToken) {
        MutableLiveData<ApiResponse<RefreshTokenResponse>> result = new MutableLiveData<>();
        authApi.logout(new RefreshTokenRequest(refreshToken)).enqueue(new Callback<ApiResponse<RefreshTokenResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<RefreshTokenResponse>> call, @NonNull Response<ApiResponse<RefreshTokenResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    Type type = new TypeToken<ApiResponse<RefreshTokenResponse>>(){}.getType();
                    result.setValue(parseError(response, type));
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<RefreshTokenResponse>> call, @NonNull Throwable t) {
                result.setValue(new ApiResponse<>(false, t.getMessage(), null));
            }
        });
        return result;
    }

    // =========================================================================
    // 2. TOKEN & USER INFO
    // =========================================================================

    public LiveData<ApiResponse<RefreshTokenResponse>> refreshToken(String refreshToken) {
        MutableLiveData<ApiResponse<RefreshTokenResponse>> result = new MutableLiveData<>();
        authApi.refreshToken(new RefreshTokenRequest(refreshToken)).enqueue(new Callback<ApiResponse<RefreshTokenResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<RefreshTokenResponse>> call, @NonNull Response<ApiResponse<RefreshTokenResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    Type type = new TypeToken<ApiResponse<RefreshTokenResponse>>(){}.getType();
                    result.setValue(parseError(response, type));
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<RefreshTokenResponse>> call, @NonNull Throwable t) {
                result.setValue(new ApiResponse<>(false, t.getMessage(), null));
            }
        });
        return result;
    }

    public LiveData<ApiResponse<User>> getMyInfo() {
        MutableLiveData<ApiResponse<User>> result = new MutableLiveData<>();
        authApi.getMyInfo().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    Type type = new TypeToken<ApiResponse<User>>(){}.getType();
                    result.setValue(parseError(response, type));
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                result.setValue(new ApiResponse<>(false, t.getMessage(), null));
            }
        });
        return result;
    }

    // =========================================================================
    // 3. PROFILE & PASSWORD
    // =========================================================================

    public LiveData<ApiResponse<User>> updateProfile(RequestBody name, RequestBody email, MultipartBody.Part avatarPart) {
        MutableLiveData<ApiResponse<User>> result = new MutableLiveData<>();
        authApi.updateProfile(name, email, avatarPart).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    Type type = new TypeToken<ApiResponse<User>>(){}.getType();
                    result.setValue(parseError(response, type));
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                result.setValue(new ApiResponse<>(false, t.getMessage(), null));
            }
        });
        return result;
    }

    public LiveData<ApiResponse<Void>> changePassword(String currentPassword, String newPassword) {
        MutableLiveData<ApiResponse<Void>> result = new MutableLiveData<>();
        authApi.changePassword(new ChangePasswordRequest(currentPassword, newPassword)).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    Type type = new TypeToken<ApiResponse<Void>>(){}.getType();
                    result.setValue(parseError(response, type));
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                result.setValue(new ApiResponse<>(false, t.getMessage(), null));
            }
        });
        return result;
    }

    // =========================================================================
    // 4. VERIFY EMAIL & OTP (Bổ sung đầy đủ)
    // =========================================================================

    // Gửi email verify
    public LiveData<ApiResponse<Object>> sendVerifyEmail(String userId) {
        return callApiObject(authApi.sendVerifyEmail(userId));
    }

    // Xác nhận OTP email (trả về User)
    public LiveData<ApiResponse<User>> verifyEmailOTP(String userId, String otp) {
        MutableLiveData<ApiResponse<User>> result = new MutableLiveData<>();
        authApi.verifyEmailOTP(userId, otp).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    Type type = new TypeToken<ApiResponse<User>>(){}.getType();
                    result.setValue(parseError(response, type));
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                result.setValue(new ApiResponse<>(false, t.getMessage(), null));
            }
        });
        return result;
    }

    // =========================================================================
    // 5. FORGOT PASSWORD & OTP ACTIONS
    // =========================================================================

    public LiveData<ApiResponse<Object>> forgotPassword(String username, String email) {
        return callApiObject(authApi.forgotPassword(username, email));
    }

    public LiveData<ApiResponse<Object>> checkOtpForgot(String email, String otp) {
        return callApiObject(authApi.checkOtpForgot(email, otp));
    }

    public LiveData<ApiResponse<Object>> resetPassword(String email, String otp, String newPassword) {
        return callApiObject(authApi.resetPassword(email, otp, newPassword));
    }

    public LiveData<ApiResponse<Object>> sendUpdateProfileOtp() {
        return callApiObject(authApi.sendUpdateProfileOtp());
    }

    public LiveData<ApiResponse<Object>> checkOtpValid(String otp) {
        return callApiObject(authApi.checkOtpValid(otp));
    }

    // =========================================================================
    // HELPER FUNCTIONS (GENERIC & REUSABLE)
    // =========================================================================

    // Hàm dùng chung cho các API trả về ApiResponse<Object> để tránh viết lặp lại code
    private LiveData<ApiResponse<Object>> callApiObject(Call<ApiResponse<Object>> call) {
        MutableLiveData<ApiResponse<Object>> result = new MutableLiveData<>();
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    Type type = new TypeToken<ApiResponse<Object>>(){}.getType();
                    result.setValue(parseError(response, type));
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                result.setValue(new ApiResponse<>(false, t.getMessage(), null));
            }
        });
        return result;
    }

    // Hàm parseError Generic: Tự động parse JSON lỗi từ backend cho mọi loại data T
    private <T> ApiResponse<T> parseError(Response<?> response, Type type) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                return gson.fromJson(errorBody, type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ApiResponse<>(false, "Lỗi Server: " + response.code(), null);
    }
}