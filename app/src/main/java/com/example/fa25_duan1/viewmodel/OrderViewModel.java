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

    // Danh sách gốc (chứa tất cả dữ liệu tải từ server)
    private List<Order> masterOrderList = new ArrayList<>();

    // Danh sách hiển thị (đã qua lọc/search/sắp xếp) -> Bind lên UI
    private final MutableLiveData<List<Order>> displayedOrders = new MutableLiveData<>();

    private SimpleDateFormat isoFormat;

    public OrderViewModel(@NonNull Application application) {
        super(application);
        repository = new OrderRepository(application);

        // Định dạng ngày chuẩn từ Server (ISO 8601 UTC)
        isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    // --- GETTERS ---
    public LiveData<List<Order>> getOrderHistory() { return orderHistory; }
    public LiveData<List<Order>> getDisplayedOrders() { return displayedOrders; }

    // --- API CALLS ---

    public LiveData<ApiResponse<CheckoutResponse>> checkout(String fullname, String address, String phone, String note, String paymentMethod) {
        CheckoutRequest request = new CheckoutRequest(fullname, address, phone, note, paymentMethod);
        return repository.checkout(request);
    }

    public void fetchOrderHistory() {
        if (currentSource != null) orderHistory.removeSource(currentSource);
        currentSource = repository.getOrderHistory();
        orderHistory.addSource(currentSource, orders -> {
            if (orders == null) orders = new ArrayList<>();
            this.masterOrderList = new ArrayList<>(orders);

            // Mặc định hiển thị tất cả, sắp xếp mới nhất
            filterAndSortOrders(0, null, 0, 0, null);

            orderHistory.setValue(orders);
        });
    }

    public LiveData<ApiResponse<Void>> cancelOrder(String orderId) {
        return repository.cancelOrder(orderId);
    }

    public void fetchAllOrdersForAdmin() {
        if (currentSource != null) orderHistory.removeSource(currentSource);
        currentSource = repository.getAllOrders();
        orderHistory.addSource(currentSource, orders -> {
            if (orders == null) orders = new ArrayList<>();
            this.masterOrderList = new ArrayList<>(orders);

            // Mặc định hiển thị tất cả
            filterAndSortOrders(0, null, 0, 0, null);

            orderHistory.setValue(orders);
        });
    }

    public LiveData<ApiResponse<Order>> updateOrderStatus(String orderId, String newStatus) {
        return repository.updateOrderStatus(orderId, newStatus);
    }

    public LiveData<ApiResponse<Order>> getOrderById(String orderId) {
        return repository.getOrderById(orderId);
    }

    public LiveData<ApiResponse<Map<String, Integer>>> getStatusCount() {
        return repository.getStatusCount();
    }

    public LiveData<ApiResponse<Integer>> getTotalOrders() {
        return repository.getTotalOrders();
    }

    // =========================================================================
    // KHU VỰC LOGIC LỌC VÀ SẮP XẾP (CORE LOGIC)
    // =========================================================================

    /**
     * Hàm lọc chính
     * @param sortType 0=Mới nhất, 1=Cũ nhất, 2=Giá tăng, 3=Giá giảm
     * @param statusFilter List index checkbox trạng thái (0..4)
     * @param startDate timestamp ngày bắt đầu
     * @param endDate timestamp ngày kết thúc
     * @param priceRanges List index checkbox giá (0..3)
     */
    public void filterAndSortOrders(int sortType, List<Integer> statusFilter, long startDate, long endDate, List<Integer> priceRanges) {
        List<Order> result = new ArrayList<>();

        if (masterOrderList == null) {
            displayedOrders.setValue(new ArrayList<>());
            return;
        }

        for (Order order : masterOrderList) {
            // 1. Kiểm tra trạng thái
            boolean matchesStatus = checkStatusMatch(order.getStatus(), statusFilter);

            // 2. Kiểm tra ngày tháng
            boolean matchesDate = checkDateMatch(order.getDate(), startDate, endDate);

            // 3. Kiểm tra khoảng giá
            boolean matchesPrice = checkPriceMatch(order.getTotal(), priceRanges);

            // Nếu thỏa mãn TẤT CẢ điều kiện -> Thêm vào kết quả
            if (matchesStatus && matchesDate && matchesPrice) {
                result.add(order);
            }
        }

        // 4. Sắp xếp kết quả
        sortResultList(result, sortType);

        // 5. Cập nhật UI
        displayedOrders.setValue(result);
    }

    // --- CÁC HÀM CHECK LOGIC CON (Đã cập nhật theo UI mới) ---

    private boolean checkStatusMatch(String serverStatus, List<Integer> filterCodes) {
        // Nếu không chọn filter nào -> Lấy hết
        if (filterCodes == null || filterCodes.isEmpty()) return true;
        if (serverStatus == null) return false;

        String status = serverStatus.toLowerCase().trim();

        for (int code : filterCodes) {
            switch (code) {
                case 0: // UI: Chờ xác nhận
                    if (status.equals("pending") ||
                            status.equals("wait_confirm") ||
                            status.equals("chờ xác nhận")) return true;
                    break;

                case 1: // UI: Đang xử lý
                    if (status.equals("processing") ||
                            status.equals("confirmed") ||
                            status.equals("đang xử lý")) return true;
                    break;

                case 2: // UI: Đang giao hàng
                    if (status.equals("shipping") ||
                            status.equals("shipped") ||
                            status.contains("đang giao")) return true;
                    break;

                case 3: // UI: Hoàn thành
                    if (status.equals("delivered") ||
                            status.equals("completed") ||
                            status.equals("hoàn thành")) return true;
                    break;

                case 4: // UI: Đã hủy
                    if (status.equals("cancelled") ||
                            status.equals("canceled") ||
                            status.equals("đã hủy")) return true;
                    break;
            }
        }
        return false;
    }

    private boolean checkPriceMatch(double total, List<Integer> rangeCodes) {
        // Nếu không chọn khoảng giá nào -> Lấy hết
        if (rangeCodes == null || rangeCodes.isEmpty()) return true;

        for (int code : rangeCodes) {
            switch (code) {
                case 0: // UI: < 500k
                    if (total < 500000) return true;
                    break;

                case 1: // UI: 500k - 1tr
                    if (total >= 500000 && total < 1000000) return true;
                    break;

                case 2: // UI: 1tr - 5tr
                    if (total >= 1000000 && total < 5000000) return true;
                    break;

                case 3: // UI: > 5tr
                    if (total >= 5000000) return true;
                    break;
            }
        }
        return false;
    }

    private boolean checkDateMatch(String dateString, long startDate, long endDate) {
        if (startDate == 0 && endDate == 0) return true;

        long orderTime = parseDateToLong(dateString);
        if (orderTime == 0) return false;

        // Chuyển về LocalDate để so sánh ngày (bỏ qua giờ phút)
        LocalDate orderDate = Instant.ofEpochMilli(orderTime).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate startLocal = (startDate == 0) ? LocalDate.MIN : Instant.ofEpochMilli(startDate).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocal = (endDate == 0) ? LocalDate.MAX : Instant.ofEpochMilli(endDate).atZone(ZoneId.systemDefault()).toLocalDate();

        return !orderDate.isBefore(startLocal) && !orderDate.isAfter(endLocal);
    }

    // --- HÀM SEARCH ---

    public void searchOrders(String query, int searchType) {
        if (masterOrderList == null || masterOrderList.isEmpty()) return;

        if (query == null || query.trim().isEmpty()) {
            // Reset về mặc định nếu xóa từ khóa
            filterAndSortOrders(0, null, 0, 0, null);
            return;
        }

        String finalQuery = query.toLowerCase().trim();
        List<Order> result = new ArrayList<>();

        for (Order order : masterOrderList) {
            boolean isMatch = false;
            switch (searchType) {
                case 0: // Tên khách hàng
                    if (order.getFullname() != null && order.getFullname().toLowerCase().contains(finalQuery)) isMatch = true;
                    break;
                case 1: // Số điện thoại
                    if (order.getPhone() != null && order.getPhone().contains(finalQuery)) isMatch = true;
                    break;
                case 2: // Ngày đặt
                    if (order.getDate() != null && order.getDate().contains(finalQuery)) isMatch = true;
                    break;
            }
            if (isMatch) result.add(order);
        }
        sortResultList(result, 0); // Search xong sort mới nhất
        displayedOrders.setValue(result);
    }

    // --- UTILS ---

    private void sortResultList(List<Order> list, int sortType) {
        if (list == null || list.isEmpty()) return;

        switch (sortType) {
            case 0: // Mới nhất
                Collections.sort(list, (o1, o2) -> Long.compare(parseDateToLong(o2.getDate()), parseDateToLong(o1.getDate())));
                break;
            case 1: // Cũ nhất
                Collections.sort(list, (o1, o2) -> Long.compare(parseDateToLong(o1.getDate()), parseDateToLong(o2.getDate())));
                break;
            case 2: // Giá thấp -> cao
                Collections.sort(list, (o1, o2) -> Double.compare(o1.getTotal(), o2.getTotal()));
                break;
            case 3: // Giá cao -> thấp
                Collections.sort(list, (o1, o2) -> Double.compare(o2.getTotal(), o1.getTotal()));
                break;
        }
    }

    private long parseDateToLong(String dateString) {
        if (dateString == null || dateString.isEmpty()) return 0;

        // Cách 1: Format chuẩn ISO
        try {
            Date date = isoFormat.parse(dateString);
            return date != null ? date.getTime() : 0;
        } catch (Exception e) {
            // Ignored
        }

        // Cách 2: Parse thủ công nếu dính timezone +00:00
        try {
            if (dateString.length() >= 23) {
                String cleanDate = dateString.substring(0, 23);
                SimpleDateFormat manualFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
                manualFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = manualFormat.parse(cleanDate);
                return date != null ? date.getTime() : 0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }
}