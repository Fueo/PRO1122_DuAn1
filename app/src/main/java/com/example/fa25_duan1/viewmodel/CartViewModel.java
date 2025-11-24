package com.example.fa25_duan1.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.CartItem;
import com.example.fa25_duan1.repository.CartRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartViewModel extends AndroidViewModel {

    private final CartRepository repository;
    private final MutableLiveData<List<CartItem>> cartItems = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Long> totalPrice = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalQuantity = new MutableLiveData<>(0);

    public CartViewModel(@NonNull Application application) {
        super(application);
        repository = new CartRepository(application);
    }

    public LiveData<List<CartItem>> getCartItems() { return cartItems; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Long> getTotalPrice() { return totalPrice; }
    public LiveData<Integer> getTotalQuantity() { return totalQuantity; }

    public void fetchCart() {
        repository.getCart().enqueue(new Callback<ApiResponse<List<CartItem>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CartItem>>> call, Response<ApiResponse<List<CartItem>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<CartItem> items = response.body().getData();
                    cartItems.setValue(items);
                    calculateTotal(items);
                } else {
                    cartItems.setValue(null);
                    totalPrice.setValue(0L);
                    totalQuantity.setValue(0);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<CartItem>>> call, Throwable t) {
                // message.setValue("Lỗi kết nối: " + t.getMessage()); // Có thể comment lại nếu không muốn hiện lỗi mạng liên tục
            }
        });
    }

    // Tăng số lượng / Thêm vào giỏ
    public void increaseQuantity(String productId) {
        repository.addToCart(productId, 1).enqueue(new Callback<ApiResponse<CartItem>>() {
            @Override
            public void onResponse(Call<ApiResponse<CartItem>> call, Response<ApiResponse<CartItem>> response) {
                if (response.isSuccessful() && response.body().isStatus()) {
                    fetchCart();
                    // message.setValue("Thêm vào giỏ thành công"); // Tùy chọn: Bật nếu muốn báo thành công mỗi lần bấm
                } else {
                    // Xử lý lỗi từ Server (400 Bad Request - Hết hàng)
                    try {
                        if (response.errorBody() != null) {
                            // Parse JSON lỗi trả về từ Server
                            String errorBody = response.errorBody().string();
                            // Dùng TypeToken để Gson hiểu rõ kiểu dữ liệu
                            Type type = new TypeToken<ApiResponse<Object>>(){}.getType();
                            ApiResponse<Object> errorResponse = new Gson().fromJson(errorBody, type);

                            if (errorResponse != null && errorResponse.getMessage() != null) {
                                message.setValue(errorResponse.getMessage());
                            } else {
                                message.setValue("Không thể thêm sản phẩm (Lỗi không xác định)");
                            }
                        } else {
                            message.setValue("Lỗi: " + response.message());
                        }
                    } catch (Exception e) {
                        message.setValue("Hết hàng hoặc lỗi hệ thống");
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<CartItem>> call, Throwable t) {
                message.setValue("Lỗi kết nối mạng");
            }
        });
    }

    // Giảm số lượng
    public void decreaseQuantity(String productId) {
        repository.decreaseQuantity(productId).enqueue(new Callback<ApiResponse<CartItem>>() {
            @Override
            public void onResponse(Call<ApiResponse<CartItem>> call, Response<ApiResponse<CartItem>> response) {
                if (response.isSuccessful() && response.body().isStatus()) {
                    fetchCart();
                } else {
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Type type = new TypeToken<ApiResponse<Object>>(){}.getType();
                            ApiResponse<Object> errorResponse = new Gson().fromJson(errorBody, type);
                            message.setValue(errorResponse.getMessage());
                        } catch (Exception e) {
                            message.setValue("Không thể giảm");
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<CartItem>> call, Throwable t) {
                message.setValue("Lỗi kết nối");
            }
        });
    }

    public void deleteItem(String cartItemId) {
        repository.deleteCartItem(cartItemId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    fetchCart();
                    message.setValue("Đã xóa sản phẩm");
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                message.setValue("Lỗi mạng khi xóa");
            }
        });
    }

    private void calculateTotal(List<CartItem> items) {
        long total = 0;
        int count = 0;
        if (items != null) {
            for (CartItem item : items) {
                if (item.getProduct() != null) {
                    total += (long) (item.getProduct().getPrice() * item.getQuantity());
                    count += item.getQuantity();
                }
            }
        }
        totalPrice.setValue(total);
        totalQuantity.setValue(count);
    }
}