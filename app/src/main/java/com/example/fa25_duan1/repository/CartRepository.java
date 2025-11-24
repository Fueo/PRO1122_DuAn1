package com.example.fa25_duan1.repository;

import android.content.Context;
import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.CartItem;
import com.example.fa25_duan1.model.CartRequest;
import com.example.fa25_duan1.network.CartApi;
import com.example.fa25_duan1.network.RetrofitClient;

import java.util.List;
import retrofit2.Call;

public class CartRepository {
    private final CartApi cartApi;

    public CartRepository(Context context) {
        this.cartApi = RetrofitClient.getInstance(context).getCartApi();
    }

    public Call<ApiResponse<List<CartItem>>> getCart() {
        return cartApi.getCart();
    }

    public Call<ApiResponse<CartItem>> addToCart(String productId, int quantity) {
        return cartApi.addToCart(new CartRequest(productId, quantity));
    }

    // Hàm giảm số lượng (Gửi 1 để server trừ 1)
    public Call<ApiResponse<CartItem>> decreaseQuantity(String productId) {
        return cartApi.decreaseQuantity(new CartRequest(productId, 1));
    }

    public Call<ApiResponse<Void>> deleteCartItem(String id) {
        return cartApi.deleteCartItem(id);
    }
}