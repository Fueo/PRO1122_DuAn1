package com.example.fa25_duan1.network;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.CheckoutRequest;
import com.example.fa25_duan1.model.CheckoutResponse;
import com.example.fa25_duan1.model.Order;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface OrderApi {
    // 1. POST: Tạo đơn hàng
    @POST("order/checkout")
    Call<ApiResponse<CheckoutResponse>> checkout(@Body CheckoutRequest request);

    // 2. GET: Lịch sử đơn hàng (Backend đã gộp chi tiết vào đây)
    @GET("order")
    Call<ApiResponse<List<Order>>> getOrderHistory();

    // 3. PUT: Hủy đơn hàng
    @PUT("order/cancel/{orderId}")
    Call<ApiResponse<Void>> cancelOrder(@Path("orderId") String orderId);

    // 4. Lấy tất cả đơn hàng (dành cho admin/nhân viên)
    @GET("order/all")
    Call<ApiResponse<List<Order>>> getAllOrders();

    // 5. PUT: Cập nhật trạng thái đơn hàng (Dành cho Admin/Nhân viên)
    // Body gửi lên dạng JSON: { "status": "Đang giao" }
    @PUT("order/update-status/{orderId}")
    Call<ApiResponse<Order>> updateOrderStatus(@Path("orderId") String orderId, @Body Map<String, String> body);

    // 6. GET: Lấy chi tiết đơn hàng theo ID
    // Endpoint này khớp với route backend: router.get("/detail/:orderId", ...)
    @GET("order/detail/{orderId}")
    Call<ApiResponse<Order>> getOrderById(@Path("orderId") String orderId);
}