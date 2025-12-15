package com.example.fa25_duan1.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fa25_duan1.model.ApiResponse; // Import ApiResponse
import com.example.fa25_duan1.model.CartItem;
import com.example.fa25_duan1.repository.CartRepository;

import java.util.ArrayList;
import java.util.List;

public class CartViewModel extends AndroidViewModel {

    private final CartRepository repository;

    // Quản lý danh sách giỏ hàng
    private final MediatorLiveData<List<CartItem>> cartItems = new MediatorLiveData<>();

    // LiveData báo lỗi về UI
    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>();

    // Biến theo dõi nguồn dữ liệu hiện tại (Kiểu ApiResponse)
    private LiveData<ApiResponse<List<CartItem>>> currentRepoSource;

    // Các biến dữ liệu tính toán (Derived data)
    private final MutableLiveData<Long> totalPrice = new MutableLiveData<>(0L);
    private final MutableLiveData<Integer> totalQuantity = new MutableLiveData<>(0);

    public CartViewModel(@NonNull Application application) {
        super(application);
        repository = new CartRepository(application);
        // refreshCart(); // Tùy chọn gọi ngay hoặc để Fragment gọi onResume
    }

    // --- Getters cho UI observe ---
    public LiveData<List<CartItem>> getCartItems() { return cartItems; }
    public LiveData<Long> getTotalPrice() { return totalPrice; }
    public LiveData<Integer> getTotalQuantity() { return totalQuantity; }
    public LiveData<String> getMessage() { return messageLiveData; }

    /**
     * Tải lại danh sách giỏ hàng từ Repository
     */
    public void refreshCart() {
        if (currentRepoSource != null) {
            cartItems.removeSource(currentRepoSource);
        }

        // Gọi Repository (trả về ApiResponse)
        currentRepoSource = repository.getCart();

        cartItems.addSource(currentRepoSource, apiResponse -> {
            List<CartItem> items = new ArrayList<>();

            if (apiResponse != null) {
                if (apiResponse.isStatus()) {
                    // Thành công: Lấy data
                    if (apiResponse.getData() != null) {
                        items = apiResponse.getData();
                    }
                } else {
                    // Thất bại: Gửi thông báo lỗi
                    messageLiveData.setValue(apiResponse.getMessage());
                }
            } else {
                messageLiveData.setValue("Lỗi kết nối");
            }

            // Cập nhật danh sách
            cartItems.setValue(items);

            // Tự động tính toán lại tổng tiền
            calculateTotal(items);
        });
    }

    // --- CRUD ACTIONS (CẬP NHẬT KIỂU TRẢ VỀ APIRESPONSE) ---

    // 1. Thêm vào giỏ / Tăng số lượng
    // Trả về ApiResponse<CartItem> để biết item mới cập nhật
    public LiveData<ApiResponse<CartItem>> addToCart(String productId, int quantity) {
        return repository.addToCart(productId, quantity);
    }

    // Alias cho addToCart(id, 1)
    public LiveData<ApiResponse<CartItem>> increaseQuantity(String productId) {
        return repository.addToCart(productId, 1);
    }

    // 2. Giảm số lượng
    public LiveData<ApiResponse<CartItem>> decreaseQuantity(String productId) {
        return repository.decreaseQuantity(productId);
    }

    // 3. Xóa sản phẩm
    // Trả về ApiResponse<Void> thay vì Boolean
    public LiveData<ApiResponse<Void>> deleteItem(String cartItemId) {
        return repository.deleteCartItem(cartItemId);
    }

    // 4. Kiểm tra giỏ hàng trước khi thanh toán (Check tồn kho)
    // Trả về ApiResponse<Boolean> (Data bên trong là true/false hợp lệ hay không)
    public LiveData<ApiResponse<Boolean>> checkCartAvailability() {
        return repository.checkCartAvailability();
    }

    // 5. Xóa toàn bộ giỏ hàng
    public LiveData<ApiResponse<Void>> clearCart() {
        return repository.clearCart();
    }

    /**
     * Logic tính tổng tiền (Chạy nội bộ mỗi khi list update)
     */
    private void calculateTotal(List<CartItem> items) {
        long total = 0;
        int count = 0;

        if (items != null) {
            for (CartItem item : items) {
                if (item.getProduct() != null) {
                    // Logic tính giá: Giá gốc * (1 - %giảm) * số lượng
                    double priceAfterDiscount = item.getPrice() * (1 - (item.getDiscount() / 100.0));
                    total += (long) (priceAfterDiscount * item.getQuantity());
                    count += item.getQuantity();
                }
            }
        }

        // Cập nhật lên LiveData
        totalPrice.setValue(total);
        totalQuantity.setValue(count);
    }
}