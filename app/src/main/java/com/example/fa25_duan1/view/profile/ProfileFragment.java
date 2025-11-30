package com.example.fa25_duan1.view.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.view.detail.DetailActivity;
import com.example.fa25_duan1.viewmodel.AuthViewModel;

public class ProfileFragment extends Fragment {
    TextView tvName, tvRole, tvEmail, tvAddress, tvChangePassword;
    ImageView ivProfile, btnEdit;
    AuthViewModel authViewModel;
    View view;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        initViews(view);

        // 2. Khởi tạo ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 3. Xử lý sự kiện Click
        btnEdit.setOnClickListener(v -> {
            openActivity("Thông tin cá nhân", "updateinfo");
        });

        tvChangePassword.setOnClickListener(v -> {
            openActivity("Thay đổi mật khẩu", "changepassword");
        });

        tvAddress.setOnClickListener(v -> {
            openActivity("Sổ địa chỉ", "address");
        });

        // Lưu ý: Không gọi observe dữ liệu ở đây nữa, vì onResume sẽ lo việc đó.
    }

    // --- THÊM HÀM onResume ---
    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }

    // --- HÀM RIÊNG ĐỂ LOAD DỮ LIỆU ---
    private void loadUserProfile() {
        // Xóa các observer cũ để tránh bị double observer khi onResume chạy nhiều lần
        if (authViewModel.getMyInfo() != null) {
            authViewModel.getMyInfo().removeObservers(getViewLifecycleOwner());
        }

        authViewModel.getMyInfo().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getData() != null) {
                User user = response.getData();
                updateUI(user); // Gọi hàm cập nhật giao diện
            } else {
                // Có thể check thêm nếu response lỗi
                Toast.makeText(requireContext(), "Đang cập nhật...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Tách hàm update UI cho gọn code
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
        tvName = view.findViewById(R.id.tvName);
        tvRole = view.findViewById(R.id.tvRole);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvAddress = view.findViewById(R.id.tvAddress);
        ivProfile = view.findViewById(R.id.ivProfile);
        tvChangePassword = view.findViewById(R.id.tvChangePassword);
        btnEdit = view.findViewById(R.id.btnEdit);
    }

    private void openActivity(String header, String content) {
        Intent intent = new Intent(view.getContext(), DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, header);
        intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, content);
        startActivity(intent);
    }
}