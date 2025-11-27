package com.example.fa25_duan1.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.CartItem;
import com.example.fa25_duan1.repository.CartRepository;

import java.util.List;

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

    // --- Fetch Cart ---
    public void fetchCart() {
        LiveData<List<CartItem>> liveData = repository.getCart();
        liveData.observeForever(new Observer<List<CartItem>>() {
            @Override
            public void onChanged(List<CartItem> items) {
                if (items != null) {
                    cartItems.setValue(items);
                    calculateTotal(items);
                } else {
                    message.setValue("Lỗi tải giỏ hàng hoặc giỏ hàng trống");
                    cartItems.setValue(null);
                    calculateTotal(null);
                }
                // Hủy đăng ký sau khi nhận kết quả (One-shot)
                liveData.removeObserver(this);
            }
        });
    }

    // --- Increase Quantity / Add ---
    public void increaseQuantity(String productId) {
        LiveData<ApiResponse<CartItem>> liveData = repository.addToCart(productId, 1);
        liveData.observeForever(new Observer<ApiResponse<CartItem>>() {
            @Override
            public void onChanged(ApiResponse<CartItem> response) {
                if (response != null && response.isStatus()) {
                    // Thành công -> Load lại danh sách để cập nhật giá/tổng tiền
                    fetchCart();
                } else {
                    // Thất bại (VD: Hết hàng)
                    if (response != null && response.getMessage() != null) {
                        message.setValue(response.getMessage());
                    } else {
                        message.setValue("Không thể thêm (Hết hàng hoặc lỗi mạng)");
                    }
                }
                liveData.removeObserver(this);
            }
        });
    }

    // --- Decrease Quantity ---
    public void decreaseQuantity(String productId) {
        LiveData<ApiResponse<CartItem>> liveData = repository.decreaseQuantity(productId);
        liveData.observeForever(new Observer<ApiResponse<CartItem>>() {
            @Override
            public void onChanged(ApiResponse<CartItem> response) {
                if (response != null && response.isStatus()) {
                    fetchCart();
                } else {
                    message.setValue(response != null ? response.getMessage() : "Lỗi giảm số lượng");
                }
                liveData.removeObserver(this);
            }
        });
    }

    // --- Delete Item ---
    public void deleteItem(String cartItemId) {
        LiveData<Boolean> liveData = repository.deleteCartItem(cartItemId);
        liveData.observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isSuccess) {
                if (isSuccess) {
                    fetchCart();
                    message.setValue("Đã xóa sản phẩm");
                } else {
                    message.setValue("Xóa thất bại");
                }
                liveData.removeObserver(this);
            }
        });
    }

    public LiveData<Boolean> checkCartAvailability() {
        return repository.checkCartAvailability();
    }

    // Tính tổng tiền
    private void calculateTotal(List<CartItem> items) {
        long total = 0;
        int count = 0;
        if (items != null) {
            for (CartItem item : items) {
                if (item.getProduct() != null) {
                    // Sử dụng item.getPrice() đã cập nhật (giá trong giỏ)
                    total += (long) (item.getPrice() * item.getQuantity());
                    count += item.getQuantity();
                }
            }
        }
        totalPrice.setValue(total);
        totalQuantity.setValue(count);
    }
}