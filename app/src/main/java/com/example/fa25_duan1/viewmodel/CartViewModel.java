package com.example.fa25_duan1.viewmodel;

// viewmodel/CartViewModel.java

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import com.example.fa25_duan1.model.CartItem;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartViewModel extends ViewModel {

    private final MutableLiveData<List<CartItem>> _cartItems = new MutableLiveData<>();
    public final LiveData<List<CartItem>> cartItems = _cartItems;

    private final double shippingFee = 25000.0;

    public final LiveData<Double> subtotal;
    public final LiveData<Double> total;

    public final LiveData<String> subtotalFormatted;
    public final LiveData<String> shippingFeeFormatted;
    public final LiveData<String> totalFormatted;

    public CartViewModel() {
        // Khởi tạo danh sách
        _cartItems.setValue(new ArrayList<>());

        // Tính toán tự động khi _cartItems thay đổi
        subtotal = Transformations.map(_cartItems, this::calculateSubtotal);
        total = Transformations.map(subtotal, sub -> sub + shippingFee);

        // Định dạng tiền tệ
        Locale localeVN = new Locale("vi", "VN");
        subtotalFormatted = Transformations.map(subtotal, amount -> formatCurrency(amount, localeVN));
        shippingFeeFormatted = new MutableLiveData<>(formatCurrency(shippingFee, localeVN));
        totalFormatted = Transformations.map(total, amount -> formatCurrency(amount, localeVN));

        loadDummyData();
    }

    private void loadDummyData() {
        List<CartItem> dummyList = new ArrayList<>();
        dummyList.add(new CartItem("1", "Và Rồi Chẳng Còn Ai", 75000.0, 1, "your_image_url_1"));
        dummyList.add(new CartItem("2", "Nhà Giả Kim", 90000.0, 2, "your_image_url_2"));
        _cartItems.setValue(dummyList);
    }

    private Double calculateSubtotal(List<CartItem> items) {
        double sum = 0.0;
        if (items != null) {
            for (CartItem item : items) {
                sum += item.getPrice() * item.getQuantity();
            }
        }
        return sum;
    }

    private String formatCurrency(Double amount, Locale locale) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        return formatter.format(amount);
    }

    // Hàm tiện ích để cập nhật LiveData (bắt buộc tạo list mới)
    private void updateCartList(List<CartItem> newList) {
        _cartItems.setValue(newList);
    }

    public void increaseQuantity(CartItem item) {
        List<CartItem> currentList = _cartItems.getValue();
        if (currentList == null) return;

        List<CartItem> newList = new ArrayList<>(currentList);
        for (int i = 0; i < newList.size(); i++) {
            if (newList.get(i).getId().equals(item.getId())) {
                CartItem oldItem = newList.get(i);
                oldItem.setQuantity(oldItem.getQuantity() + 1);
                break;
            }
        }
        updateCartList(newList);
    }

    public void decreaseQuantity(CartItem item) {
        List<CartItem> currentList = _cartItems.getValue();
        if (currentList == null) return;

        List<CartItem> newList = new ArrayList<>(currentList);
        for (int i = 0; i < newList.size(); i++) {
            CartItem currentItem = newList.get(i);
            if (currentItem.getId().equals(item.getId()) && currentItem.getQuantity() > 1) {
                currentItem.setQuantity(currentItem.getQuantity() - 1);
                break;
            }
        }
        updateCartList(newList);
    }

    public void deleteItem(CartItem item) {
        List<CartItem> currentList = _cartItems.getValue();
        if (currentList == null) return;

        List<CartItem> newList = new ArrayList<>(currentList);
        // Sử dụng removeIf cho Java 8+
        newList.removeIf(cartItem -> cartItem.getId().equals(item.getId()));
        updateCartList(newList);
    }
}