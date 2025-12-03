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
import java.util.TimeZone;

public class OrderViewModel extends AndroidViewModel {

    private final OrderRepository repository;

    private final MediatorLiveData<List<Order>> orderHistory = new MediatorLiveData<>();
    private LiveData<List<Order>> currentSource;

    private List<Order> masterOrderList = new ArrayList<>();
    // displayedOrders là list sẽ được bind lên RecyclerView
    private final MutableLiveData<List<Order>> displayedOrders = new MutableLiveData<>();
    private SimpleDateFormat isoFormat;

    public OrderViewModel(@NonNull Application application) {
        super(application);
        repository = new OrderRepository(application);

        // [QUAN TRỌNG] Sử dụng pattern "yyyy-MM-dd'T'HH:mm:ss.SSSX" hoặc "SSSXX" tùy API
        // "X" đại diện cho ISO 8601 timezone (+00:00 hoặc Z)
        // Lưu ý: Nếu crash ở API thấp, hãy dùng try-catch linh hoạt bên dưới
        isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public LiveData<List<Order>> getOrderHistory() { return orderHistory; }
    public LiveData<List<Order>> getDisplayedOrders() { return displayedOrders; }

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

            // Mặc định khi mới load xong thì hiển thị tất cả (không lọc)
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

    // =========================================================================
    // KHU VỰC LOGIC LỌC VÀ SẮP XẾP (ĐÃ BẬT LẠI)
    // =========================================================================

    /**
     * Hàm lọc chính.
     * @param sortType: 0=Mới nhất, 1=Cũ nhất, 2=Giá tăng, 3=Giá giảm
     * @param statusFilter: List các index trạng thái được chọn (VD: [0, 1] là Pending và Processing)
     * @param startDate: timestamp ngày bắt đầu (0 nếu không chọn)
     * @param endDate: timestamp ngày kết thúc (0 nếu không chọn)
     * @param priceRanges: List index khoảng giá được chọn
     */
    public void filterAndSortOrders(int sortType, List<Integer> statusFilter, long startDate, long endDate, List<Integer> priceRanges) {
        List<Order> result = new ArrayList<>();

        // Nếu danh sách gốc rỗng, return luôn để tránh lỗi
        if (masterOrderList == null) {
            displayedOrders.setValue(new ArrayList<>());
            return;
        }

        // 1. Duyệt qua từng đơn hàng trong danh sách gốc
        for (Order order : masterOrderList) {
            // Kiểm tra trạng thái
            boolean matchesStatus = checkStatusMatch(order.getStatus(), statusFilter);

            // Kiểm tra ngày tháng
            boolean matchesDate = checkDateMatch(order.getDate(), startDate, endDate);

            // Kiểm tra khoảng giá
            boolean matchesPrice = checkPriceMatch(order.getTotal(), priceRanges);

            // Nếu thỏa mãn TẤT CẢ các điều kiện thì thêm vào list kết quả
            if (matchesStatus && matchesDate && matchesPrice) {
                result.add(order);
            }
        }

        // 2. Sắp xếp danh sách kết quả
        sortResultList(result, sortType);

        // 3. Cập nhật lên LiveData để UI tự động thay đổi
        displayedOrders.setValue(result);
    }

    // --- CÁC HÀM CHECK LOGIC CON ---

    private boolean checkStatusMatch(String serverStatus, List<Integer> filterCodes) {
        // Nếu không chọn filter nào (null hoặc rỗng) -> coi như chọn tất cả
        if (filterCodes == null || filterCodes.isEmpty()) return true;
        if (serverStatus == null) return false;

        // Chuyển status từ server về chữ thường để so sánh (VD: "Đang xử lý" -> "đang xử lý")
        String status = serverStatus.toLowerCase().trim();

        // Duyệt qua các mã code được chọn từ UI
        for (int code : filterCodes) {
            switch (code) {
                case 0: // UI: Chờ xác nhận
                    if (status.equals("chờ xác nhận")) return true;
                    break;

                case 1: // UI: Đang xử lý / Đã xác nhận
                    // Dữ liệu của bạn là "Đang xử lý"
                    if (status.equals("đang xử lý") || status.equals("đã xác nhận")) return true;
                    break;

                case 2: // UI: Đang giao hàng
                    // Kiểm tra các từ khóa thường gặp
                    if (status.equals("đang giao hàng") || status.equals("đang vận chuyển") || status.contains("đang giao")) return true;
                    break;

                case 3: // UI: Đã giao / Hoàn thành
                    if (status.equals("đã giao hàng") || status.equals("hoàn thành") || status.equals("giao hàng thành công")) return true;
                    break;

                case 4: // UI: Đã hủy
                    // Lưu ý: chữ "hủy" và "huỷ" (u-y và u-y-?) đôi khi khác nhau trong bộ gõ, nên check cả 2 cho chắc
                    if (status.equals("đã hủy") || status.equals("đã huỷ")) return true;
                    break;
            }
        }
        return false;
    }

    private boolean checkDateMatch(String dateString, long startDate, long endDate) {
        if (startDate == 0 && endDate == 0) return true;

        long orderTime = parseDateToLong(dateString);
        if (orderTime == 0) return false;

        // Convert timestamps về LocalDate (không bị ảnh hưởng timezone)
        LocalDate orderDate = Instant.ofEpochMilli(orderTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate startLocal = Instant.ofEpochMilli(startDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate endLocal = Instant.ofEpochMilli(endDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return !(orderDate.isBefore(startLocal) || orderDate.isAfter(endLocal));
    }

    private boolean checkPriceMatch(double total, List<Integer> rangeCodes) {
        // Nếu không chọn khoảng giá nào -> lấy tất cả
        if (rangeCodes == null || rangeCodes.isEmpty()) return true;

        for (int code : rangeCodes) {
            switch (code) {
                case 0: // Dưới 5 triệu
                    if (total < 5000000) return true;
                    break;
                case 1: // 5 triệu - 10 triệu
                    if (total >= 5000000 && total < 10000000) return true;
                    break;
                case 2: // 10 triệu - 20 triệu
                    if (total >= 10000000 && total < 20000000) return true;
                    break;
                case 3: // Trên 20 triệu
                    if (total >= 20000000) return true;
                    break;
            }
        }
        return false;
    }

    // --- HÀM SEARCH (GIỮ NGUYÊN NHƯNG SỬ DỤNG LẠI LIST FILTER) ---

    public void searchOrders(String query, int searchType) {
        if (masterOrderList == null || masterOrderList.isEmpty()) return;

        if (query == null || query.trim().isEmpty()) {
            // Nếu xóa search, quay lại logic filter hiện tại (reset về mặc định)
            // Lưu ý: Nếu muốn giữ filter state (ngày, giá) khi xóa search,
            // bạn cần lưu biến state filter ra biến toàn cục class.
            // Ở đây tạm thời reset filter.
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
                case 2: // Ngày đặt (String match)
                    if (order.getDate() != null && order.getDate().contains(finalQuery)) isMatch = true;
                    break;
            }
            if (isMatch) result.add(order);
        }
        sortResultList(result, 0); // Search xong thì sort mới nhất
        displayedOrders.setValue(result);
    }

    // --- HELPER METHODS CHUNG ---

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

        // CÁCH 1: Thử parse bằng format chuẩn (cho trường hợp server trả về 'Z')
        try {
            Date date = isoFormat.parse(dateString);
            return date != null ? date.getTime() : 0;
        } catch (Exception e) {
            // Bỏ qua lỗi để chạy xuống CÁCH 2
        }

        // CÁCH 2: [FIX LỖI MÚI GIỜ] Xử lý thủ công cho trường hợp đuôi "+00:00"
        // Server trả về: 2025-12-02T00:14:16.385+00:00
        try {
            // Cắt bỏ phần đuôi timezone (+00:00), chỉ lấy phần ngày giờ (23 ký tự đầu)
            if (dateString.length() >= 23) {
                String cleanDate = dateString.substring(0, 23); // Lấy "2025-12-02T00:14:16.385"

                // Tạo format mới ép buộc là UTC
                SimpleDateFormat manualFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
                manualFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Bắt buộc hiểu đây là giờ UTC

                Date date = manualFormat.parse(cleanDate);
                return date != null ? date.getTime() : 0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public LiveData<ApiResponse<Order>> getOrderById(String orderId) {
        return repository.getOrderById(orderId);
    }
}