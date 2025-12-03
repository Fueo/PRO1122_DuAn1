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
                    // Cố gắng parse lỗi từ server (ví dụ: Hết hàng)
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

    // --- 2. Get Order History (Đã bao gồm chi tiết) ---
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

    // Helper: Parse lỗi JSON từ server (cho CheckoutResponse)
    private ApiResponse<CheckoutResponse> parseError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Type type = new TypeToken<ApiResponse<CheckoutResponse>>(){}.getType();
                return new Gson().fromJson(errorBody, type);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // Helper: Parse lỗi JSON từ server (cho Void)
    private ApiResponse<Void> parseErrorVoid(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Type type = new TypeToken<ApiResponse<Void>>(){}.getType();
                return new Gson().fromJson(errorBody, type);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public LiveData<List<Order>> getAllOrders() {
        MutableLiveData<List<Order>> data = new MutableLiveData<>();
        orderApi.getAllOrders().enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Order>>> call, @NonNull Response<ApiResponse<List<Order>>> response) {
                // --- THÊM LOG DEBUG TẠI ĐÂY ---
                Log.d("DEBUG_REPO", "Code: " + response.code());
                if (response.body() != null) {
                    Log.d("DEBUG_REPO", "Data size: " + (response.body().getData() != null ? response.body().getData().size() : "null"));
                } else {
                    Log.e("DEBUG_REPO", "Body null. ErrorBody: " + response.errorBody());
                }
                // -----------------------------

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    data.setValue(response.body().getData() != null ? response.body().getData() : new ArrayList<>());
                } else {
                    data.setValue(null);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Order>>> call, @NonNull Throwable t) {
                Log.e("DEBUG_REPO", "Failure: " + t.getMessage());
                data.setValue(null);
            }
        });
        return data;
    }

    // --- [MỚI] 5. Update Order Status ---
    public LiveData<ApiResponse<Order>> updateOrderStatus(String orderId, String newStatus) {
        MutableLiveData<ApiResponse<Order>> result = new MutableLiveData<>();

        // Tạo body JSON { "status": "..." }
        Map<String, String> body = new HashMap<>();
        body.put("status", newStatus);

        orderApi.updateOrderStatus(orderId, body).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Order>> call, @NonNull Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    // Parse lỗi trả về null hoặc parseError như hàm checkout
                    result.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Order>> call, @NonNull Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    public LiveData<ApiResponse<Order>> getOrderById(String orderId) {
        MutableLiveData<ApiResponse<Order>> result = new MutableLiveData<>();

        orderApi.getOrderById(orderId).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Order>> call, @NonNull Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Trả về dữ liệu thành công
                    result.setValue(response.body());
                } else {
                    // Xử lý lỗi (trả về null hoặc object lỗi)
                    result.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Order>> call, @NonNull Throwable t) {
                // Lỗi kết nối
                result.setValue(null);
            }
        });

        return result;
    }
}