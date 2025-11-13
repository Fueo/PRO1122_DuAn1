package com.example.fa25_duan1.view.auth;

import android.content.Intent;
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
import com.example.fa25_duan1.view.home.HomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

public class LoginFragment extends Fragment {

    TextInputLayout tilUsername, tilPassword;
    MaterialButton btnLogin;
    TextView tvSignUp; // <-- THÊM BIẾN NÀY

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tilUsername = view.findViewById(R.id.tilUsername);
        tilPassword = view.findViewById(R.id.tilPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvSignUp = view.findViewById(R.id.tvSignUp); // <-- THÊM DÒNG NÀY

        btnLogin.setOnClickListener(v -> {
            // ... (code xử lý đăng nhập của bạn)
            Toast.makeText(getContext(), "Đăng nhập...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), HomeActivity.class);
            getActivity().startActivity(intent);
        });

        tvSignUp.setOnClickListener(v -> {
            // Tìm ViewPager2 trong AuthActivity và lướt nó sang trang 1 (Register)
            ViewPager2 viewPager = requireActivity().findViewById(R.id.view_pager_auth);
            viewPager.setCurrentItem(1); // 1 là vị trí của RegisterFragment
        });
    }
}