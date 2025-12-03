package com.example.fa25_duan1.view.management.order;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.OrderManageAdapter;
import com.example.fa25_duan1.model.Order;
import com.example.fa25_duan1.view.management.UpdateActivity; // Import UpdateActivity
import com.example.fa25_duan1.viewmodel.OrderViewModel;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.List;

import io.github.cutelibs.cutedialog.CuteDialog;

public class OrderManageFragment extends Fragment {

    private View layoutEmpty;
    private RecyclerView rvData;
    private OrderManageAdapter adapter;
    private OrderViewModel viewModel;

    // [GIỐNG ORDER HISTORY] Khởi tạo list cục bộ để quản lý dữ liệu
    private List<Order> listOrders = new ArrayList<>();

    // Request code để nhận biết khi quay lại từ màn hình update
    private static final int REQUEST_CODE_UPDATE_ORDER = 1002;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ordermanagement, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Nhúng OrderFilterFragment
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragment_filter, new OrderFilterFragment())
                    .commit();
        }

        // 2. Init Views
        rvData = view.findViewById(R.id.rvData);
        layoutEmpty = view.findViewById(R.id.layout_empty);

        // 3. Setup RecyclerView
        rvData.setLayoutManager(new LinearLayoutManager(getContext()));
        rvData.setHasFixedSize(true);

        adapter = new OrderManageAdapter(getContext(), listOrders, new OrderManageAdapter.OnOrderClickListener() {
            @Override
            public void onOrderClick(Order order) {
                openUpdateActivity(order);
            }
        });
        rvData.setAdapter(adapter);

        // 4. Init ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(OrderViewModel.class);
        setupObservers();
    }

    private void openUpdateActivity(Order order) {
        if (order == null) return;

        Intent intent = new Intent(getContext(), UpdateActivity.class);
        intent.putExtra(UpdateActivity.EXTRA_HEADER_TITLE, "Chi tiết đơn hàng");
        intent.putExtra(UpdateActivity.EXTRA_CONTENT_FRAGMENT, "order");

        // --- CHỈ TRUYỀN ID STRING (Tránh lỗi Serializable) ---
        // Kiểm tra trong Model Order của bạn hàm lấy id là getId() hay get_id()
        intent.putExtra("orderId", order.getId());

        startActivityForResult(intent, REQUEST_CODE_UPDATE_ORDER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_UPDATE_ORDER) {
            if (viewModel != null) {
                viewModel.fetchAllOrdersForAdmin();
            }
        }
    }

    private void setupObservers() {
        viewModel.getDisplayedOrders().observe(getViewLifecycleOwner(), orders -> {
            listOrders.clear();
            if (orders != null) listOrders.addAll(orders);
            adapter.notifyDataSetChanged();

            if (listOrders.isEmpty()) {
                rvData.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            } else {
                rvData.setVisibility(View.VISIBLE);
                layoutEmpty.setVisibility(View.GONE);
            }
        });
        viewModel.getOrderHistory().observe(getViewLifecycleOwner(), new Observer<List<Order>>() {
            @Override
            public void onChanged(List<Order> orders) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) viewModel.fetchAllOrdersForAdmin();
    }
}