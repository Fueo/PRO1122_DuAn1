package com.example.fa25_duan1.view.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.fa25_duan1.view.auth.AuthActivity; // Import AuthActivity
import com.example.fa25_duan1.view.detail.DetailActivity;
import com.example.fa25_duan1.viewmodel.CartViewModel;

import io.github.cutelibs.cutedialog.CuteDialog; // Import CuteDialog

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

    // --- Cập nhật lại giỏ hàng khi quay lại màn hình này (Resume) ---
    @Override
    public void onResume() {
        super.onResume();
        // Chỉ refresh giỏ hàng nếu ĐÃ ĐĂNG NHẬP
        if (cartViewModel != null && !isGuestUser()) {
            cartViewModel.refreshCart();
        }
    }

    private void setupCartBadge() {
        // Khởi tạo ViewModel (Scope Activity)
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        // --- OBSERVE ITEM ĐỂ HIỆN BADGE ---
        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), items -> {
            // items.size() là số loại sản phẩm.
            int count = (items == null) ? 0 : items.size();

            if (count > 0) {
                tvBadge.setVisibility(View.VISIBLE);
                tvBadge.setText(String.valueOf(count));
            } else {
                tvBadge.setVisibility(View.GONE);
            }
        });

        // Chỉ gọi API nếu không phải là khách
        if (!isGuestUser()) {
            cartViewModel.refreshCart();
        } else {
            // Nếu là khách, ẩn badge đi
            tvBadge.setVisibility(View.GONE);
        }

        // [MỚI] Xử lý click icon giỏ hàng (Có check Guest)
        flCart.setOnClickListener(v -> {
            if (isGuestUser()) {
                // Nếu là khách -> Hiện Dialog bắt đăng nhập
                showLoginRequiredDialog();
            } else {
                // Nếu đã đăng nhập -> Vào giỏ hàng bình thường
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
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_info)
                .setTitle("Yêu cầu đăng nhập")
                .setDescription("Bạn cần đăng nhập để xem giỏ hàng và thanh toán.")
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
        if (getActivity() == null) return;

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