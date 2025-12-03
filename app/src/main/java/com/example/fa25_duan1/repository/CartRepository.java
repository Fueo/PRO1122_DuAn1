package com.example.fa25_duan1.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.CartItem;
import com.example.fa25_duan1.model.CartRequest;
import com.example.fa25_duan1.network.CartApi;
import com.example.fa25_duan1.network.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartRepository {
    private final CartApi cartApi;

    public CartRepository(Context context) {
        this.cartApi = RetrofitClient.getInstance(context).getCartApi();
    }

    // --- Helper: Parse Lỗi Chung ---
    private <T> ApiResponse<T> parseError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                JSONObject jsonObject = new JSONObject(errorJson);
                String msg = jsonObject.optString("message", "Lỗi không xác định");
                return new ApiResponse<>(false, msg, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ApiResponse<>(false, "Lỗi kết nối hoặc server", null);
    }

    // --- 1. Get Cart List ---
    public LiveData<List<CartItem>> getCart() {
        MutableLiveData<List<CartItem>> data = new MutableLiveData<>();
        cartApi.getCart().enqueue(new Callback<ApiResponse<List<CartItem>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<CartItem>>> call, @NonNull Response<ApiResponse<List<CartItem>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    data.setValue(response.body().getData() != null ? response.body().getData() : new ArrayList<>());
                } else {
                    data.setValue(null);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<CartItem>>> call, @NonNull Throwable t) {
                data.setValue(null);
            }
        });
        return data;
    }

    // --- 2. Add to Cart / Increase (Đã sửa để hứng message) ---
    public LiveData<ApiResponse<CartItem>> addToCart(String productId, int quantity) {
        MutableLiveData<ApiResponse<CartItem>> result = new MutableLiveData<>();

        cartApi.addToCart(new CartRequest(productId, quantity)).enqueue(new Callback<ApiResponse<CartItem>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CartItem>> call, @NonNull Response<ApiResponse<CartItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    // Gọi hàm helper để lấy message lỗi từ backend (VD: Hết hàng)
                    result.setValue(parseError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CartItem>> call, @NonNull Throwable t) {
                result.setValue(null);
            }
        });

        return result;
    }

    // --- 3. Decrease Quantity (Đã sửa để hứng message) ---
    public LiveData<ApiResponse<CartItem>> decreaseQuantity(String productId) {
        MutableLiveData<ApiResponse<CartItem>> result = new MutableLiveData<>();
        cartApi.decreaseQuantity(new CartRequest(productId, 1)).enqueue(new Callback<ApiResponse<CartItem>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CartItem>> call, @NonNull Response<ApiResponse<CartItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    result.setValue(parseError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CartItem>> call, @NonNull Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    // --- 4. Delete Cart Item ---
    public LiveData<Boolean> deleteCartItem(String id) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        cartApi.deleteCartItem(id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                result.setValue(response.isSuccessful());
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                result.setValue(false);
            }
        });
        return result;
    }

    // --- 5. Check Cart Availability ---
    public LiveData<Boolean> checkCartAvailability() {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        cartApi.checkCartAvailability().enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Boolean>> call, @NonNull Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Boolean isValid = response.body().getData();
                    result.setValue(isValid != null ? isValid : false);
                } else {
                    result.setValue(false);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Boolean>> call, @NonNull Throwable t) {
                result.setValue(false);
            }
        });
        return result;
    }

    // --- 6. Clear Cart ---
    public LiveData<Boolean> clearCart() {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        cartApi.clearCart().enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    result.setValue(true);
                } else {
                    result.setValue(false);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                result.setValue(false);
            }
        });
        return result;
    }
}