package com.example.fa25_duan1.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.CartItem;
import com.example.fa25_duan1.repository.CartRepository;

import java.util.ArrayList;
import java.util.List;

public class CartViewModel extends AndroidViewModel {

    private final CartRepository repository;

    // Quản lý danh sách giỏ hàng bằng MediatorLiveData (giống AddressViewModel)
    private final MediatorLiveData<List<CartItem>> cartItems = new MediatorLiveData<>();

    // Biến theo dõi nguồn dữ liệu hiện tại để tránh chồng chéo observer
    private LiveData<List<CartItem>> currentRepoSource;

    // Các biến dữ liệu tính toán (Derived data)
    private final MutableLiveData<Long> totalPrice = new MutableLiveData<>(0L);
    private final MutableLiveData<Integer> totalQuantity = new MutableLiveData<>(0);

    public CartViewModel(@NonNull Application application) {
        super(application);
        repository = new CartRepository(application);
        // Tự động tải giỏ hàng khi khởi tạo ViewModel
        refreshCart();
    }

    // --- Getters cho UI observe ---
    public LiveData<List<CartItem>> getCartItems() { return cartItems; }
    public LiveData<Long> getTotalPrice() { return totalPrice; }
    public LiveData<Integer> getTotalQuantity() { return totalQuantity; }

    /**
     * Tải lại danh sách giỏ hàng từ Repository (Giống logic AddressViewModel)
     */
    public void refreshCart() {
        // 1. Gỡ bỏ nguồn cũ nếu đang lắng nghe
        if (currentRepoSource != null) {
            cartItems.removeSource(currentRepoSource);
        }

        // 2. Lấy nguồn mới từ Repository
        currentRepoSource = repository.getCart();

        // 3. Lắng nghe nguồn mới và cập nhật dữ liệu
        cartItems.addSource(currentRepoSource, items -> {
            if (items == null) {
                items = new ArrayList<>();
            }
            // Cập nhật danh sách
            cartItems.setValue(items);

            // Tự động tính toán lại tổng tiền bất cứ khi nào danh sách thay đổi
            calculateTotal(items);
        });
    }

    // --- CRUD ACTIONS (Trả về LiveData để View tự xử lý) ---

    // Tăng số lượng / Thêm vào giỏ
    public LiveData<ApiResponse<CartItem>> increaseQuantity(String productId) {
        return repository.addToCart(productId, 1);
    }

    // Giảm số lượng
    public LiveData<ApiResponse<CartItem>> decreaseQuantity(String productId) {
        return repository.decreaseQuantity(productId);
    }

    // Xóa sản phẩm
    public LiveData<Boolean> deleteItem(String cartItemId) {
        return repository.deleteCartItem(cartItemId);
    }

    // Kiểm tra giỏ hàng trước khi thanh toán
    public LiveData<Boolean> checkCartAvailability() {
        return repository.checkCartAvailability();
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

    public LiveData<Boolean> clearCart() {
        return repository.clearCart();
    }

    public LiveData<ApiResponse<CartItem>> addToCart(String productId, int quantity) {
        return repository.addToCart(productId, quantity);
    }

}