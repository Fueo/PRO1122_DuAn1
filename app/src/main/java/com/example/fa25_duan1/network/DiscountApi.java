package com.example.fa25_duan1.network;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.Discount;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface DiscountApi {

    // Lấy danh sách mã giảm giá
    @GET("discount") // Hoặc "discount/" tùy config base url
    Call<ApiResponse<List<Discount>>> getAllDiscounts();

    // Lấy chi tiết 1 mã
    @GET("discount/{id}")
    Call<ApiResponse<Discount>> getDiscountById(@Path("id") String id);

    // Thêm mới
    @POST("discount/add")
    Call<ApiResponse<Discount>> addDiscount(@Body Discount discount);

    // Cập nhật
    @PUT("discount/update/{id}")
    Call<ApiResponse<Discount>> updateDiscount(@Path("id") String id, @Body Discount discount);

    // Xóa
    @DELETE("discount/delete/{id}")
    Call<ApiResponse<Void>> deleteDiscount(@Path("id") String id);
}