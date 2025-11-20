package com.example.fa25_duan1.view.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.view.auth.AuthActivity;
import com.example.fa25_duan1.view.detail.DetailActivity;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.view.dialog.ConfirmDialogFragment;
import com.example.fa25_duan1.view.welcome.WelcomeActivity;
import com.example.fa25_duan1.viewmodel.AuthViewModel;

public class UserFragment extends Fragment {
    RelativeLayout rlProfile, rlHistory;
    LinearLayout rlLogout;
    TextView tvName, tvRole, tvPhone, tvEmail;
    ImageView ivProfile;
    AuthViewModel authViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rlProfile = view.findViewById(R.id.rlProfile);
        rlHistory = view.findViewById(R.id.rlHistory);
        rlLogout = view.findViewById(R.id.rlLogout);
        tvName = view.findViewById(R.id.tvName);
        tvRole = view.findViewById(R.id.tvRole);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvEmail = view.findViewById(R.id.tvEmail);
        ivProfile = view.findViewById(R.id.ivProfile);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        authViewModel.getMyInfo().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getData() != null) {
                User user = response.getData();
                String role = user.getRole() == 0 ? "Khách hàng" : user.getRole() == 1 ? "Nhân viên" : "Admin";

                tvName.setText(user.getName());
                tvRole.setText(role);
                tvEmail.setText(user.getEmail());
                tvPhone.setText(
                        (user.getPhone() == null || user.getPhone().isEmpty())
                                ? "Chưa cập nhật"
                                : user.getPhone()
                );

                if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                    Glide.with(requireActivity())
                            .load(user.getAvatar())
                            .placeholder(R.drawable.ic_avatar_placeholder)
                            .error(R.drawable.ic_avatar_placeholder)
                            .into(ivProfile);
                } else {
                    ivProfile.setImageResource(R.drawable.ic_avatar_placeholder);
                }

            } else {
                Toast.makeText(requireContext(), "Không lấy được thông tin", Toast.LENGTH_SHORT).show();
            }
        });


        rlProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), DetailActivity.class);
                intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Trang cá nhân");
                intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "profile");
                startActivity(intent);
            }
        });

        rlHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), DetailActivity.class);
                intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Lịch sử mua hàng");
                intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "orderhistory");
                startActivity(intent);
            }
        });

        rlLogout.setOnClickListener(v -> {
            showLogoutDialogConfirm();
        });
    }

    // --- PHẦN QUAN TRỌNG ĐÃ SỬA ---
    private void handleLogoutSuccess(SharedPreferences sharedPref) {
        SharedPreferences.Editor editor = sharedPref.edit();

        // 1. Xóa Token
        editor.remove("accessToken");
        editor.remove("refreshToken");

        // 2. QUAN TRỌNG: Xóa trạng thái "Ghi nhớ mật khẩu"
        // Để lần sau mở app, nó sẽ không tự động đăng nhập nữa
        editor.remove("rememberMe");

        editor.apply();

        Toast.makeText(requireContext(), "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
        startAuthActivity(0);
        requireActivity().finish();
    }
    // ------------------------------

    private void startAuthActivity(int tab) {
        Intent intent = new Intent(requireActivity(), AuthActivity.class);
        intent.putExtra("DEFAULT_TAB", tab);
        startActivity(intent);
    }

    private void showLogoutDialogConfirm() {
        ConfirmDialogFragment dialog = new ConfirmDialogFragment(
                "Xác nhận đăng xuất?",
                "",
                new ConfirmDialogFragment.OnConfirmListener() {
                    @Override
                    public void onConfirmed() {
                        // 1. Lấy Refresh Token từ SharedPreferences
                        SharedPreferences sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        String refreshToken = sharedPref.getString("refreshToken", null);

                        if (refreshToken != null) {
                            // 2. Gọi hàm logout qua ViewModel
                            authViewModel.logout(refreshToken).observe(getViewLifecycleOwner(), response -> {
                                if (response != null) {
                                    // Đăng xuất thành công hoặc API trả về 200/204
                                    handleLogoutSuccess(sharedPref);
                                } else {
                                    // Xử lý lỗi nhưng vẫn logout cục bộ để tránh kẹt
                                    Toast.makeText(requireContext(), "Đã xảy ra lỗi khi đăng xuất. Đang thực hiện đăng xuất cục bộ.", Toast.LENGTH_LONG).show();
                                    handleLogoutSuccess(sharedPref);
                                }
                            });
                        } else {
                            // Không có Refresh Token, thực hiện đăng xuất cục bộ ngay lập tức
                            handleLogoutSuccess(sharedPref);
                        }
                    }
                }
        );

        dialog.show(getParentFragmentManager(), "ConfirmDialog");
    }
}