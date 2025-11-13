package com.example.fa25_duan1.view.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // <-- THÊM IMPORT
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2; // <-- THÊM IMPORT

import com.example.fa25_duan1.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterFragment extends Fragment {

    TextInputLayout tilUsername, tilPassword, tilConfirmPassword;
    MaterialButton btnRegister;
    TextView tvLogin; // <-- THÊM BIẾN NÀY

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ
        tilUsername = view.findViewById(R.id.tilUsername);
        tilPassword = view.findViewById(R.id.tilPassword);
        tilConfirmPassword = view.findViewById(R.id.tilConfirmPassword);
        btnRegister = view.findViewById(R.id.btnRegister);
        tvLogin = view.findViewById(R.id.tvLogin); // <-- THÊM DÒNG NÀY

        // Xử lý sự kiện nút Đăng ký
        btnRegister.setOnClickListener(v -> {
            // ... (code xử lý đăng ký của bạn)
            Toast.makeText(getContext(), "Đăng ký...", Toast.LENGTH_SHORT).show();
        });

        // --- THÊM SỰ KIỆN CLICK CHO tvLogin ---
        tvLogin.setOnClickListener(v -> {
            // Tìm ViewPager2 trong AuthActivity và lướt nó sang trang 0 (Login)
            ViewPager2 viewPager = requireActivity().findViewById(R.id.view_pager_auth);
            viewPager.setCurrentItem(0); // 0 là vị trí của LoginFragment
        });
    }
}