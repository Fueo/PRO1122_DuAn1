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

import com.shashank.sony.fancytoastlib.FancyToast;
import io.github.cutelibs.cutedialog.CuteDialog;

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

        initViews(view);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        btnRegister.setOnClickListener(v -> handleRegister());

        tvLogin.setOnClickListener(v -> {
            ViewPager2 viewPager = requireActivity().findViewById(R.id.view_pager_auth);
            if (viewPager != null) viewPager.setCurrentItem(0);
        });
    }

    private void initViews(View view) {
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

        // --- HỎI XÁC NHẬN TRƯỚC KHI GỬI ---
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_confirm) // Đảm bảo bạn có icon này hoặc thay bằng icon khác
                .setTitle("Xác nhận đăng ký")
                .setDescription("Bạn có chắc chắn muốn tạo tài khoản này?")
                .setPositiveButtonText("Đăng ký", v -> {
                    performRegistration(username, password, name, email);
                })
                .setNegativeButtonText("Hủy", v -> {})
                .show();
    }

    private void performRegistration(String username, String password, String name, String email) {
        // Hiện Loading
        CuteDialog.withIcon loadingDialog = showLoadingDialog();

        // Quan sát ApiResponse
        authViewModel.register(username, password, name, email).observe(getViewLifecycleOwner(), apiResponse -> {
            loadingDialog.dismiss(); // Tắt loading

            if (apiResponse == null) {
                showErrorDialog("Lỗi kết nối hoặc phản hồi không hợp lệ.");
                return;
            }

            if (apiResponse.isStatus()) {
                // === THÀNH CÔNG ===
                showSuccessDialog();
            } else {
                // === THẤT BẠI ===
                // Lấy message từ Backend (vd: "Username đã tồn tại")
                showErrorDialog(apiResponse.getMessage());
            }
        });
    }

    // =================================================================================
    // CÁC DIALOG HIỂN THỊ
    // =================================================================================

    private CuteDialog.withIcon showLoadingDialog() {
        CuteDialog.withIcon dialog = new CuteDialog.withIcon(requireActivity())
                .setTitle("Đang xử lý...")
                .setDescription("Vui lòng đợi trong giây lát")
                .hidePositiveButton(true)
                .hideNegativeButton(true);
        dialog.show();
        return dialog;
    }

    private void showSuccessDialog() {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_success) // Đảm bảo có icon success
                .setTitle("Đăng ký thành công!")
                .setDescription("Tài khoản đã được tạo. Vui lòng đăng nhập để tiếp tục.")
                .setPositiveButtonText("Đăng nhập ngay", v -> {
                    // Chuyển sang Tab Login (Tab 0)
                    ViewPager2 viewPager = requireActivity().findViewById(R.id.view_pager_auth);
                    if (viewPager != null) {
                        viewPager.setCurrentItem(0);
                    }
                    // Reset form (tuỳ chọn)
                    clearForm();
                })
                .hideNegativeButton(true)
                .show();
    }

    private void showErrorDialog(String message) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_error) // Đảm bảo có icon error
                .setTitle("Đăng ký thất bại")
                .setDescription(message) // Hiển thị lỗi backend tại đây
                .setPositiveButtonText("Thử lại", v -> {})
                .show();
    }

    private void clearForm() {
        edtUsername.setText("");
        edtName.setText("");
        edtEmail.setText("");
        edtPassword.setText("");
        edtConfirmPassword.setText("");
        edtUsername.requestFocus();
    }

    // =================================================================================
    // VALIDATION
    // =================================================================================

    private boolean validateInput(String username, String name, String email, String password, String confirmPassword) {
        if (username.isEmpty()) {
            tilUsername.setError("Vui lòng nhập Username");
            return false;
        } else {
            tilUsername.setError(null);
        }

        if (name.isEmpty()) {
            FancyToast.makeText(requireContext(), "Vui lòng nhập Họ tên", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
            return false;
        }

        if (email.isEmpty()) {
            FancyToast.makeText(requireContext(), "Vui lòng nhập Email", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            FancyToast.makeText(requireContext(), "Email không hợp lệ", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
            return false;
        }

        if (password.isEmpty()) {
            tilPassword.setError("Nhập mật khẩu");
            return false;
        }
        if (password.length() < 6) {
            tilPassword.setError("Mật khẩu phải từ 6 ký tự");
            return false;
        } else {
            tilPassword.setError(null);
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Mật khẩu không khớp");
            return false;
        } else {
            tilConfirmPassword.setError(null);
        }

        return true;
    }
}