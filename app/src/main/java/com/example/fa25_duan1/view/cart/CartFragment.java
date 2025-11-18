package com.example.fa25_duan1.view.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.CartAdapter;
import com.example.fa25_duan1.model.CartItem;
import com.example.fa25_duan1.view.detail.DetailActivity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class CartFragment extends Fragment implements CartAdapter.OnCartItemClickListener {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private ArrayList<CartItem> cartItems;

    private TextView tvSubtotal, tvShippingFee, tvTotal;
    private MaterialButton btnBack, btnContinue;

    // Khai báo thêm các View để điều khiển ẩn/hiện
    private View layoutEmptyCart; // Layout rỗng (được include)
    private View scrollArea;      // Vùng nội dung chính (ScrollView)
    private View bottomBar;       // Thanh button bên dưới

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        recyclerView = view.findViewById(R.id.recycler_view_cart);
        tvSubtotal = view.findViewById(R.id.tv_subtotal);
        tvShippingFee = view.findViewById(R.id.tv_shipping_fee);
        tvTotal = view.findViewById(R.id.tv_total);
        btnBack = view.findViewById(R.id.btn_back);
        btnContinue = view.findViewById(R.id.btn_continue);

        // Ánh xạ các view layout
        layoutEmptyCart = view.findViewById(R.id.layout_empty_cart);
        scrollArea = view.findViewById(R.id.scroll_area);
        bottomBar = view.findViewById(R.id.bottom_bar);

        // 2. Tạo dữ liệu mẫu (Bạn có thể thay bằng logic lấy từ DB/API)
        cartItems = new ArrayList<>();
        cartItems.add(new CartItem("1", "Và Rồi Chẳng Còn Ai", 75000, 1, "https://picsum.photos/200"));
        cartItems.add(new CartItem("2", "Harry Potter và Hòn Đá Phù Thủy", 120000, 2, "https://picsum.photos/201"));
        cartItems.add(new CartItem("3", "Đắc Nhân Tâm", 95000, 1, "https://picsum.photos/202"));

        // 3. Setup RecyclerView
        cartAdapter = new CartAdapter(getContext(), cartItems, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(cartAdapter);

        // 4. Cập nhật giao diện lần đầu
        updateOrderSummary();
        checkEmptyState(); // <--- Kiểm tra rỗng ngay khi vào màn hình

        // 5. Xử lý sự kiện nút bấm
        btnBack.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        btnContinue.setOnClickListener(v -> {
            // Chỉ cho đi tiếp nếu giỏ hàng có đồ
            if (cartItems != null && !cartItems.isEmpty()) {
                Intent intent = new Intent(view.getContext(), DetailActivity.class);
                intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Trang thanh toán");
                intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "checkout");
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Hàm kiểm tra danh sách để ẩn hiện giao diện tương ứng
     */
    private void checkEmptyState() {
        if (cartItems == null || cartItems.isEmpty()) {
            // Nếu rỗng: Hiện layout Empty, Ẩn nội dung chính & bottom bar
            layoutEmptyCart.setVisibility(View.VISIBLE);
            scrollArea.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);
        } else {
            // Nếu có hàng: Ẩn layout Empty, Hiện nội dung chính & bottom bar
            layoutEmptyCart.setVisibility(View.GONE);
            scrollArea.setVisibility(View.VISIBLE);
            bottomBar.setVisibility(View.VISIBLE);
        }
    }

    // Cập nhật tổng tiền
    private void updateOrderSummary() {
        if (cartItems.isEmpty()) {
            tvSubtotal.setText("0 VNĐ");
            tvShippingFee.setText("0 VNĐ");
            tvTotal.setText("0 VNĐ");
            return;
        }

        int subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getPrice() * item.getQuantity();
        }
        int shippingFee = 25000; // Ví dụ phí cố định
        int total = subtotal + shippingFee;

        tvSubtotal.setText(String.format("%,.0f VNĐ", (double)subtotal));
        tvShippingFee.setText(String.format("%,.0f VNĐ", (double)shippingFee));
        tvTotal.setText(String.format("%,.0f VNĐ", (double)total));
    }

    // ------------------- CartAdapter.OnCartItemClickListener -------------------
    @Override
    public void onIncreaseClick(CartItem item, int position) {
        item.setQuantity(item.getQuantity() + 1);
        cartAdapter.notifyItemChanged(position);
        updateOrderSummary();
    }

    @Override
    public void onDecreaseClick(CartItem item, int position) {
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            cartAdapter.notifyItemChanged(position);
            updateOrderSummary();
        }
    }

    @Override
    public void onDeleteClick(CartItem item, int position) {
        cartItems.remove(position);
        cartAdapter.notifyItemRemoved(position);

        // Cập nhật lại range để tránh lỗi index khi xóa item giữa list
        cartAdapter.notifyItemRangeChanged(position, cartItems.size());

        updateOrderSummary();
        checkEmptyState(); // <--- Quan trọng: Kiểm tra lại sau khi xóa item
    }
}