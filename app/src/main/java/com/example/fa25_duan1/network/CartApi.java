package com.example.fa25_duan1.network;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.CartItem;
import com.example.fa25_duan1.model.CartRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CartApi {
    @GET("cart")
    Call<ApiResponse<List<CartItem>>> getCart();

    @POST("cart/add")
    Call<ApiResponse<CartItem>> addToCart(@Body CartRequest request);

    // API MỚI: Giảm số lượng
    @POST("cart/decrease")
    Call<ApiResponse<CartItem>> decreaseQuantity(@Body CartRequest request);

    @DELETE("cart/delete/{id}")
    Call<ApiResponse<Void>> deleteCartItem(@Path("id") String id);

    @GET("cart/check")
    Call<ApiResponse<Boolean>> checkCartAvailability();
}