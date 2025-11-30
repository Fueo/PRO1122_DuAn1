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

    // LiveData danh sách đơn hàng
    private final MutableLiveData<List<Order>> orderHistory = new MutableLiveData<>();

    // LiveData thông báo lỗi (Chỉ dùng để bắn lỗi ra UI)
    private final MutableLiveData<String> message = new MutableLiveData<>();

    // LiveData báo hiệu Checkout thành công (chứa OrderId)
    private final MutableLiveData<String> checkoutSuccessOrderId = new MutableLiveData<>();

    public OrderViewModel(@NonNull Application application) {
        super(application);
        repository = new OrderRepository(application);
    }

    // --- Getters ---
    public LiveData<List<Order>> getOrderHistory() { return orderHistory; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<String> getCheckoutSuccessOrderId() { return checkoutSuccessOrderId; }

    // --- 1. CHECKOUT (ĐÃ SỬA) ---
    public void checkout(String fullname, String address, String phone, String note, String paymentMethod) {
        CheckoutRequest request = new CheckoutRequest(fullname, address, phone, note, paymentMethod);

        // Reset lại trạng thái trước khi gọi API để tránh bị trigger sự kiện cũ
        message.setValue(null);
        checkoutSuccessOrderId.setValue(null);

        LiveData<ApiResponse<CheckoutResponse>> liveData = repository.checkout(request);
        liveData.observeForever(new Observer<ApiResponse<CheckoutResponse>>() {
            @Override
            public void onChanged(ApiResponse<CheckoutResponse> response) {
                if (response != null && response.isStatus()) {
                    // --- THAY ĐỔI QUAN TRỌNG Ở ĐÂY ---

                    // 1. KHÔNG set message khi thành công nữa.
                    // message.setValue("Đặt hàng thành công!"); // <--- Đã xóa dòng này để tránh hiện Toast đỏ

                    // 2. Chỉ bắn OrderId để hiện Dialog Success
                    if (response.getData() != null) {
                        checkoutSuccessOrderId.setValue(response.getData().getOrderId());
                    }
                } else {
                    // Khi thất bại thì mới bắn message để hiện Toast lỗi
                    if (response != null && response.getMessage() != null) {
                        message.setValue(response.getMessage());
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
                    // Chỉ báo lỗi nếu thực sự cần thiết, hoặc có thể để null
                    // message.setValue("Lỗi tải lịch sử");
                    orderHistory.setValue(null);
                }
                liveData.removeObserver(this);
            }
        });
    }

    // --- 3. CANCEL ORDER ---
    public void cancelOrder(String orderId) {
        // Reset message
        message.setValue(null);

        LiveData<ApiResponse<Void>> liveData = repository.cancelOrder(orderId);
        liveData.observeForever(new Observer<ApiResponse<Void>>() {
            @Override
            public void onChanged(ApiResponse<Void> response) {
                if (response != null && response.isStatus()) {
                    // Với hủy đơn hàng, ta vẫn cần thông báo Toast cho người dùng biết
                    // Nhưng ở Fragment quản lý đơn hàng, bạn nên dùng Toast Success (Màu xanh)
                    // Hoặc tạm thời cứ để message, nhưng bên Fragment phải xử lý hiển thị đúng màu.
                    // Tuy nhiên, ở đây tôi set null message và load lại list để UI tự cập nhật

                    message.setValue("Đã hủy đơn hàng thành công"); // Cái này sẽ hiện Toast đỏ nếu Fragment checkout dùng chung logic, nhưng Cancel thường ở màn hình khác.

                    fetchOrderHistory(); // Reload lại list
                } else {
                    message.setValue(response != null ? response.getMessage() : "Lỗi khi hủy đơn");
                }
                liveData.removeObserver(this);
            }
        });
    }
}