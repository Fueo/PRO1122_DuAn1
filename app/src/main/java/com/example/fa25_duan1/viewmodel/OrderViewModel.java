package com.example.fa25_duan1.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.CheckoutRequest;
import com.example.fa25_duan1.model.CheckoutResponse;
import com.example.fa25_duan1.model.Order;
import com.example.fa25_duan1.model.ZaloPayResult;
import com.example.fa25_duan1.repository.OrderRepository;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class OrderViewModel extends AndroidViewModel {

    private final OrderRepository repository;

    private final MediatorLiveData<List<Order>> orderHistory = new MediatorLiveData<>();
    private LiveData<List<Order>> currentSource;

    private List<Order> masterOrderList = new ArrayList<>();
    private final MutableLiveData<List<Order>> displayedOrders = new MutableLiveData<>();

    private SimpleDateFormat isoFormat;

    public OrderViewModel(@NonNull Application application) {
        super(application);
        repository = new OrderRepository(application);

        isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    // --- GETTERS ---
    public LiveData<List<Order>> getOrderHistory() { return orderHistory; }
    public LiveData<List<Order>> getDisplayedOrders() { return displayedOrders; }

    // --- API CALLS ---

    // 1. Checkout
    public LiveData<ApiResponse<CheckoutResponse>> checkout(String fullname, String address, String phone, String note, String paymentMethod) {
        CheckoutRequest request = new CheckoutRequest(fullname, address, phone, note, paymentMethod);
        return repository.checkout(request);
    }

    // 2. Fetch History (User)
    public void fetchOrderHistory() {
        if (currentSource != null) orderHistory.removeSource(currentSource);
        currentSource = repository.getOrderHistory();
        orderHistory.addSource(currentSource, orders -> {
            if (orders == null) orders = new ArrayList<>();
            this.masterOrderList = new ArrayList<>(orders);
            filterAndSortOrders(0, null, 0, 0, null);
            orderHistory.setValue(orders);
        });
    }

    // 3. Cancel
    public LiveData<ApiResponse<Void>> cancelOrder(String orderId) {
        return repository.cancelOrder(orderId);
    }

    // 4. Fetch All (Admin)
    public void fetchAllOrdersForAdmin() {
        if (currentSource != null) orderHistory.removeSource(currentSource);
        currentSource = repository.getAllOrders();
        orderHistory.addSource(currentSource, orders -> {
            if (orders == null) orders = new ArrayList<>();
            this.masterOrderList = new ArrayList<>(orders);
            filterAndSortOrders(0, null, 0, 0, null);
            orderHistory.setValue(orders);
        });
    }

    // 5. Update Order Status (ADMIN - Full Control)
    public LiveData<ApiResponse<Order>> updateOrderStatus(String orderId, String newStatus, String paymentMethod, Boolean isPaid) {
        return repository.updateOrderStatus(orderId, newStatus, paymentMethod, isPaid);
    }

    // 6. Update Payment Method (USER)
    public LiveData<ApiResponse<Order>> updatePaymentMethod(String orderId, String newPaymentMethod) {
        return repository.updatePaymentMethod(orderId, newPaymentMethod);
    }

    // 7. Get Detail
    public LiveData<ApiResponse<Order>> getOrderById(String orderId) {
        return repository.getOrderById(orderId);
    }

    // 8. Stats
    public LiveData<ApiResponse<Map<String, Integer>>> getStatusCount() {
        return repository.getStatusCount();
    }
    public LiveData<ApiResponse<Integer>> getTotalOrders() {
        return repository.getTotalOrders();
    }

    // [MỚI 9] Tạo thanh toán ZaloPay
    public LiveData<ApiResponse<ZaloPayResult>> createZaloPayPayment(String orderId) {
        return repository.createZaloPayOrder(orderId);
    }

    // =========================================================================
    // KHU VỰC LOGIC LỌC VÀ SẮP XẾP
    // =========================================================================

    public void filterAndSortOrders(int sortType, List<Integer> statusFilter, long startDate, long endDate, List<Integer> priceRanges) {
        List<Order> result = new ArrayList<>();
        if (masterOrderList == null) {
            displayedOrders.setValue(new ArrayList<>());
            return;
        }

        for (Order order : masterOrderList) {
            boolean matchesStatus = checkStatusMatch(order.getStatus(), statusFilter);
            boolean matchesDate = checkDateMatch(order.getDate(), startDate, endDate);
            boolean matchesPrice = checkPriceMatch(order.getTotal(), priceRanges);

            if (matchesStatus && matchesDate && matchesPrice) {
                result.add(order);
            }
        }
        sortResultList(result, sortType);
        displayedOrders.setValue(result);
    }

    // --- CÁC HÀM CHECK LOGIC CON ---
    private boolean checkStatusMatch(String serverStatus, List<Integer> filterCodes) {
        if (filterCodes == null || filterCodes.isEmpty()) return true;
        if (serverStatus == null) return false;
        String status = serverStatus.toLowerCase().trim();

        for (int code : filterCodes) {
            switch (code) {
                case 0: if (status.equals("pending") || status.equals("wait_confirm") || status.equals("chờ xác nhận")) return true; break;
                case 1: if (status.equals("processing") || status.equals("confirmed") || status.equals("đang xử lý")) return true; break;
                case 2: if (status.equals("shipping") || status.equals("shipped") || status.contains("đang giao")) return true; break;
                case 3: if (status.equals("delivered") || status.equals("completed") || status.equals("hoàn thành")) return true; break;
                case 4: if (status.equals("cancelled") || status.equals("canceled") || status.equals("đã hủy")) return true; break;
            }
        }
        return false;
    }

    private boolean checkPriceMatch(double total, List<Integer> rangeCodes) {
        if (rangeCodes == null || rangeCodes.isEmpty()) return true;
        for (int code : rangeCodes) {
            switch (code) {
                case 0: if (total < 500000) return true; break;
                case 1: if (total >= 500000 && total < 1000000) return true; break;
                case 2: if (total >= 1000000 && total < 5000000) return true; break;
                case 3: if (total >= 5000000) return true; break;
            }
        }
        return false;
    }

    private boolean checkDateMatch(String dateString, long startDate, long endDate) {
        if (startDate == 0 && endDate == 0) return true;
        long orderTime = parseDateToLong(dateString);
        if (orderTime == 0) return false;

        LocalDate orderDate = Instant.ofEpochMilli(orderTime).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate startLocal = (startDate == 0) ? LocalDate.MIN : Instant.ofEpochMilli(startDate).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocal = (endDate == 0) ? LocalDate.MAX : Instant.ofEpochMilli(endDate).atZone(ZoneId.systemDefault()).toLocalDate();
        return !orderDate.isBefore(startLocal) && !orderDate.isAfter(endLocal);
    }

    public void searchOrders(String query, int searchType) {
        if (masterOrderList == null || masterOrderList.isEmpty()) return;
        if (query == null || query.trim().isEmpty()) {
            filterAndSortOrders(0, null, 0, 0, null);
            return;
        }
        String finalQuery = query.toLowerCase().trim();
        List<Order> result = new ArrayList<>();
        for (Order order : masterOrderList) {
            boolean isMatch = false;
            switch (searchType) {
                case 0: if (order.getFullname() != null && order.getFullname().toLowerCase().contains(finalQuery)) isMatch = true; break;
                case 1: if (order.getPhone() != null && order.getPhone().contains(finalQuery)) isMatch = true; break;
                case 2: if (order.getDate() != null && order.getDate().contains(finalQuery)) isMatch = true; break;
            }
            if (isMatch) result.add(order);
        }
        sortResultList(result, 0);
        displayedOrders.setValue(result);
    }

    private void sortResultList(List<Order> list, int sortType) {
        if (list == null || list.isEmpty()) return;
        switch (sortType) {
            case 0: Collections.sort(list, (o1, o2) -> Long.compare(parseDateToLong(o2.getDate()), parseDateToLong(o1.getDate()))); break;
            case 1: Collections.sort(list, (o1, o2) -> Long.compare(parseDateToLong(o1.getDate()), parseDateToLong(o2.getDate()))); break;
            case 2: Collections.sort(list, (o1, o2) -> Double.compare(o1.getTotal(), o2.getTotal())); break;
            case 3: Collections.sort(list, (o1, o2) -> Double.compare(o2.getTotal(), o1.getTotal())); break;
        }
    }

    private long parseDateToLong(String dateString) {
        if (dateString == null || dateString.isEmpty()) return 0;
        try {
            Date date = isoFormat.parse(dateString);
            return date != null ? date.getTime() : 0;
        } catch (Exception e) {}
        try {
            if (dateString.length() >= 23) {
                String cleanDate = dateString.substring(0, 23);
                SimpleDateFormat manualFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
                manualFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = manualFormat.parse(cleanDate);
                return date != null ? date.getTime() : 0;
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return 0;
    }
}