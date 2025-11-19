package com.example.fa25_duan1.view.auth;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.view.dialog.ConfirmDialogFragment;
import com.example.fa25_duan1.view.dialog.NotificationDialogFragment;
import com.example.fa25_duan1.viewmodel.AuthViewModel;
// import com.example.fa25_duan1.model.Auth.AuthResponse; // Cần import nếu AuthResponse có getMessage()
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterFragment extends Fragment {

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

        // Ánh xạ (Giữ nguyên)
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

        // Khởi tạo ViewModel
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        // Xử lý sự kiện nút Đăng ký
        btnRegister.setOnClickListener(v -> {
            handleRegister();
        });

        tvLogin.setOnClickListener(v -> {
            ViewPager2 viewPager = requireActivity().findViewById(R.id.view_pager_auth);
            if (viewPager != null) {
                viewPager.setCurrentItem(0);
            }
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

        ConfirmDialogFragment dialog = new ConfirmDialogFragment(
                "Xác nhận đăng ký",
                "Bạn có chắc chắn muốn đăng ký tài khoản này?",
                () -> {
                    performRegistration(username, password, name, email);
                }
        );

        dialog.show(getParentFragmentManager(), "ConfirmDialog");
    }

    private boolean validateInput(String username, String name, String email, String password, String confirmPassword) {
        // Đặt lại trạng thái lỗi (Giữ nguyên)
        tilUsername.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        edtName.setError(null);
        edtEmail.setError(null);

        boolean isValid = true;

        if (username.isEmpty()) {
            tilUsername.setError("Tên đăng nhập không được để trống.");
            isValid = false;
        }

        if (name.isEmpty()) {
            edtName.setError("Họ tên không được để trống.");
            isValid = false;
        }

        if (email.isEmpty()) {
            edtEmail.setError("Email không được để trống.");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ.");
            isValid = false;
        }

        if (password.isEmpty()) {
            tilPassword.setError("Mật khẩu không được để trống.");
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự.");
            isValid = false;
        }

        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError("Xác nhận mật khẩu không được để trống.");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Mật khẩu xác nhận không khớp.");
            isValid = false;
        }

        return isValid;
    }

    private void performRegistration(String username, String password, String name, String email) {

        // Gọi hàm register từ AuthViewModel
        authViewModel.register(username, password, name, email).observe(getViewLifecycleOwner(), response -> {
            if (response != null) {
                if (response.getMessage() != null) {
                    showSuccessDialog();
                } else {
                    String errorMessage = "Lỗi đăng ký không xác định.";
                    try {

                    } catch (Exception e) {

                    }
                    showErrorToast("Đăng ký thất bại: " + errorMessage);
                }
            }
            else {
                showErrorToast("Lỗi kết nối hoặc phản hồi không hợp lệ từ server.");
            }
        });
    }

    private void showSuccessDialog() {
        NotificationDialogFragment dialogFragment = NotificationDialogFragment.newInstance(
                "Thành công",
                "Bạn đã đăng ký thành công! Vui lòng đăng nhập.",
                "Đăng nhập",
                NotificationDialogFragment.TYPE_SUCCESS,
                () -> {
                    ViewPager2 viewPager = requireActivity().findViewById(R.id.view_pager_auth);
                    if (viewPager != null) {
                        viewPager.setCurrentItem(0);
                    }
                }
        );
        dialogFragment.show(getParentFragmentManager(), "SuccessDialog");
    }

    private void showErrorToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }
}