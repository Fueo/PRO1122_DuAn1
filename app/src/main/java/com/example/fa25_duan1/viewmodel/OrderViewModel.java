package com.example.fa25_duan1.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.CheckoutRequest;
import com.example.fa25_duan1.model.CheckoutResponse;
import com.example.fa25_duan1.model.Order;
import com.example.fa25_duan1.repository.OrderRepository;

import java.util.List;

public class OrderViewModel extends AndroidViewModel {

    private final OrderRepository repository;

    // LiveData danh sách đơn hàng (đã kèm chi tiết)
    private final MutableLiveData<List<Order>> orderHistory = new MutableLiveData<>();

    // LiveData thông báo (Toast)
    private final MutableLiveData<String> message = new MutableLiveData<>();

    // LiveData báo hiệu Checkout thành công (chứa OrderId để chuyển màn hình)
    private final MutableLiveData<String> checkoutSuccessOrderId = new MutableLiveData<>();

    public OrderViewModel(@NonNull Application application) {
        super(application);
        repository = new OrderRepository(application);
    }

    // --- Getters ---
    public LiveData<List<Order>> getOrderHistory() { return orderHistory; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<String> getCheckoutSuccessOrderId() { return checkoutSuccessOrderId; }

    // --- 1. CHECKOUT ---
    public void checkout(String fullname, String address, String phone, String note, String paymentMethod) {
        CheckoutRequest request = new CheckoutRequest(fullname, address, phone, note, paymentMethod);

        LiveData<ApiResponse<CheckoutResponse>> liveData = repository.checkout(request);
        liveData.observeForever(new Observer<ApiResponse<CheckoutResponse>>() {
            @Override
            public void onChanged(ApiResponse<CheckoutResponse> response) {
                if (response != null && response.isStatus()) {
                    message.setValue("Đặt hàng thành công!");
                    // Bắn OrderId ra để Activity biết mà chuyển sang màn hình "Thành công" hoặc "Lịch sử"
                    if (response.getData() != null) {
                        checkoutSuccessOrderId.setValue(response.getData().getOrderId());
                    }
                } else {
                    if (response != null && response.getMessage() != null) {
                        message.setValue(response.getMessage()); // VD: Sản phẩm A không đủ số lượng
                    } else {
                        message.setValue("Đặt hàng thất bại. Vui lòng thử lại.");
                    }
                }
                liveData.removeObserver(this);
            }
        });
    }

    // --- 2. FETCH HISTORY ---
    public void fetchOrderHistory() {
        LiveData<List<Order>> liveData = repository.getOrderHistory();
        liveData.observeForever(new Observer<List<Order>>() {
            @Override
            public void onChanged(List<Order> orders) {
                if (orders != null) {
                    orderHistory.setValue(orders);
                } else {
                    message.setValue("Lỗi tải lịch sử đơn hàng hoặc danh sách trống");
                    orderHistory.setValue(null);
                }
                liveData.removeObserver(this);
            }
        });
    }

    // --- 3. CANCEL ORDER ---
    public void cancelOrder(String orderId) {
        LiveData<ApiResponse<Void>> liveData = repository.cancelOrder(orderId);
        liveData.observeForever(new Observer<ApiResponse<Void>>() {
            @Override
            public void onChanged(ApiResponse<Void> response) {
                if (response != null && response.isStatus()) {
                    message.setValue("Đã hủy đơn hàng");
                    // Reload lại danh sách ngay lập tức để cập nhật UI
                    fetchOrderHistory();
                } else {
                    message.setValue(response != null ? response.getMessage() : "Lỗi khi hủy đơn");
                }
                liveData.removeObserver(this);
            }
        });
    }
}