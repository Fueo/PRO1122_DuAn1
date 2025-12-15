package com.example.fa25_duan1.network;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.CheckoutRequest;
import com.example.fa25_duan1.model.CheckoutResponse;
import com.example.fa25_duan1.model.Order;
import com.example.fa25_duan1.model.ZaloPayResult;

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

    // 2. GET: Lịch sử đơn hàng
    @GET("order")
    Call<ApiResponse<List<Order>>> getOrderHistory();

    // 3. PUT: Hủy đơn hàng
    @PUT("order/cancel/{orderId}")
    Call<ApiResponse<Void>> cancelOrder(@Path("orderId") String orderId);

    // 4. Lấy tất cả đơn hàng (Admin)
    @GET("order/all")
    Call<ApiResponse<List<Order>>> getAllOrders();

    // 5. PUT: Cập nhật trạng thái đơn hàng (Admin/Staff)
    // [UPDATE] Sửa Map<String, String> thành Map<String, Object> để gửi được boolean isPaid
    @PUT("order/update-status/{orderId}")
    Call<ApiResponse<Order>> updateOrderStatus(@Path("orderId") String orderId, @Body Map<String, Object> body);

    // 6. GET: Chi tiết đơn hàng
    @GET("order/detail/{orderId}")
    Call<ApiResponse<Order>> getOrderById(@Path("orderId") String orderId);

    // 7. Thống kê trạng thái
    @GET("order/stats/status-count")
    Call<ApiResponse<Map<String, Integer>>> getStatusCount();

    // 8. Tổng đơn hàng
    @GET("order/stats/total-orders")
    Call<ApiResponse<Integer>> getTotalOrders();

    // [MỚI 9] User tự cập nhật Payment Method (Chỉ khi Pending)
    // Body: { "paymentMethod": "QR" }
    @PUT("order/user/update-payment/{orderId}")
    Call<ApiResponse<Order>> updatePaymentMethod(@Path("orderId") String orderId, @Body Map<String, String> body);

    @POST("payment/create-zalopay-order")
    Call<ApiResponse<ZaloPayResult>> createZaloPayOrder(@Body Map<String, String> body);

    @POST("payment/check-status-zalopay")
    Call<ApiResponse<Map<String, Object>>> checkZaloPayStatus(@Body Map<String, String> body);
}