package com.example.fa25_duan1.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.CheckoutRequest;
import com.example.fa25_duan1.model.CheckoutResponse;
import com.example.fa25_duan1.model.Order;
import com.example.fa25_duan1.model.ZaloPayResult;
import com.example.fa25_duan1.network.OrderApi;
import com.example.fa25_duan1.network.RetrofitClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 1. Extend BaseRepository
public class OrderRepository extends BaseRepository {
    private final OrderApi orderApi;

    public OrderRepository(Context context) {
        this.orderApi = RetrofitClient.getInstance(context).getOrderApi();
    }

    // --- 1. Checkout ---
    public LiveData<ApiResponse<CheckoutResponse>> checkout(CheckoutRequest request) {
        return performRequest(orderApi.checkout(request));
    }

    // --- 2. Get Order History ---
    // Make sure OrderApi returns Call<ApiResponse<List<Order>>>
    public LiveData<ApiResponse<List<Order>>> getOrderHistory() {
        return performRequest(orderApi.getOrderHistory());
    }

    // --- 3. Cancel Order ---
    public LiveData<ApiResponse<Void>> cancelOrder(String orderId) {
        return performRequest(orderApi.cancelOrder(orderId));
    }

    // --- 4. Get All Orders (Admin) ---
    public LiveData<ApiResponse<List<Order>>> getAllOrders() {
        return performRequest(orderApi.getAllOrders());
    }

    // --- 5. Update Order Status (ADMIN) ---
    public LiveData<ApiResponse<Order>> updateOrderStatus(String orderId, String status, String paymentMethod, Boolean isPaid) {
        Map<String, Object> body = new HashMap<>();
        if (status != null) body.put("status", status);
        if (paymentMethod != null) body.put("paymentMethod", paymentMethod);
        if (isPaid != null) body.put("isPaid", isPaid);

        return performRequest(orderApi.updateOrderStatus(orderId, body));
    }

    // --- 6. Get Order By ID ---
    public LiveData<ApiResponse<Order>> getOrderById(String orderId) {
        return performRequest(orderApi.getOrderById(orderId));
    }

    // --- 7 & 8 Stats ---
    public LiveData<ApiResponse<Map<String, Integer>>> getStatusCount() {
        return performRequest(orderApi.getStatusCount());
    }

    public LiveData<ApiResponse<Integer>> getTotalOrders() {
        return performRequest(orderApi.getTotalOrders());
    }

    // --- 9. Update Payment Method (USER) ---
    public LiveData<ApiResponse<Order>> updatePaymentMethod(String orderId, String newPaymentMethod) {
        Map<String, String> body = new HashMap<>();
        body.put("paymentMethod", newPaymentMethod);
        return performRequest(orderApi.updatePaymentMethod(orderId, body));
    }

    // --- 10. Create ZaloPay Order ---
    public LiveData<ApiResponse<ZaloPayResult>> createZaloPayOrder(String orderId) {
        Map<String, String> body = new HashMap<>();
        body.put("orderId", orderId);
        return performRequest(orderApi.createZaloPayOrder(body));
    }

    // [MỚI 11] Check Status ZaloPay
    public LiveData<ApiResponse<Map<String, Object>>> checkZaloPayStatus(String appTransId) {
        Map<String, String> body = new HashMap<>();
        body.put("app_trans_id", appTransId);
        return performRequest(orderApi.checkZaloPayStatus(body));
    }
}