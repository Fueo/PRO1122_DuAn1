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
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    // Nếu server trả về lỗi 400/500 nhưng có body JSON (message lỗi)
                    // Ta vẫn cố gắng trả về body để ViewModel lấy message
                    // Tuy nhiên Retrofit đưa vào errorBody, nên ở đây ta trả về null hoặc custom logic.
                    // Để đơn giản theo pattern Category, ta trả về null nếu không success.
                    // NHƯNG với Cart, cần message, nên ta sẽ xử lý ở ViewModel nếu result là null.
                    result.setValue(null);

                    // Mở rộng: Nếu muốn lấy message lỗi từ errorBody ở đây thì phức tạp hơn một chút,
                    // Nên giữ logic đơn giản giống CategoryRepository là trả về null khi lỗi mạng/server.
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CartItem>> call, @NonNull Throwable t) {
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
}