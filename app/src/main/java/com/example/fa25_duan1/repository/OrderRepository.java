package com.example.fa25_duan1.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.CheckoutRequest;
import com.example.fa25_duan1.model.CheckoutResponse;
import com.example.fa25_duan1.model.Order;
import com.example.fa25_duan1.network.OrderApi;
import com.example.fa25_duan1.network.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderRepository {
    private final OrderApi orderApi;

    public OrderRepository(Context context) {
        this.orderApi = RetrofitClient.getInstance(context).getOrderApi();
    }

    // --- 1. Checkout ---
    public LiveData<ApiResponse<CheckoutResponse>> checkout(CheckoutRequest request) {
        MutableLiveData<ApiResponse<CheckoutResponse>> result = new MutableLiveData<>();
        orderApi.checkout(request).enqueue(new Callback<ApiResponse<CheckoutResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CheckoutResponse>> call, @NonNull Response<ApiResponse<CheckoutResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    // Parse lỗi từ server
                    result.setValue(parseError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CheckoutResponse>> call, @NonNull Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    // --- 2. Get Order History ---
    public LiveData<List<Order>> getOrderHistory() {
        MutableLiveData<List<Order>> data = new MutableLiveData<>();
        orderApi.getOrderHistory().enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Order>>> call, @NonNull Response<ApiResponse<List<Order>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    data.setValue(response.body().getData() != null ? response.body().getData() : new ArrayList<>());
                } else {
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Order>>> call, @NonNull Throwable t) {
                data.setValue(null);
            }
        });
        return data;
    }

    // --- 3. Cancel Order ---
    public LiveData<ApiResponse<Void>> cancelOrder(String orderId) {
        MutableLiveData<ApiResponse<Void>> result = new MutableLiveData<>();
        orderApi.cancelOrder(orderId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    result.setValue(parseErrorVoid(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    // --- 4. Get All Orders (Admin) ---
    public LiveData<List<Order>> getAllOrders() {
        MutableLiveData<List<Order>> data = new MutableLiveData<>();
        orderApi.getAllOrders().enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Order>>> call, @NonNull Response<ApiResponse<List<Order>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    data.setValue(response.body().getData() != null ? response.body().getData() : new ArrayList<>());
                } else {
                    data.setValue(null);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Order>>> call, @NonNull Throwable t) {
                data.setValue(null);
            }
        });
        return data;
    }

    // --- 5. Update Order Status (CẬP NHẬT ĐỂ LẤY LỖI) ---
    public LiveData<ApiResponse<Order>> updateOrderStatus(String orderId, String newStatus) {
        MutableLiveData<ApiResponse<Order>> result = new MutableLiveData<>();

        Map<String, String> body = new HashMap<>();
        body.put("status", newStatus);

        orderApi.updateOrderStatus(orderId, body).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Order>> call, @NonNull Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    // [QUAN TRỌNG] Thay vì trả về null, hãy parse lỗi từ server để lấy message
                    result.setValue(parseErrorOrder(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Order>> call, @NonNull Throwable t) {
                // Lỗi mạng thật sự thì mới trả về null (hoặc tạo object lỗi mạng)
                result.setValue(null);
            }
        });
        return result;
    }

    // --- 6. Get Order By ID ---
    public LiveData<ApiResponse<Order>> getOrderById(String orderId) {
        MutableLiveData<ApiResponse<Order>> result = new MutableLiveData<>();

        orderApi.getOrderById(orderId).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Order>> call, @NonNull Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    // Parse lỗi nếu cần
                    result.setValue(parseErrorOrder(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Order>> call, @NonNull Throwable t) {
                result.setValue(null);
            }
        });

        return result;
    }

    public LiveData<ApiResponse<Map<String, Integer>>> getStatusCount() {
        MutableLiveData<ApiResponse<Map<String, Integer>>> result = new MutableLiveData<>();

        orderApi.getStatusCount().enqueue(new Callback<ApiResponse<Map<String, Integer>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Integer>>> call, @NonNull Response<ApiResponse<Map<String, Integer>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    result.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Integer>>> call, @NonNull Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    // --- [MỚI] 8. Get Total Orders ---
    public LiveData<ApiResponse<Integer>> getTotalOrders() {
        MutableLiveData<ApiResponse<Integer>> result = new MutableLiveData<>();

        orderApi.getTotalOrders().enqueue(new Callback<ApiResponse<Integer>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Integer>> call, @NonNull Response<ApiResponse<Integer>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    result.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Integer>> call, @NonNull Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    // ================= HELPER PARSE ERROR =================

    // Helper 1: Parse lỗi cho CheckoutResponse
    private ApiResponse<CheckoutResponse> parseError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Type type = new TypeToken<ApiResponse<CheckoutResponse>>(){}.getType();
                return new Gson().fromJson(errorBody, type);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new ApiResponse<>(false, "Lỗi không xác định: " + response.message(), null);
    }

    // Helper 2: Parse lỗi cho Void
    private ApiResponse<Void> parseErrorVoid(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Type type = new TypeToken<ApiResponse<Void>>(){}.getType();
                return new Gson().fromJson(errorBody, type);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new ApiResponse<>(false, "Lỗi không xác định: " + response.message(), null);
    }

    // [MỚI] Helper 3: Parse lỗi cho Order (Dùng cho UpdateStatus và GetById)
    private ApiResponse<Order> parseErrorOrder(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                // Đọc chuỗi JSON lỗi từ Backend (VD: { "status": false, "message": "Không đủ tồn kho" })
                String errorBody = response.errorBody().string();
                Type type = new TypeToken<ApiResponse<Order>>(){}.getType();
                return new Gson().fromJson(errorBody, type);
            }
        } catch (Exception e) { e.printStackTrace(); }

        // Trả về object lỗi mặc định để UI không bị NullPointerException
        return new ApiResponse<>(false, "Lỗi server: " + response.code(), null);
    }


}