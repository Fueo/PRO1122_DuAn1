package com.example.fa25_duan1.view.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.OrderAdapter;
import com.example.fa25_duan1.model.Order;
import com.example.fa25_duan1.model.OrderDetail;
import com.example.fa25_duan1.view.detail.DetailActivity;
import com.example.fa25_duan1.viewmodel.OrderViewModel;
import com.example.fa25_duan1.viewmodel.CartViewModel;

import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.List;

import io.github.cutelibs.cutedialog.CuteDialog;

public class OrderHistoryFragment extends Fragment {

    private RecyclerView rvOrders;
    private View layoutEmpty;
    private OrderAdapter orderAdapter;
    private List<Order> orderList = new ArrayList<>();

    private OrderViewModel orderViewModel;
    private CartViewModel cartViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_purchasehistory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvOrders = view.findViewById(R.id.rvOrders);
        layoutEmpty = view.findViewById(R.id.layout_empty_cart);

        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setHasFixedSize(true);

        // Khởi tạo ViewModel
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        // Dùng requireActivity để CartViewModel sống chung với Activity (giữ data giỏ hàng toàn cục)
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        orderAdapter = new OrderAdapter(getContext(), orderList, new OrderAdapter.OnOrderActionListener() {
            @Override
            public void onCancelOrder(String orderId) {
                showConfirmCancelDialog(orderId);
            }

            @Override
            public void onRepurchase(Order order) {
                handleRepurchaseOrder(order);
            }

            @Override
            public void onViewDetail(Order order) {
                openViewActivity(order);
            }
        });
        rvOrders.setAdapter(orderAdapter);

        setupObservers();
        orderViewModel.fetchOrderHistory();
    }

    private void setupObservers() {
        orderViewModel.getOrderHistory().observe(getViewLifecycleOwner(), orders -> {
            orderList.clear();
            if (orders != null) {
                orderList.addAll(orders);
            }
            orderAdapter.notifyDataSetChanged();

            if (orderList.isEmpty()) {
                rvOrders.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            } else {
                rvOrders.setVisibility(View.VISIBLE);
                layoutEmpty.setVisibility(View.GONE);
            }
        });
    }

    // =========================================================================
    // LOGIC ĐẶT LẠI ĐƠN HÀNG (REPURCHASE)
    // =========================================================================

    private void handleRepurchaseOrder(Order order) {
        FancyToast.makeText(getContext(), "Đang xử lý đặt lại đơn...", FancyToast.LENGTH_SHORT, FancyToast.INFO, false).show();

        // Bước 1: Xóa giỏ hàng hiện tại (Bất kể thành công hay thất bại)
        observeOnce(cartViewModel.clearCart(), isCleared -> {
            prepareAndAddItems(order);
        });
    }

    private void prepareAndAddItems(Order order) {
        if (order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
            FancyToast.makeText(getContext(), "Đơn hàng không có sản phẩm nào!", FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
            return;
        }

        // Lọc ra các sản phẩm hợp lệ
        List<OrderDetail> itemsToAdd = new ArrayList<>();
        for (OrderDetail item : order.getOrderDetails()) {
            if (item.getProduct() != null && item.getProduct().getId() != null) {
                itemsToAdd.add(item);
            }
        }

        if (itemsToAdd.isEmpty()) {
            FancyToast.makeText(getContext(), "Sản phẩm trong đơn không còn tồn tại!", FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
            return;
        }

        // Bước 2: Bắt đầu thêm từng sản phẩm (Đệ quy)
        addSingleItemRecursive(itemsToAdd, 0);
    }

    /**
     * Hàm đệ quy thêm từng sản phẩm vào giỏ để đảm bảo đồng bộ và bắt lỗi chính xác
     */
    private void addSingleItemRecursive(List<OrderDetail> items, int index) {
        // ĐIỀU KIỆN DỪNG: Đã thêm hết danh sách
        if (index >= items.size()) {
            onRepurchaseSuccess();
            return;
        }

        OrderDetail currentItem = items.get(index);
        String productId = String.valueOf(currentItem.getProduct().getId());
        int quantity = currentItem.getQuantity();

        // Gọi ViewModel thêm sản phẩm
        // (Lưu ý: CartViewModel cần có hàm addToCart(id, qty) trả về LiveData<ApiResponse>)
        observeOnce(cartViewModel.addToCart(productId, quantity), response -> {
            // Kiểm tra kết quả từ Backend
            if (response != null && response.isStatus()) {
                // Thành công -> Tiếp tục thêm sản phẩm tiếp theo
                addSingleItemRecursive(items, index + 1);
            } else {
                // Thất bại (VD: Hết hàng) -> Dừng lại và báo lỗi ngay
                String errorMsg = (response != null && response.getMessage() != null)
                        ? response.getMessage()
                        : "Lỗi khi thêm sản phẩm: " + currentItem.getProduct().getName();
                showErrorDialog(errorMsg);
            }
        });
    }

    private void onRepurchaseSuccess() {
        // Refresh để cập nhật lại badge/số lượng
        cartViewModel.refreshCart();

        FancyToast.makeText(getContext(), "Đã thêm đơn hàng vào giỏ!", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();

        // Chuyển sang màn hình Giỏ hàng
        Intent intent = new Intent(getContext(), DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Giỏ hàng");
        intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "cart");
        startActivity(intent);
        // requireActivity().finish(); // Tùy chọn nếu muốn đóng màn hình hiện tại
    }

    private void showErrorDialog(String message) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_error) // Đảm bảo có icon này
                .setTitle("Không thể thêm vào giỏ")
                .setDescription(message)
                .setPrimaryColor(R.color.red) // Đảm bảo có màu red
                .setPositiveButtonColor(R.color.red)
                .setPositiveButtonText("Đóng", v -> {})
                .hideNegativeButton(true)
                .show();
    }

    // =========================================================================
    // LOGIC HỦY ĐƠN & XEM CHI TIẾT
    // =========================================================================

    private void showConfirmCancelDialog(String orderId) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_confirm)
                .setTitle("Xác nhận hủy đơn")
                .setDescription("Bạn có chắc chắn muốn hủy đơn hàng này không?")
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setPositiveButtonText("Xác nhận", v -> performCancelOrder(orderId))
                .setNegativeButtonText("Hủy", v -> {})
                .show();
    }

    private void performCancelOrder(String orderId) {
        orderViewModel.cancelOrder(orderId).observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isStatus()) {
                FancyToast.makeText(getContext(), "Đã hủy đơn hàng thành công", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();
                orderViewModel.fetchOrderHistory();
            } else {
                String msg = (response != null) ? response.getMessage() : "Lỗi khi hủy đơn";
                FancyToast.makeText(getContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
            }
        });
    }

    private void openViewActivity(Order order) {
        if (order == null) return;

        Intent intent = new Intent(getContext(), DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Chi tiết đơn hàng");
        intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "order");
        intent.putExtra("orderId", order.getId());
        startActivity(intent);
    }

    // Helper: Observer dùng 1 lần rồi tự hủy
    private <T> void observeOnce(LiveData<T> liveData, Observer<T> observer) {
        liveData.observe(getViewLifecycleOwner(), new Observer<T>() {
            @Override
            public void onChanged(T t) {
                liveData.removeObserver(this);
                observer.onChanged(t);
            }
        });
    }
}