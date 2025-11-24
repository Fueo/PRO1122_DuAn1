package com.example.fa25_duan1.view.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.view.cart.CartFragment;
import com.example.fa25_duan1.view.detail.DetailActivity;
import com.example.fa25_duan1.viewmodel.CartViewModel;

public class HeaderHomeFragment extends Fragment {

    private FrameLayout flCart;
    private EditText etSearch;
    private CartViewModel cartViewModel;
    private TextView tvBadge;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_header, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        flCart = view.findViewById(R.id.fl_cart_container);
        etSearch = view.findViewById(R.id.et_search);
        tvBadge = view.findViewById(R.id.tv_badge);

        // 1. Setup Badge và gọi dữ liệu lần đầu
        setupCartBadge();

        // 2. Xử lý tìm kiếm
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {

                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    performSearch(query);
                }
                return true;
            }
            return false;
        });
    }

    // --- QUAN TRỌNG: Cập nhật lại giỏ hàng khi quay lại màn hình này ---
    @Override
    public void onResume() {
        super.onResume();
        if (cartViewModel != null) {
            // Gọi hàm fetchCart() theo đúng tên trong ViewModel của bạn
            cartViewModel.fetchCart();
        }
    }

    private void setupCartBadge() {
        // Khởi tạo ViewModel
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        // --- SỬA LỖI ĐẾM BADGE ---
        // Thay vì observe getTotalQuantity (cộng dồn số lượng), hãy observe getCartItems (danh sách item)
        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), items -> {
            // items.size() chính là số lượng đầu sản phẩm (unique product)
            int count = (items == null) ? 0 : items.size();

            if (count > 0) {
                tvBadge.setVisibility(View.VISIBLE);
                tvBadge.setText(String.valueOf(count));
            } else {
                tvBadge.setVisibility(View.GONE);
            }
        });

        // Gọi API lấy giỏ hàng ngay lập tức
        cartViewModel.fetchCart();

        // Xử lý click icon giỏ hàng
        flCart.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Giỏ hàng");
            intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "cart");
            startActivity(intent);
        });
    }

    private void performSearch(String query) {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        ProductFragment productFragment = new ProductFragment();
        Bundle args = new Bundle();
        args.putString("search_query", query);
        productFragment.setArguments(args);

        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).loadFragment(productFragment, true);
        }
    }
}