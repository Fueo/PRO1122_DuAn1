package com.example.fa25_duan1.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fa25_duan1.model.ApiResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseRepository {

    protected final Gson gson = new Gson();

    /**
     * Hàm dùng chung để gọi API.
     * Tự động xử lý: Success, Error Body (4xx, 5xx), và Failure (Mất mạng).
     */
    protected <T> LiveData<ApiResponse<T>> performRequest(Call<ApiResponse<T>> call) {
        MutableLiveData<ApiResponse<T>> result = new MutableLiveData<>();

        call.enqueue(new Callback<ApiResponse<T>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<T>> call, @NonNull Response<ApiResponse<T>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Thành công (200 OK)
                    result.setValue(response.body());
                } else {
                    // Lỗi từ Server (400, 401, 404, 500...) -> Parse JSON lỗi
                    Type type = new TypeToken<ApiResponse<T>>() {}.getType();
                    result.setValue(parseError(response, type));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<T>> call, @NonNull Throwable t) {
                // Lỗi Mạng / Server Sập -> Tạo message tiếng Việt dễ hiểu
                String friendlyMsg = getNetworkErrorMessage(t);
                result.setValue(new ApiResponse<>(false, friendlyMsg, null));
            }
        });

        return result;
    }

    // Helper: Parse lỗi JSON từ server
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

    // Helper: Dịch lỗi mạng sang tiếng Việt
    private String getNetworkErrorMessage(Throwable t) {
        if (t instanceof ConnectException) {
            return "Không thể kết nối Server. Máy chủ có thể đang bảo trì.";
        } else if (t instanceof SocketTimeoutException) {
            return "Kết nối quá hạn. Mạng chậm hoặc Server phản hồi lâu.";
        } else if (t instanceof UnknownHostException) {
            return "Không có internet. Vui lòng kiểm tra Wifi/4G.";
        }
        return "Lỗi kết nối: " + t.getMessage();
    }
}