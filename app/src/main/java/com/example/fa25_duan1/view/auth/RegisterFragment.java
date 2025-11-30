package com.example.fa25_duan1.view.auth;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.viewmodel.AuthViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

// IMPORT THƯ VIỆN MỚI
import com.shashank.sony.fancytoastlib.FancyToast;
// Lưu ý: Package name có thể khác tùy version, hãy để IDE tự gợi ý import (Alt+Enter)
import io.github.cutelibs.cutedialog.CuteDialog;

public class RegisterFragment extends Fragment {

    // ... (Khai báo biến giữ nguyên) ...
    TextInputLayout tilUsername, tilPassword, tilConfirmPassword;
    EditText edtUsername, edtName, edtEmail, edtPassword, edtConfirmPassword;
    MaterialButton btnRegister;
    AuthViewModel authViewModel;
    TextView tvLogin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // ... (Ánh xạ view giữ nguyên) ...
        tilUsername = view.findViewById(R.id.tilUsername);
        tilPassword = view.findViewById(R.id.tilPassword);
        tilConfirmPassword = view.findViewById(R.id.tilConfirmPassword);
        edtUsername = view.findViewById(R.id.edtUsername);
        edtName = view.findViewById(R.id.edtName);
        edtEmail = view.findViewById(R.id.edtEmail);
        edtPassword = view.findViewById(R.id.edtPassword);
        edtConfirmPassword = view.findViewById(R.id.edtConfirmPassword);
        btnRegister = view.findViewById(R.id.btnRegister);
        tvLogin = view.findViewById(R.id.tvLogin);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        btnRegister.setOnClickListener(v -> handleRegister());

        tvLogin.setOnClickListener(v -> {
            ViewPager2 viewPager = requireActivity().findViewById(R.id.view_pager_auth);
            if (viewPager != null) viewPager.setCurrentItem(0);
        });
    }

    private void handleRegister() {
        String username = edtUsername.getText().toString().trim();
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String confirmPassword = edtConfirmPassword.getText().toString();

        if (!validateInput(username, name, email, password, confirmPassword)) {
            return;
        }

        // --- SỬ DỤNG CUTEDIALOG (XÁC NHẬN) ---
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_confirm)
                .setTitle("Xác nhận đăng ký")
                .setDescription("Bạn có chắc chắn muốn đăng ký tài khoản này?")
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.gray_text)

                .setPositiveButtonText("Đăng ký", v -> {
                    performRegistration(username, password, name, email);
                })
                .setNegativeButtonText("Hủy", v -> {
                    // Tự động đóng
                })
                .show();
    }

    private void performRegistration(String username, String password, String name, String email) {
        authViewModel.register(username, password, name, email).observe(getViewLifecycleOwner(), response -> {
            if (response != null) {
                if (response.getMessage() != null) {
                    showSuccessDialog();
                } else {
                    showErrorToast("Đăng ký thất bại: Phản hồi không xác định.");
                }
            } else {
                showErrorToast("Lỗi kết nối hoặc phản hồi không hợp lệ từ server.");
            }
        });
    }

    private void showSuccessDialog() {
        // --- SỬ DỤNG CUTEDIALOG (THÀNH CÔNG) ---
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_success)
                .setTitle("Thành công")
                .setDescription("Bạn đã đăng ký thành công! Vui lòng đăng nhập để tiếp tục.")
                .setPositiveButtonText("Đăng nhập", v -> {
                    ViewPager2 viewPager = requireActivity().findViewById(R.id.view_pager_auth);
                    if (viewPager != null) {
                        viewPager.setCurrentItem(0);
                    }
                })
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.gray_text)
                .hideNegativeButton(true)
                .show();
    }
    private void showErrorToast(String message) {
        FancyToast.makeText(
                requireContext(),
                message,
                FancyToast.LENGTH_LONG,
                FancyToast.ERROR,
                true
        ).show();
    }

    // ... (Hàm validateInput giữ nguyên) ...
    private boolean validateInput(String username, String name, String email, String password, String confirmPassword) {
        // ... Code validate cũ của bạn ...
        return true; // (Ví dụ)
    }
}