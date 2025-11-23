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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CartFragment extends Fragment implements CartAdapter.OnCartItemClickListener {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private ArrayList<CartItem> cartItems;

    private TextView tvSubtotal, tvShippingFee, tvTotal;
    private MaterialButton btnBack, btnContinue;

    // Các View điều khiển ẩn/hiện
    private View layoutEmptyCart;
    private View scrollArea;
    private View bottomBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View (Đã kiểm tra khớp với XML)
        recyclerView = view.findViewById(R.id.recycler_view_cart);
        tvSubtotal = view.findViewById(R.id.tv_subtotal);
        tvShippingFee = view.findViewById(R.id.tv_shipping_fee);
        tvTotal = view.findViewById(R.id.tv_total);
        btnBack = view.findViewById(R.id.btn_back);
        btnContinue = view.findViewById(R.id.btn_continue);

        layoutEmptyCart = view.findViewById(R.id.layout_empty_cart);
        scrollArea = view.findViewById(R.id.scroll_area);
        bottomBar = view.findViewById(R.id.bottom_bar); // ID này đã khớp với XML mới

        // 2. Tạo dữ liệu mẫu
        cartItems = new ArrayList<>();
        cartItems.add(new CartItem("1", "Và Rồi Chẳng Còn Ai", 75000, 1, "https://picsum.photos/200"));
        cartItems.add(new CartItem("2", "Harry Potter", 120000, 2, "https://picsum.photos/201"));
        cartItems.add(new CartItem("3", "Đắc Nhân Tâm", 95000, 1, "https://picsum.photos/202"));

        // 3. Setup RecyclerView
        if (getContext() != null) {
            cartAdapter = new CartAdapter(getContext(), cartItems, this);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(cartAdapter);
        }

        // 4. Cập nhật giao diện
        updateOrderSummary();
        checkEmptyState();

        // 5. Xử lý sự kiện
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        btnContinue.setOnClickListener(v -> {
            if (cartItems != null && !cartItems.isEmpty()) {
                Intent intent = new Intent(requireContext(), DetailActivity.class);
                intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Trang thanh toán");
                intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "checkout");
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkEmptyState() {
        if (cartItems == null || cartItems.isEmpty()) {
            if (layoutEmptyCart != null) layoutEmptyCart.setVisibility(View.VISIBLE);
            if (scrollArea != null) scrollArea.setVisibility(View.GONE);
            if (bottomBar != null) bottomBar.setVisibility(View.GONE);
        } else {
            if (layoutEmptyCart != null) layoutEmptyCart.setVisibility(View.GONE);
            if (scrollArea != null) scrollArea.setVisibility(View.VISIBLE);
            if (bottomBar != null) bottomBar.setVisibility(View.VISIBLE);
        }
    }

    private void updateOrderSummary() {
        // Format tiền tệ Việt Nam (dùng dấu chấm phân cách hàng nghìn)
        DecimalFormat formatter = new DecimalFormat("###,###,###");

        if (cartItems == null || cartItems.isEmpty()) {
            tvSubtotal.setText("0 đ");
            tvShippingFee.setText("0 đ");
            tvTotal.setText("0 đ");
            return;
        }

        long subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += (long) item.getPrice() * item.getQuantity();
        }
        long shippingFee = 25000;
        long total = subtotal + shippingFee;

        // Thay thế dấu phẩy mặc định bằng dấu chấm nếu cần (hoặc dùng Locale)
        // Cách đơn giản nhất để ra 300.000 đ:
        String strSubtotal = formatter.format(subtotal).replace(",", ".") + " đ";
        String strShipping = formatter.format(shippingFee).replace(",", ".") + " đ";
        String strTotal = formatter.format(total).replace(",", ".") + " đ";

        tvSubtotal.setText(strSubtotal);
        tvShippingFee.setText(strShipping);
        tvTotal.setText(strTotal);
    }

    // ------------------- Interface Implementation -------------------

    @Override
    public void onIncreaseClick(CartItem item, int position) {
        if (item != null) {
            item.setQuantity(item.getQuantity() + 1);
            if (cartAdapter != null) cartAdapter.notifyItemChanged(position);
            updateOrderSummary();
        }
    }

    @Override
    public void onDecreaseClick(CartItem item, int position) {
        if (item != null && item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            if (cartAdapter != null) cartAdapter.notifyItemChanged(position);
            updateOrderSummary();
        }
    }

    @Override
    public void onDeleteClick(CartItem item, int position) {
        if (cartItems != null && position >= 0 && position < cartItems.size()) {
            cartItems.remove(position);
            if (cartAdapter != null) {
                cartAdapter.notifyItemRemoved(position);
                cartAdapter.notifyItemRangeChanged(position, cartItems.size());
            }
            updateOrderSummary();
            checkEmptyState();
        }
    }
}