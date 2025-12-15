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

    // ===================== LIVE DATA =====================
    private final MediatorLiveData<List<Order>> orderHistory = new MediatorLiveData<>();
    private LiveData<ApiResponse<List<Order>>> currentSource;
    private final MutableLiveData<List<Order>> displayedOrders = new MutableLiveData<>();
    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>();

    // ===================== DATA =====================
    private List<Order> masterOrderList = new ArrayList<>();

    // ===================== FILTER STATE =====================
    private int currentSortType = 0;
    private List<Integer> currentStatusFilter = null;
    private long currentStartDate = 0;
    private long currentEndDate = 0;
    private List<Integer> currentPriceRanges = null;

    // ===================== DATE FORMAT =====================
    private final SimpleDateFormat isoFormat;

    public OrderViewModel(@NonNull Application application) {
        super(application);
        repository = new OrderRepository(application);

        isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    // ===================== GETTERS =====================
    public LiveData<List<Order>> getOrderHistory() {
        return orderHistory;
    }

    public LiveData<List<Order>> getDisplayedOrders() {
        return displayedOrders;
    }

    public LiveData<String> getMessage() {
        return messageLiveData;
    }

    // ======================================================
    // ===================== API CALLS ======================
    // ======================================================

    // 1. Checkout
    public LiveData<ApiResponse<CheckoutResponse>> checkout(String fullname, String address, String phone, String note, String paymentMethod) {
        CheckoutRequest request = new CheckoutRequest(fullname, address, phone, note, paymentMethod);
        return repository.checkout(request);
    }

    // 2. Fetch Order History (USER)
    public void fetchOrderHistory() {
        if (currentSource != null) orderHistory.removeSource(currentSource);

        currentSource = repository.getOrderHistory();
        orderHistory.addSource(currentSource, apiResponse -> handleOrderResponse(apiResponse));
    }

    // 3. Fetch All Orders (ADMIN)
    public void fetchAllOrdersForAdmin() {
        if (currentSource != null) orderHistory.removeSource(currentSource);

        currentSource = repository.getAllOrders();
        orderHistory.addSource(currentSource, apiResponse -> handleOrderResponse(apiResponse));
    }

    // 4. Cancel Order
    public LiveData<ApiResponse<Void>> cancelOrder(String orderId) {
        return repository.cancelOrder(orderId);
    }

    // 5. Update Order Status (ADMIN)
    public LiveData<ApiResponse<Order>> updateOrderStatus(String orderId, String newStatus, String paymentMethod, Boolean isPaid) {
        return repository.updateOrderStatus(orderId, newStatus, paymentMethod, isPaid);
    }

    // 6. Update Payment Method (USER)
    public LiveData<ApiResponse<Order>> updatePaymentMethod(String orderId, String newPaymentMethod) {
        return repository.updatePaymentMethod(orderId, newPaymentMethod);
    }

    // 7. Get Order Detail
    public LiveData<ApiResponse<Order>> getOrderById(String orderId) {
        return repository.getOrderById(orderId);
    }

    // 8. Statistics
    public LiveData<ApiResponse<Map<String, Integer>>> getStatusCount() {
        return repository.getStatusCount();
    }

    public LiveData<ApiResponse<Integer>> getTotalOrders() {
        return repository.getTotalOrders();
    }

    // 9. ZaloPay
    public LiveData<ApiResponse<ZaloPayResult>> createZaloPayPayment(String orderId) {
        return repository.createZaloPayOrder(orderId);
    }

    public LiveData<ApiResponse<Map<String, Object>>> checkZaloPayStatus(String appTransId) {
        return repository.checkZaloPayStatus(appTransId);
    }

    // ======================================================
    // ===================== CORE LOGIC =====================
    // ======================================================

    private void handleOrderResponse(ApiResponse<List<Order>> apiResponse) {
        List<Order> orders = new ArrayList<>();

        if (apiResponse != null) {
            if (apiResponse.isStatus() && apiResponse.getData() != null) {
                orders = apiResponse.getData();
            } else {
                messageLiveData.setValue(apiResponse.getMessage());
            }
        } else {
            messageLiveData.setValue("Lỗi kết nối");
        }

        masterOrderList = new ArrayList<>(orders);
        applyFilterInternal(); // ✅ GIỮ FILTER
        orderHistory.setValue(orders);
    }

    // ======================================================
    // ===================== FILTER & SORT ==================
    // ======================================================

    public void filterAndSortOrders(int sortType,
                                    List<Integer> statusFilter,
                                    long startDate,
                                    long endDate,
                                    List<Integer> priceRanges) {

        // ✅ SAVE STATE
        currentSortType = sortType;
        currentStatusFilter = statusFilter;
        currentStartDate = startDate;
        currentEndDate = endDate;
        currentPriceRanges = priceRanges;

        applyFilterInternal();
    }

    private void applyFilterInternal() {
        List<Order> result = new ArrayList<>();
        if (masterOrderList == null || masterOrderList.isEmpty()) {
            displayedOrders.setValue(result);
            return;
        }

        for (Order order : masterOrderList) {
            if (checkStatusMatch(order.getStatus(), currentStatusFilter)
                    && checkDateMatch(order.getDate(), currentStartDate, currentEndDate)
                    && checkPriceMatch(order.getTotal(), currentPriceRanges)) {
                result.add(order);
            }
        }

        sortResultList(result, currentSortType);
        displayedOrders.setValue(result);
    }

    // ======================================================
    // ===================== SEARCH =========================
    // ======================================================

    public void searchOrders(String query, int searchType) {
        if (masterOrderList == null || masterOrderList.isEmpty()) return;

        if (query == null || query.trim().isEmpty()) {
            applyFilterInternal();
            return;
        }

        String finalQuery = query.toLowerCase().trim();
        List<Order> result = new ArrayList<>();

        for (Order order : masterOrderList) {
            boolean match = false;
            switch (searchType) {
                case 0:
                    match = order.getFullname() != null && order.getFullname().toLowerCase().contains(finalQuery);
                    break;
                case 1:
                    match = order.getPhone() != null && order.getPhone().contains(finalQuery);
                    break;
                case 2:
                    match = order.getDate() != null && order.getDate().contains(finalQuery);
                    break;
            }
            if (match) result.add(order);
        }

        sortResultList(result, currentSortType);
        displayedOrders.setValue(result);
    }

    // ======================================================
    // ===================== UTIL METHODS ===================
    // ======================================================

    private boolean checkStatusMatch(String serverStatus, List<Integer> filterCodes) {
        if (filterCodes == null || filterCodes.isEmpty()) return true;
        if (serverStatus == null) return false;

        String status = serverStatus.toLowerCase().trim();
        for (int code : filterCodes) {
            switch (code) {
                case 0:
                    if (status.contains("pending") || status.contains("wait") || status.contains("chờ")) return true;
                    break;
                case 1:
                    if (status.contains("processing") || status.contains("confirmed")) return true;
                    break;
                case 2:
                    if (status.contains("shipping") || status.contains("giao")) return true;
                    break;
                case 3:
                    if (status.contains("completed") || status.contains("delivered")) return true;
                    break;
                case 4:
                    if (status.contains("cancel")) return true;
                    break;
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

        LocalDate orderDate = Instant.ofEpochMilli(orderTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate start = startDate == 0 ? LocalDate.MIN :
                Instant.ofEpochMilli(startDate).atZone(ZoneId.systemDefault()).toLocalDate();

        LocalDate end = endDate == 0 ? LocalDate.MAX :
                Instant.ofEpochMilli(endDate).atZone(ZoneId.systemDefault()).toLocalDate();

        return !orderDate.isBefore(start) && !orderDate.isAfter(end);
    }

    private void sortResultList(List<Order> list, int sortType) {
        if (list == null || list.isEmpty()) return;

        switch (sortType) {
            case 0:
                Collections.sort(list, (o1, o2) -> Long.compare(parseDateToLong(o2.getDate()), parseDateToLong(o1.getDate())));
                break;
            case 1:
                Collections.sort(list, (o1, o2) -> Long.compare(parseDateToLong(o1.getDate()), parseDateToLong(o2.getDate())));
                break;
            case 2:
                Collections.sort(list, (o1, o2) -> Double.compare(o1.getTotal(), o2.getTotal()));
                break;
            case 3:
                Collections.sort(list, (o1, o2) -> Double.compare(o2.getTotal(), o1.getTotal()));
                break;
        }
    }

    private long parseDateToLong(String dateString) {
        if (dateString == null || dateString.isEmpty()) return 0;
        try {
            Date date = isoFormat.parse(dateString);
            return date != null ? date.getTime() : 0;
        } catch (Exception ignored) {}
        return 0;
    }

    public int getCurrentSortType() {
        return currentSortType;
    }

    public List<Integer> getCurrentStatusFilter() {
        return currentStatusFilter;
    }

    public long getCurrentStartDate() {
        return currentStartDate;
    }

    public long getCurrentEndDate() {
        return currentEndDate;
    }

    public List<Integer> getCurrentPriceRanges() {
        return currentPriceRanges;
    }
}
