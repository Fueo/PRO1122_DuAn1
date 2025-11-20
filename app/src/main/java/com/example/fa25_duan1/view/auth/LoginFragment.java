package com.example.fa25_duan1.view.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox; // <--- MỚI: Import CheckBox
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.viewpager2.widget.ViewPager2;
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
    TextView tvSignUp;
    CheckBox cbRememberMe; // <--- MỚI: Khai báo biến CheckBox

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        tilUsername = view.findViewById(R.id.tilUsername);
        tilPassword = view.findViewById(R.id.tilPassword);
        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvSignUp = view.findViewById(R.id.tvSignUp);
        cbRememberMe = view.findViewById(R.id.cbRememberMe); // <--- MỚI: Ánh xạ CheckBox

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 2. XỬ LÝ TỰ ĐỘNG ĐĂNG NHẬP (AUTO LOGIN)
        // Kiểm tra ngay khi màn hình vừa mở
        checkAutoLogin();

        // 3. Sự kiện bấm nút Đăng nhập
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getActivity(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.login(username, password).observe(requireActivity(), new Observer<AuthResponse>() {
                @Override
                public void onChanged(AuthResponse authResponse) {
                    if (authResponse != null && authResponse.getAccessToken() != null && authResponse.getRefreshToken() != null) {

                        // Lưu Token và Trạng thái CheckBox vào SharedPreferences
                        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        editor.putString("accessToken", authResponse.getAccessToken());
                        editor.putString("refreshToken", authResponse.getRefreshToken());

                        // <--- MỚI: Lưu trạng thái người dùng có tick "Ghi nhớ" hay không
                        editor.putBoolean("rememberMe", cbRememberMe.isChecked());

                        editor.apply();

                        Toast.makeText(requireActivity(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                        // Chuyển trang
                        goToHomeActivity();
                    } else {
                        Toast.makeText(getActivity(), "Đăng nhập thất bại. Kiểm tra lại thông tin!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        tvSignUp.setOnClickListener(v -> {
            ViewPager2 viewPager = requireActivity().findViewById(R.id.view_pager_auth);
            if (viewPager != null) {
                viewPager.setCurrentItem(1);
            }
        });
    }

    // <--- MỚI: Hàm kiểm tra tự động đăng nhập
    private void checkAutoLogin() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        // Lấy trạng thái rememberMe (mặc định là false nếu chưa lưu)
        boolean isRemembered = sharedPreferences.getBoolean("rememberMe", false);
        // Lấy token
        String accessToken = sharedPreferences.getString("accessToken", null);

        // Nếu người dùng đã chọn Ghi nhớ mật khẩu VÀ đã có Token
        if (isRemembered && accessToken != null) {
            goToHomeActivity();
        }
    }

    // <--- MỚI: Hàm chuyển trang để dùng chung (tránh viết lại code lặp)
    private void goToHomeActivity() {
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        requireActivity().startActivity(intent);
        requireActivity().finish();
    }
}