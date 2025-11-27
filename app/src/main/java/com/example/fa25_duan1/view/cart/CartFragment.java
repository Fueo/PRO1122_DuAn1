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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.CartAdapter;
import com.example.fa25_duan1.model.CartItem;
import com.example.fa25_duan1.view.detail.DetailActivity;
import com.example.fa25_duan1.viewmodel.CartViewModel;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class CartFragment extends Fragment implements CartAdapter.OnCartItemClickListener {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private ArrayList<CartItem> cartItemsList = new ArrayList<>();
    private CartViewModel cartViewModel;

    private TextView tvSubtotal, tvShippingFee, tvTotal;
    private MaterialButton btnBack, btnContinue;
    private View layoutEmptyCart, scrollArea, bottomBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Dùng requireActivity để đồng bộ số lượng badge
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        initViews(view);
        setupRecyclerView();
        setupObservers();
        setupButtons();

        // Gọi API
        cartViewModel.fetchCart();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_cart);
        tvSubtotal = view.findViewById(R.id.tv_subtotal);
        tvShippingFee = view.findViewById(R.id.tv_shipping_fee);
        tvTotal = view.findViewById(R.id.tv_total);
        btnBack = view.findViewById(R.id.btn_back);
        btnContinue = view.findViewById(R.id.btn_continue);
        layoutEmptyCart = view.findViewById(R.id.layout_empty_cart);
        scrollArea = view.findViewById(R.id.scroll_area);
        bottomBar = view.findViewById(R.id.bottom_bar);
    }

    private void setupRecyclerView() {
        if (getContext() != null) {
            cartAdapter = new CartAdapter(getContext(), cartItemsList, this);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(cartAdapter);
        }
    }

    private void setupObservers() {
        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), items -> {
            cartItemsList.clear();
            if (items != null) {
                cartItemsList.addAll(items);
            }
            cartAdapter.notifyDataSetChanged();
            checkEmptyState();
        });

        cartViewModel.getTotalPrice().observe(getViewLifecycleOwner(), this::updateOrderSummary);

        // Hiển thị thông báo lỗi (ví dụ: Hết hàng)
        cartViewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOrderSummary(long subtotal) {
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        long shippingFee = (subtotal > 0) ? 25000 : 0;
        long total = subtotal + shippingFee;

        tvSubtotal.setText(formatter.format(subtotal).replace(",", ".") + " đ");
        tvShippingFee.setText(formatter.format(shippingFee).replace(",", ".") + " đ");
        tvTotal.setText(formatter.format(total).replace(",", ".") + " đ");
    }

    private void checkEmptyState() {
        if (cartItemsList.isEmpty()) {
            layoutEmptyCart.setVisibility(View.VISIBLE);
            scrollArea.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);
        } else {
            layoutEmptyCart.setVisibility(View.GONE);
            scrollArea.setVisibility(View.VISIBLE);
            bottomBar.setVisibility(View.VISIBLE);
        }
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        btnContinue.setOnClickListener(v -> {
            if (cartItemsList.isEmpty()) {
                Toast.makeText(requireContext(), "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            cartViewModel.checkCartAvailability().observe(getViewLifecycleOwner(), isValid -> {
                if (Boolean.TRUE.equals(isValid)) {
                    // 1. Nếu hợp lệ (Đủ hàng) -> Chuyển trang
                    Intent intent = new Intent(requireContext(), DetailActivity.class);
                    intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Trang thanh toán");
                    intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "checkout");
                    startActivity(intent);
                } else {
                    // 2. Nếu không hợp lệ (Hết hàng/Lỗi) -> Báo lỗi & Load lại giỏ
                    Toast.makeText(requireContext(),
                            "Một số sản phẩm đã hết hàng hoặc không đủ số lượng. Vui lòng kiểm tra lại!",
                            Toast.LENGTH_LONG).show();

                    // Gọi fetchCart để cập nhật lại giao diện (User sẽ thấy số lượng tồn kho thực tế)
                    cartViewModel.fetchCart();
                }
            });
        });
    }

    // --- XỬ LÝ SỰ KIỆN TĂNG / GIẢM ---

    @Override
    public void onIncreaseClick(CartItem item, int position) {
        if (item.getProduct() != null) {
            // Gọi hàm tăng, nếu hết hàng ViewModel sẽ báo lỗi qua Toast
            cartViewModel.increaseQuantity(item.getProduct().getId());
        }
    }

    @Override
    public void onDecreaseClick(CartItem item, int position) {
        if (item.getProduct() != null) {
            if (item.getQuantity() > 1) {
                // Gọi API giảm
                cartViewModel.decreaseQuantity(item.getProduct().getId());
            } else {
                Toast.makeText(getContext(), "Số lượng tối thiểu là 1. Hãy bấm nút Xóa nếu muốn bỏ sản phẩm.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDeleteClick(CartItem item, int position) {
        cartViewModel.deleteItem(item.getId());
    }
}