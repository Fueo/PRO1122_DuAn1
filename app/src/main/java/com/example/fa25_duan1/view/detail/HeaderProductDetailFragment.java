package com.example.fa25_duan1.view.detail;

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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.view.home.HomeActivity;
import com.example.fa25_duan1.viewmodel.CartViewModel;

public class HeaderProductDetailFragment extends Fragment {

    private ImageView iv_back;
    private EditText etSearch;
    private FrameLayout flCart; // Mới thêm
    private TextView tvBadge;   // Mới thêm
    private CartViewModel cartViewModel; // Mới thêm

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Đảm bảo layout fragment_header_detail đã có fl_cart_container và tv_badge
        return inflater.inflate(R.layout.fragment_header_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        iv_back = view.findViewById(R.id.iv_back);
        etSearch = view.findViewById(R.id.et_search);
        flCart = view.findViewById(R.id.fl_cart_container); // Copy từ HeaderHome
        tvBadge = view.findViewById(R.id.tv_badge);         // Copy từ HeaderHome

        // 2. Xử lý nút Back
        iv_back.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        // 3. Setup Badge giỏ hàng (Logic giống hệt HeaderHome)
        setupCartBadge();

        // 4. Xử lý sự kiện tìm kiếm
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

    // --- QUAN TRỌNG: Cập nhật lại giỏ hàng khi quay lại màn hình này (Resume) ---
    @Override
    public void onResume() {
        super.onResume();
        if (cartViewModel != null) {
            cartViewModel.fetchCart();
        }
    }

    private void setupCartBadge() {
        // Khởi tạo ViewModel (Lưu ý: Scope là Activity chứa Fragment này)
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        // --- LOGIC ĐẾM BADGE CHUẨN (Dùng .size() thay vì quantity) ---
        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), items -> {
            int count = (items == null) ? 0 : items.size();

            if (count > 0) {
                tvBadge.setVisibility(View.VISIBLE);
                tvBadge.setText(String.valueOf(count));
            } else {
                tvBadge.setVisibility(View.GONE);
            }
        });

        // Gọi API lấy dữ liệu ngay lập tức
        cartViewModel.fetchCart();

        // Xử lý click icon giỏ hàng -> Chuyển về HomeActivity và mở Fragment Cart
        flCart.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Giỏ hàng");
            intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "cart");
            startActivity(intent);
        });
    }

    private void performSearch(String query) {
        // Ẩn bàn phím
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // Chuyển về HomeActivity và mở Fragment Product với từ khóa search
        navigateToHome("product", query);
    }

    // Hàm chung để chuyển hướng về HomeActivity
    private void navigateToHome(String targetFragment, String searchQuery) {
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        intent.putExtra("target_fragment", targetFragment); // "cart" hoặc "product"

        if (!searchQuery.isEmpty()) {
            intent.putExtra("search_query", searchQuery);
        }

        // Cờ này giúp quay lại Activity cũ thay vì tạo mới chồng lên,
        // hoặc tạo mới nếu chưa có (tuỳ logic app của bạn)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}