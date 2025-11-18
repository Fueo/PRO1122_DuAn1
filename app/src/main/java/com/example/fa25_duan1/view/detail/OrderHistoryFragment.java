package com.example.fa25_duan1.view.detail;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.OrderAdapter;
import com.example.fa25_duan1.model.Order;
import com.example.fa25_duan1.model.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryFragment extends Fragment {

    private RecyclerView rvOrders;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    RelativeLayout rlProfile;
    RelativeLayout rlHistory;
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_purchasehistory, container, false);
    }
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvOrders = view.findViewById(R.id.rvOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setHasFixedSize(true);

        fakeData(); // tạo dữ liệu demo

        orderAdapter = new OrderAdapter(getContext(), orderList);
        rvOrders.setAdapter(orderAdapter);
    }
    private void fakeData() {
        orderList = new ArrayList<>();

        // Tạo list sản phẩm demo
        List<OrderItem> items1 = new ArrayList<>();
        items1.add(new OrderItem("Và Rồi Chẳng Còn Ai...", 1, "76.000 VND", 0));
        items1.add(new OrderItem("Và Rồi Chẳng Còn Ai...", 1, "75.000 VND", 0));

        Order order1 = new Order(
                "6915dbd3ad9de604d9ece8a1",
                "Thanh toán khi nhận hàng",
                "150.000 đ",
                "processing",
                "9:42 11/10/2025",
                "120 Yên Lãng, Phường Đống Đa, Hà Nội",
                "0933 444 336",
                items1
        );

        List<OrderItem> items2 = new ArrayList<>(items1); // cho nhanh
        Order order2 = new Order(
                "2",
                "Chuyển khoản",
                "150.000 đ",
                "done",
                "9:41 11/10/2025",
                "120 Yên Lãng, Phường Đống Đa, Hà Nội",
                "0933 444 336",
                items2
        );

        List<OrderItem> items3 = new ArrayList<>(items1);
        Order order3 = new Order(
                "3",
                "Chuyển khoản",
                "150.000 đ",
                "canceled",
                "9:40 11/10/2025",
                "120 Yên Lãng, Phường Đống Đa, Hà Nội",
                "0933 444 336",
                items3
        );

        orderList.add(order1);
        orderList.add(order2);
        orderList.add(order3);
    }
}
