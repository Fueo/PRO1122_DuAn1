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

    // --- 3 HÀM MỚI ---
    public LiveData<String> forgotPassword(String username, String email) {
        MutableLiveData<String> msg = new MutableLiveData<>();
        authApi.forgotPassword(username, email).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    if(response.body().isStatus()) msg.setValue("OK");
                    else msg.setValue(response.body().getMessage());
                } else msg.setValue("Lỗi kết nối.");
            }
            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) { msg.setValue("Lỗi mạng: " + t.getMessage()); }
        });
        return msg;
    }

    public LiveData<String> sendVerifyEmail(String userId) {
        MutableLiveData<String> msg = new MutableLiveData<>();
        authApi.sendVerifyEmail(userId).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    if(response.body().isStatus()) msg.setValue("OK");
                    else msg.setValue(response.body().getMessage());
                } else msg.setValue("Lỗi server.");
            }
            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) { msg.setValue(t.getMessage()); }
        });
        return msg;
    }

    public LiveData<String> verifyEmailOTP(String userId, String otp) {
        MutableLiveData<String> msg = new MutableLiveData<>();
        authApi.verifyEmailOTP(userId, otp).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    if(response.body().isStatus()) msg.setValue("OK");
                    else msg.setValue(response.body().getMessage());
                } else msg.setValue("Mã không đúng.");
            }
            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) { msg.setValue(t.getMessage()); }
        });
        return msg;
    }

    public LiveData<String> resetPassword(String email, String otp, String newPassword) {
        MutableLiveData<String> msg = new MutableLiveData<>();
        authApi.resetPassword(email, otp, newPassword).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    if(response.body().isStatus()) msg.setValue("OK");
                    else msg.setValue(response.body().getMessage());
                } else msg.setValue("Lỗi kết nối.");
            }
            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) { msg.setValue(t.getMessage()); }
        });
        return msg;
    }

    public LiveData<String> checkOtpForgot(String email, String otp) {
        MutableLiveData<String> msg = new MutableLiveData<>();
        authApi.checkOtpForgot(email, otp).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    if(response.body().isStatus()) msg.setValue("OK");
                    else msg.setValue(response.body().getMessage());
                } else {
                    // Sửa lại đoạn này để biết lỗi gì
                    msg.setValue("Lỗi Server: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                msg.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
        return msg;
    }

    public LiveData<String> sendUpdateProfileOtp() {
        MutableLiveData<String> msg = new MutableLiveData<>();
        authApi.sendUpdateProfileOtp().enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isStatus()) msg.setValue("OK");
                    else msg.setValue(response.body().getMessage());
                } else {
                    msg.setValue("Lỗi Server: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                msg.setValue("Lỗi mạng: " + t.getMessage());
            }
        });
        return msg;
    }

    // --- [MỚI] Kiểm tra OTP hợp lệ ---
    public LiveData<String> checkOtpValid(String otp) {
        MutableLiveData<String> msg = new MutableLiveData<>();
        authApi.checkOtpValid(otp).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isStatus()) msg.setValue("OK");
                    else msg.setValue(response.body().getMessage());
                } else {
                    msg.setValue("Lỗi Server: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                msg.setValue("Lỗi mạng: " + t.getMessage());
            }
        });
        return msg;
    }
}