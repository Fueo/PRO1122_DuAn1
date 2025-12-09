package com.example.fa25_duan1.repository;

import android.content.Context;

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

    // --- 5. Update Order Status (ADMIN - Cập nhật đầy đủ) ---
    // status: Trạng thái đơn, paymentMethod: COD/QR..., isPaid: Đã thanh toán hay chưa
    public LiveData<ApiResponse<Order>> updateOrderStatus(String orderId, String status, String paymentMethod, Boolean isPaid) {
        MutableLiveData<ApiResponse<Order>> result = new MutableLiveData<>();

        Map<String, Object> body = new HashMap<>();
        if (status != null) body.put("status", status);
        if (paymentMethod != null) body.put("paymentMethod", paymentMethod);
        if (isPaid != null) body.put("isPaid", isPaid);

        orderApi.updateOrderStatus(orderId, body).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Order>> call, @NonNull Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
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

    // --- 6. Get Order By ID ---
    public LiveData<ApiResponse<Order>> getOrderById(String orderId) {
        MutableLiveData<ApiResponse<Order>> result = new MutableLiveData<>();
        orderApi.getOrderById(orderId).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Order>> call, @NonNull Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
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

    // --- 7 & 8 Stats ---
    public LiveData<ApiResponse<Map<String, Integer>>> getStatusCount() {
        MutableLiveData<ApiResponse<Map<String, Integer>>> result = new MutableLiveData<>();
        orderApi.getStatusCount().enqueue(new Callback<ApiResponse<Map<String, Integer>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Integer>>> call, @NonNull Response<ApiResponse<Map<String, Integer>>> response) {
                if (response.isSuccessful() && response.body() != null) result.setValue(response.body());
                else result.setValue(null);
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Integer>>> call, @NonNull Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    public LiveData<ApiResponse<Integer>> getTotalOrders() {
        MutableLiveData<ApiResponse<Integer>> result = new MutableLiveData<>();
        orderApi.getTotalOrders().enqueue(new Callback<ApiResponse<Integer>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Integer>> call, @NonNull Response<ApiResponse<Integer>> response) {
                if (response.isSuccessful() && response.body() != null) result.setValue(response.body());
                else result.setValue(null);
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Integer>> call, @NonNull Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    // --- [MỚI 9] Update Payment Method (USER) ---
    public LiveData<ApiResponse<Order>> updatePaymentMethod(String orderId, String newPaymentMethod) {
        MutableLiveData<ApiResponse<Order>> result = new MutableLiveData<>();

        Map<String, String> body = new HashMap<>();
        body.put("paymentMethod", newPaymentMethod);

        orderApi.updatePaymentMethod(orderId, body).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Order>> call, @NonNull Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    // Parse lỗi logic từ backend (VD: Không phải Pending, sai method...)
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

    // ================= HELPER PARSE ERROR =================
    private ApiResponse<CheckoutResponse> parseError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Type type = new TypeToken<ApiResponse<CheckoutResponse>>(){}.getType();
                return new Gson().fromJson(errorBody, type);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new ApiResponse<>(false, "Lỗi: " + response.message(), null);
    }

    private ApiResponse<Void> parseErrorVoid(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Type type = new TypeToken<ApiResponse<Void>>(){}.getType();
                return new Gson().fromJson(errorBody, type);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new ApiResponse<>(false, "Lỗi: " + response.message(), null);
    }

    private ApiResponse<Order> parseErrorOrder(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Type type = new TypeToken<ApiResponse<Order>>(){}.getType();
                return new Gson().fromJson(errorBody, type);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new ApiResponse<>(false, "Lỗi server: " + response.code(), null);
    }
}