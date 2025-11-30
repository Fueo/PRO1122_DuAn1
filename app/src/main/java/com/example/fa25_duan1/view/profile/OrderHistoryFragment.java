package com.example.fa25_duan1.view.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.OrderAdapter;
import com.example.fa25_duan1.model.Order;
import com.example.fa25_duan1.viewmodel.OrderViewModel;
import com.shashank.sony.fancytoastlib.FancyToast; // Thêm FancyToast

import java.util.ArrayList;
import java.util.List;

import io.github.cutelibs.cutedialog.CuteDialog;

public class OrderHistoryFragment extends Fragment {

    private RecyclerView rvOrders;
    private View layoutEmpty;
    private OrderAdapter orderAdapter;
    private List<Order> orderList = new ArrayList<>();
    private OrderViewModel orderViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_purchasehistory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        rvOrders = view.findViewById(R.id.rvOrders);
        layoutEmpty = view.findViewById(R.id.layout_empty_cart);

        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setHasFixedSize(true);

        // 2. Khởi tạo ViewModel
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);

        // 3. Setup Adapter
        orderAdapter = new OrderAdapter(getContext(), orderList, new OrderAdapter.OnOrderActionListener() {
            @Override
            public void onCancelOrder(String orderId) {
                showConfirmCancelDialog(orderId);
            }
        });
        rvOrders.setAdapter(orderAdapter);

        // 4. Lắng nghe dữ liệu
        setupObservers();

        // 5. Gọi API lấy danh sách
        orderViewModel.fetchOrderHistory();
    }

    private void setupObservers() {
        // Chỉ lắng nghe danh sách đơn hàng trả về (Dữ liệu thụ động)
        orderViewModel.getOrderHistory().observe(getViewLifecycleOwner(), orders -> {
            orderList.clear();

            if (orders != null) {
                orderList.addAll(orders);
            }
            orderAdapter.notifyDataSetChanged();

            // Logic kiểm tra hiển thị layout empty
            if (orderList.isEmpty()) {
                rvOrders.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            } else {
                rvOrders.setVisibility(View.VISIBLE);
                layoutEmpty.setVisibility(View.GONE);
            }
        });

        // SỬA: Đã XÓA observer orderViewModel.getMessage() tại đây
    }

    private void showConfirmCancelDialog(String orderId) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_confirm)
                .setTitle("Xác nhận hủy đơn")
                .setDescription("Bạn có chắc chắn muốn hủy đơn hàng này không? Hành động này không thể hoàn tác.")
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.gray_text)
                .setPositiveButtonText("Xác nhận", v -> {
                    // GỌI HÀM HỦY ĐƠN (LOGIC MỚI)
                    performCancelOrder(orderId);
                })
                .setNegativeButtonText("Hủy", v -> {})
                .show();
    }

    // --- LOGIC HỦY ĐƠN MỚI ---
    private void performCancelOrder(String orderId) {
        // Gọi ViewModel và lắng nghe kết quả trực tiếp
        orderViewModel.cancelOrder(orderId).observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isStatus()) {
                // 1. Thành công
                FancyToast.makeText(getContext(), "Đã hủy đơn hàng thành công", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();

                // 2. Refresh lại danh sách
                orderViewModel.fetchOrderHistory();
            } else {
                // 3. Thất bại
                String msg = (response != null) ? response.getMessage() : "Lỗi khi hủy đơn";
                FancyToast.makeText(getContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (orderViewModel != null) {
            orderViewModel.fetchOrderHistory();
        }
    }
}