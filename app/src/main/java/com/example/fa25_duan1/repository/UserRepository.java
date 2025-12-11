package com.example.fa25_duan1.repository;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.network.RetrofitClient;
import com.example.fa25_duan1.network.UserApi;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private final UserApi userApi;
    private final Gson gson = new Gson(); // Để parse lỗi từ errorBody

    public UserRepository(Context context) {
        userApi = RetrofitClient.getInstance(context).getUserApi();
    }

    /**
     * HÀM XỬ LÝ TẬP TRUNG (CORE)
     * ViewModel chỉ cần gọi hàm này, mọi thứ onResponse/onFailure/ErrorParsing
     * đều được xử lý tại đây và bắn ra LiveData kết quả cuối cùng.
     */
    private <T> LiveData<ApiResponse<T>> callApi(Call<ApiResponse<T>> call) {
        MutableLiveData<ApiResponse<T>> liveData = new MutableLiveData<>();

        call.enqueue(new Callback<ApiResponse<T>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<T>> call, @NonNull Response<ApiResponse<T>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 1. Thành công (HTTP 200) -> Trả về body gốc
                    liveData.setValue(response.body());
                } else {
                    // 2. Lỗi từ Server (HTTP 400, 404, 500...)
                    // Retrofit đẩy JSON lỗi vào errorBody(), ta phải parse nó ra
                    try {
                        if (response.errorBody() != null) {
                            ApiResponse<T> errorResponse = gson.fromJson(
                                    response.errorBody().charStream(),
                                    new TypeToken<ApiResponse<T>>() {}.getType()
                            );
                            liveData.setValue(errorResponse);
                        } else {
                            liveData.setValue(new ApiResponse<>(false, "Lỗi không xác định: " + response.code(), null));
                        }
                    } catch (Exception e) {
                        liveData.setValue(new ApiResponse<>(false, "Lỗi phân tích dữ liệu lỗi: " + e.getMessage(), null));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<T>> call, @NonNull Throwable t) {
                // 3. Lỗi mạng, mất kết nối, timeout
                liveData.setValue(new ApiResponse<>(false, "Lỗi kết nối: " + t.getMessage(), null));
            }
        });

        return liveData;
    }

    // --- CÁC HÀM PUBLIC ---
    // Code cực gọn, chỉ việc truyền Call vào hàm callApi

    public LiveData<ApiResponse<List<User>>> getAllUsers() {
        return callApi(userApi.getAllUsers());
    }

    public LiveData<ApiResponse<Integer>> getTotalAccount() {
        return callApi(userApi.getTotalAccount());
    }

    public LiveData<ApiResponse<User>> getUserByID(String id) {
        return callApi(userApi.getUserByID(id));
    }

    public LiveData<ApiResponse<User>> addUserWithAvatar(RequestBody username, RequestBody password, RequestBody name, RequestBody email, RequestBody role, MultipartBody.Part avatar) {
        return callApi(userApi.addUserWithAvatar(username, password, name, email, role, avatar));
    }

    public LiveData<ApiResponse<User>> updateUserWithAvatar(String id, RequestBody username, RequestBody password, RequestBody name, RequestBody email, RequestBody role, MultipartBody.Part avatar) {
        return callApi(userApi.updateUserWithAvatar(id, username, password, name, email, role, avatar));
    }

    public LiveData<ApiResponse<Object>> deleteUser(String id) {
        return callApi(userApi.deleteUser(id));
    }
}