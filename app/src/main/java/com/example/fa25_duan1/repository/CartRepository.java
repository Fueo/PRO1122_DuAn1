package com.example.fa25_duan1.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.CartItem;
import com.example.fa25_duan1.model.CartRequest;
import com.example.fa25_duan1.network.CartApi;
import com.example.fa25_duan1.network.RetrofitClient;
import java.util.List;

public class CartRepository extends BaseRepository {
    private final CartApi cartApi;

    public CartRepository(Context context) {
        this.cartApi = RetrofitClient.getInstance(context).getCartApi();
    }

    public LiveData<ApiResponse<List<CartItem>>> getCart() {
        return performRequest(cartApi.getCart());
    }

    public LiveData<ApiResponse<CartItem>> addToCart(String productId, int quantity) {
        return performRequest(cartApi.addToCart(new CartRequest(productId, quantity)));
    }

    public LiveData<ApiResponse<CartItem>> decreaseQuantity(String productId) {
        return performRequest(cartApi.decreaseQuantity(new CartRequest(productId, 1)));
    }

    public LiveData<ApiResponse<Void>> deleteCartItem(String id) {
        return performRequest(cartApi.deleteCartItem(id));
    }

    public LiveData<ApiResponse<Boolean>> checkCartAvailability() {
        return performRequest(cartApi.checkCartAvailability());
    }

    public LiveData<ApiResponse<Void>> clearCart() {
        return performRequest(cartApi.clearCart());
    }
}