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

    // --- Get Cart List ---
    public LiveData<List<CartItem>> getCart() {
        MutableLiveData<List<CartItem>> data = new MutableLiveData<>();
        cartApi.getCart().enqueue(new Callback<ApiResponse<List<CartItem>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<CartItem>>> call, @NonNull Response<ApiResponse<List<CartItem>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    data.setValue(response.body().getData() != null ? response.body().getData() : new ArrayList<>());
                } else {
                    data.setValue(null); // Trả về null để ViewModel biết là lỗi
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<CartItem>>> call, @NonNull Throwable t) {
                data.setValue(null);
            }
        });
        return data;
    }

    // --- Add to Cart / Increase ---
    // Trả về ApiResponse để lấy được message lỗi (VD: Hết hàng)
    public LiveData<ApiResponse<CartItem>> addToCart(String productId, int quantity) {
        MutableLiveData<ApiResponse<CartItem>> result = new MutableLiveData<>();

        cartApi.addToCart(new CartRequest(productId, quantity)).enqueue(new Callback<ApiResponse<CartItem>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CartItem>> call, @NonNull Response<ApiResponse<CartItem>> response) {
                // TRƯỜNG HỢP THÀNH CÔNG (200 OK)
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                }
                // TRƯỜNG HỢP LỖI (400, 404, 500...)
                else {
                    try {
                        if (response.errorBody() != null) {
                            // 1. Lấy chuỗi JSON lỗi từ server
                            String errorJson = response.errorBody().string();

                            // 2. Parse lấy message
                            org.json.JSONObject jsonObject = new org.json.JSONObject(errorJson);
                            String msg = jsonObject.optString("message", "Có lỗi xảy ra");

                            // 3. Tạo object lỗi giả để trả về UI
                            ApiResponse<CartItem> errorResponse = new ApiResponse<>();
                            errorResponse.setStatus(false);
                            errorResponse.setMessage(msg);
                            errorResponse.setData(null);

                            // --- SỬA LỖI Ở ĐÂY: Dùng 'result' thay vì 'data' ---
                            result.setValue(errorResponse);
                        } else {
                            result.setValue(null);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        // --- SỬA LỖI Ở ĐÂY: Dùng 'result' thay vì 'data' ---
                        result.setValue(null);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CartItem>> call, @NonNull Throwable t) {
                // Lỗi mạng (mất mạng, server down)
                result.setValue(null);
            }
        });

        return result;
    }

    // --- Decrease Quantity ---
    public LiveData<ApiResponse<CartItem>> decreaseQuantity(String productId) {
        MutableLiveData<ApiResponse<CartItem>> result = new MutableLiveData<>();
        cartApi.decreaseQuantity(new CartRequest(productId, 1)).enqueue(new Callback<ApiResponse<CartItem>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CartItem>> call, @NonNull Response<ApiResponse<CartItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    result.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CartItem>> call, @NonNull Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    // --- Delete Cart Item ---
    public LiveData<Boolean> deleteCartItem(String id) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        cartApi.deleteCartItem(id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                // Thành công khi status code 200
                result.setValue(response.isSuccessful());
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                result.setValue(false);
            }
        });
        return result;
    }

    public LiveData<Boolean> checkCartAvailability() {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        cartApi.checkCartAvailability().enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Boolean>> call, @NonNull Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    // Lấy giá trị boolean từ data
                    Boolean isValid = response.body().getData();
                    // Nếu data null thì mặc định false, ngược lại lấy giá trị thật
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

    public LiveData<Boolean> clearCart() {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        cartApi.clearCart().enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                // Kiểm tra thành công (status code 200 và body.status = true)
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    result.setValue(true);
                } else {
                    result.setValue(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                // Lỗi kết nối
                result.setValue(false);
            }
        });

        return result;
    }
}