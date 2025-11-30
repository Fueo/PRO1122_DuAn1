package com.example.fa25_duan1.view.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.view.auth.AuthActivity;
import com.example.fa25_duan1.view.detail.DetailActivity;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.viewmodel.AuthViewModel;

// --- IMPORT THƯ VIỆN UI MỚI ---
import com.shashank.sony.fancytoastlib.FancyToast;
import io.github.cutelibs.cutedialog.CuteDialog;

public class UserFragment extends Fragment {
    LinearLayout rlProfile, rlHistory;
    TextView tvName, tvRole, tvEmail;
    Button btnLogout;
    ImageView ivProfile;
    AuthViewModel authViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        initViews(view);

        // 2. Khởi tạo ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 3. Xử lý sự kiện Click
        rlProfile.setOnClickListener(v -> {
            Intent intent = new Intent(view.getContext(), DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Trang cá nhân");
            intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "profile");
            startActivity(intent);
        });

        rlHistory.setOnClickListener(v -> {
            Intent intent = new Intent(view.getContext(), DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Lịch sử mua hàng");
            intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "orderhistory");
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            showLogoutDialogConfirm();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    // --- HÀM LOAD DỮ LIỆU ---
    private void loadUserData() {
        if (authViewModel.getMyInfo() != null) {
            authViewModel.getMyInfo().removeObservers(getViewLifecycleOwner());
        }

        authViewModel.getMyInfo().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getData() != null) {
                User user = response.getData();
                updateUI(user);
            } else {
                // Có thể thêm Toast lỗi nhẹ nếu cần, hoặc để trống
            }
        });
    }

    // --- HÀM CẬP NHẬT GIAO DIỆN ---
    private void updateUI(User user) {
        String role = user.getRole() == 0 ? "Khách hàng" : user.getRole() == 1 ? "Nhân viên" : "Admin";

        tvName.setText(user.getName());
        tvRole.setText(role);
        tvEmail.setText(user.getEmail());

        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            Glide.with(requireActivity())
                    .load(user.getAvatar())
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
                    .into(ivProfile);
        } else {
            ivProfile.setImageResource(R.drawable.ic_avatar_placeholder);
        }
    }

    private void initViews(View view) {
        rlProfile = view.findViewById(R.id.rlProfile);
        rlHistory = view.findViewById(R.id.rlHistory);
        btnLogout = view.findViewById(R.id.btnLogout);
        tvName = view.findViewById(R.id.tvName);
        tvRole = view.findViewById(R.id.tvRole);
        tvEmail = view.findViewById(R.id.tvEmail);
        ivProfile = view.findViewById(R.id.ivProfile);
    }

    // --- CÁC HÀM XỬ LÝ LOGOUT ---
    private void handleLogoutSuccess(SharedPreferences sharedPref) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("accessToken");
        editor.remove("refreshToken");
        editor.remove("rememberMe");
        editor.apply();

        // Thay Toast thường bằng FancyToast SUCCESS
        FancyToast.makeText(requireContext(),
                "Đăng xuất thành công!",
                FancyToast.LENGTH_SHORT,
                FancyToast.SUCCESS,
                true).show();

        startAuthActivity(0);
        requireActivity().finish();
    }

    private void startAuthActivity(int tab) {
        Intent intent = new Intent(requireActivity(), AuthActivity.class);
        intent.putExtra("DEFAULT_TAB", tab);
        startActivity(intent);
    }

    private void showLogoutDialogConfirm() {
        // --- SỬ DỤNG CUTEDIALOG (ĐĂNG XUẤT) ---
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_info) // Nên dùng icon logout nếu có (R.drawable.ic_logout)
                .setTitle("Đăng xuất")
                .setDescription("Bạn có chắc chắn muốn đăng xuất khỏi ứng dụng?")

                // --- CẤU HÌNH MÀU SẮC (BLUE) ---
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.gray_text)

                .setPositiveButtonText("Đăng xuất", v -> {
                    performLogout();
                })
                .setNegativeButtonText("Hủy", v -> {
                    // Đóng dialog
                })
                .show();
    }

    private void performLogout() {
        SharedPreferences sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String refreshToken = sharedPref.getString("refreshToken", null);

        if (refreshToken != null) {
            authViewModel.logout(refreshToken).observe(getViewLifecycleOwner(), response -> {
                if (response != null) {
                    handleLogoutSuccess(sharedPref);
                } else {
                    // Thay Toast lỗi bằng FancyToast WARNING
                    FancyToast.makeText(requireContext(),
                            "Lỗi mạng, đăng xuất cục bộ.",
                            FancyToast.LENGTH_SHORT,
                            FancyToast.WARNING,
                            true).show();

                    handleLogoutSuccess(sharedPref);
                }
            });
        } else {
            handleLogoutSuccess(sharedPref);
        }
    }
}