package com.example.fa25_duan1.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.CheckoutRequest;
import com.example.fa25_duan1.model.CheckoutResponse;
import com.example.fa25_duan1.model.Order;
import com.example.fa25_duan1.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;

public class OrderViewModel extends AndroidViewModel {

    private final OrderRepository repository;

    // Dùng MediatorLiveData để quản lý danh sách lịch sử (Giống AddressViewModel)
    private final MediatorLiveData<List<Order>> orderHistory = new MediatorLiveData<>();
    private LiveData<List<Order>> currentSource;

    public OrderViewModel(@NonNull Application application) {
        super(application);
        repository = new OrderRepository(application);
        // Tự động tải lịch sử khi khởi tạo (nếu muốn)
        // fetchOrderHistory();
    }

    public LiveData<List<Order>> getOrderHistory() { return orderHistory; }

    // --- 1. CHECKOUT (CHUẨN: Trả về LiveData) ---
    // Fragment sẽ gọi hàm này và observe kết quả trực tiếp
    public LiveData<ApiResponse<CheckoutResponse>> checkout(String fullname, String address, String phone, String note, String paymentMethod) {
        CheckoutRequest request = new CheckoutRequest(fullname, address, phone, note, paymentMethod);
        return repository.checkout(request);
    }

    // --- 2. FETCH HISTORY (CHUẨN: Dùng Mediator) ---
    public void fetchOrderHistory() {
        if (currentSource != null) {
            orderHistory.removeSource(currentSource);
        }

        currentSource = repository.getOrderHistory();

        orderHistory.addSource(currentSource, orders -> {
            if (orders == null) {
                orders = new ArrayList<>();
            }
            // Có thể sort danh sách tại đây nếu cần (VD: Đơn mới nhất lên đầu)
            // orders.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));

            orderHistory.setValue(orders);
        });
    }

    // --- 3. CANCEL ORDER (CHUẨN: Trả về LiveData) ---
    public LiveData<ApiResponse<Void>> cancelOrder(String orderId) {
        return repository.cancelOrder(orderId);
    }
}