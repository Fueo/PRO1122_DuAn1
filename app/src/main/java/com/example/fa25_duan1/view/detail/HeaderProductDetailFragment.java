package com.example.fa25_duan1.view.detail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences; // Import SharedPreferences
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
import com.example.fa25_duan1.view.auth.AuthActivity; // Import AuthActivity
import com.example.fa25_duan1.view.home.HomeActivity;
import com.example.fa25_duan1.viewmodel.CartViewModel;

import io.github.cutelibs.cutedialog.CuteDialog; // Import CuteDialog

public class HeaderProductDetailFragment extends Fragment {

    private ImageView iv_back;
    private EditText etSearch;
    private FrameLayout flCart;
    private TextView tvBadge;
    private CartViewModel cartViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_header_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        iv_back = view.findViewById(R.id.iv_back);
        etSearch = view.findViewById(R.id.et_search);
        flCart = view.findViewById(R.id.fl_cart_container);
        tvBadge = view.findViewById(R.id.tv_badge);

        // 2. Xử lý nút Back
        iv_back.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        // 3. Setup Badge giỏ hàng
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

    // --- Cập nhật lại giỏ hàng khi quay lại màn hình này (Resume) ---
    @Override
    public void onResume() {
        super.onResume();
        // [SỬA] Chỉ gọi refresh nếu không phải là Guest
        if (cartViewModel != null && !isGuestUser()) {
            cartViewModel.refreshCart();
        }
    }

    private void setupCartBadge() {
        // Khởi tạo ViewModel (Scope: Activity để chia sẻ dữ liệu chung)
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        // --- OBSERVE LIST ITEM ĐỂ HIỆN BADGE ---
        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), items -> {
            int count = (items == null) ? 0 : items.size();

            if (count > 0) {
                tvBadge.setVisibility(View.VISIBLE);
                tvBadge.setText(String.valueOf(count));
            } else {
                tvBadge.setVisibility(View.GONE);
            }
        });

        // [SỬA] Logic gọi API và hiển thị Badge
        if (!isGuestUser()) {
            cartViewModel.refreshCart();
        } else {
            // Nếu là Guest, ẩn Badge đi để tránh hiển thị sai
            tvBadge.setVisibility(View.GONE);
        }

        // [SỬA] Xử lý click icon giỏ hàng (Check Guest)
        flCart.setOnClickListener(v -> {
            if (isGuestUser()) {
                // Nếu là Guest -> Hiện dialog bắt đăng nhập
                showLoginRequiredDialog();
            } else {
                // Nếu User -> Vào giỏ hàng
                Intent intent = new Intent(getContext(), DetailActivity.class);
                intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Giỏ hàng");
                intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "cart");
                startActivity(intent);
            }
        });
    }

    // --- [MỚI] HÀM KIỂM TRA GUEST ---
    private boolean isGuestUser() {
        if (getActivity() == null) return true;
        SharedPreferences prefs = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("accessToken", null);
        return token == null; // Không có token -> Là Guest
    }

    // --- [MỚI] DIALOG YÊU CẦU ĐĂNG NHẬP ---
    private void showLoginRequiredDialog() {
        if (getActivity() == null) return;

        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_info)
                .setTitle("Yêu cầu đăng nhập")
                .setDescription("Bạn cần đăng nhập để xem giỏ hàng và mua sắm.")
                .setPositiveButtonText("Đăng nhập", v -> {
                    // Chuyển sang màn hình Auth
                    Intent intent = new Intent(requireActivity(), AuthActivity.class);
                    // Clear task để user không bấm back về lại được
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButtonText("Để sau", v -> {})
                .show();
    }

    private void performSearch(String query) {
        // Ẩn bàn phím
        if (getActivity() == null) return;
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        navigateToHome("product", query);
    }

    private void navigateToHome(String targetFragment, String searchQuery) {
        if (getActivity() == null) return;

        Intent intent = new Intent(getActivity(), HomeActivity.class);
        intent.putExtra("target_fragment", targetFragment);

        if (!searchQuery.isEmpty()) {
            intent.putExtra("search_query", searchQuery);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}