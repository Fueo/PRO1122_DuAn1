package com.example.fa25_duan1.view.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView; // <-- THÊM IMPORT
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.viewpager2.widget.ViewPager2; // <-- THÊM IMPORT
import androidx.lifecycle.ViewModelProvider;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.Auth.AuthResponse;
import com.example.fa25_duan1.view.home.HomeActivity;
import com.example.fa25_duan1.viewmodel.AuthViewModel;
import com.google.android.material.textfield.TextInputLayout;

public class LoginFragment extends Fragment {
    private AuthViewModel viewModel;
    TextInputLayout tilUsername, tilPassword;
    EditText etUsername, etPassword;
    Button btnLogin;
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
        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvSignUp = view.findViewById(R.id.tvSignUp);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);


        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            viewModel.login(username, password).observe(requireActivity(), new Observer<AuthResponse>() {
                @Override
                public void onChanged(AuthResponse authResponse) {
                    if (authResponse != null && authResponse.getAccessToken() != null && authResponse.getRefreshToken() != null) {
                        // 1. Lưu accessToken và refreshToken vào SharedPreferences
                        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("accessToken", authResponse.getAccessToken());
                        editor.putString("refreshToken", authResponse.getRefreshToken());
                        editor.apply(); // commit async

                        // 2. Thông báo đăng nhập thành công
                        Toast.makeText(requireActivity(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                        // 3. Chuyển sang HomeActivity
                        Intent intent = new Intent(getActivity(), HomeActivity.class);
                        requireActivity().startActivity(intent);
                        requireActivity().finish(); // đóng LoginActivity nếu muốn
                    } else {
                        Toast.makeText(getActivity(), "Login failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });


        tvSignUp.setOnClickListener(v -> {
            // Tìm ViewPager2 trong AuthActivity và lướt nó sang trang 1 (Register)
            ViewPager2 viewPager = requireActivity().findViewById(R.id.view_pager_auth);
            viewPager.setCurrentItem(1); // 1 là vị trí của RegisterFragment
        });
    }
}