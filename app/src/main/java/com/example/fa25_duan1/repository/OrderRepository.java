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
import java.util.List;

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
}