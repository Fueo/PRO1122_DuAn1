package com.example.fa25_duan1.view.profile;

import android.app.AlertDialog;
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

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryFragment extends Fragment {

    private RecyclerView rvOrders;
    private View layoutEmpty; // 1. Khai báo biến cho layout trống
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

        // 2. Ánh xạ View
        rvOrders = view.findViewById(R.id.rvOrders);
        layoutEmpty = view.findViewById(R.id.layout_empty_cart); // Ánh xạ layout empty

        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setHasFixedSize(true);

        // 3. Khởi tạo ViewModel
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);

        // 4. Setup Adapter
        orderAdapter = new OrderAdapter(getContext(), orderList, new OrderAdapter.OnOrderActionListener() {
            @Override
            public void onCancelOrder(String orderId) {
                showConfirmCancelDialog(orderId);
            }
        });
        rvOrders.setAdapter(orderAdapter);

        // 5. Lắng nghe dữ liệu
        setupObservers();

        // 6. Gọi API lấy danh sách
        orderViewModel.fetchOrderHistory();
    }

    private void setupObservers() {
        // Lắng nghe danh sách đơn hàng trả về
        orderViewModel.getOrderHistory().observe(getViewLifecycleOwner(), orders -> {
            // Luôn clear list cũ trước khi add mới
            orderList.clear();

            if (orders != null) {
                orderList.addAll(orders);
            }
            orderAdapter.notifyDataSetChanged();

            // 7. Logic kiểm tra hiển thị layout empty
            if (orderList.isEmpty()) {
                rvOrders.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            } else {
                rvOrders.setVisibility(View.VISIBLE);
                layoutEmpty.setVisibility(View.GONE);
            }
        });

        // Lắng nghe thông báo lỗi/thành công
        orderViewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showConfirmCancelDialog(String orderId) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận hủy đơn")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Đồng ý hủy", (dialog, which) -> {
                    orderViewModel.cancelOrder(orderId);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (orderViewModel != null) {
            orderViewModel.fetchOrderHistory();
        }
    }
}