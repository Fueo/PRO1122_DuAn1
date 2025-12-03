package com.example.fa25_duan1.view.auth;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import androidx.lifecycle.ViewModelProvider;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.view.home.HomeActivity;
import com.example.fa25_duan1.viewmodel.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.shashank.sony.fancytoastlib.FancyToast;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import io.github.cutelibs.cutedialog.CuteDialog;

public class LoginFragment extends Fragment {
    private AuthViewModel authViewModel;
    TextInputLayout tilUsername, tilPassword;
    EditText etUsername, etPassword;
    Button btnLogin;
    TextView tvSignUp, tvForgotPassword;
    CheckBox cbRememberMe;

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
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);
        cbRememberMe = view.findViewById(R.id.cbRememberMe);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        checkAutoLogin();

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (username.isEmpty() || password.isEmpty()) {
                FancyToast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin!", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
                return;
            }
            authViewModel.login(username, password).observe(requireActivity(), authResponse -> {
                if (authResponse != null && authResponse.getAccessToken() != null) {
                    String userId = getUserIdFromToken(authResponse.getAccessToken());
                    SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("accessToken", authResponse.getAccessToken());
                    editor.putString("refreshToken", authResponse.getRefreshToken());
                    editor.putString("userId", userId);
                    editor.putBoolean("rememberMe", cbRememberMe.isChecked());
                    editor.apply();

                    FancyToast.makeText(requireContext(), "Đăng nhập thành công!", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
                    goToHomeActivity();
                } else {
                    showLoginErrorDialog();
                }
            });
        });

        tvSignUp.setOnClickListener(v -> {
            ViewPager2 viewPager = requireActivity().findViewById(R.id.view_pager_auth);
            if (viewPager != null) viewPager.setCurrentItem(1);
        });

        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    // --- BƯỚC 1: NHẬP EMAIL ---
    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_dialog_forgot_pass, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        TextInputEditText etE = dialogView.findViewById(R.id.etForgotEmail);
        TextInputEditText etU = dialogView.findViewById(R.id.etForgotUsername);
        Button btnSend = dialogView.findViewById(R.id.btnSendCode);

        btnSend.setOnClickListener(v -> {
            String email = etE.getText().toString().trim();
            String username = etU.getText().toString().trim();

            if (email.isEmpty() || username.isEmpty()) {
                FancyToast.makeText(requireContext(), "Nhập đủ thông tin!", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
                return;
            }

            authViewModel.forgotPassword(username, email).observe(getViewLifecycleOwner(), result -> {
                if ("OK".equals(result)) {
                    dialog.dismiss();
                    FancyToast.makeText(requireContext(), "Đã gửi mã OTP về Email!", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
                    showOtpStepDialog(email);
                } else {
                    FancyToast.makeText(requireContext(), result, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                }
            });
        });
    }

    // --- BƯỚC 2: NHẬP OTP ---
    private void showOtpStepDialog(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_dialog_verify_otp, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);
        dialog.show();

        EditText etOtp = dialogView.findViewById(R.id.etOtpInput);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmOtp);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelOtp);
        btnConfirm.setText("Tiếp tục");

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String otp = etOtp.getText().toString().trim();
            if (otp.length() < 6) {
                FancyToast.makeText(requireContext(), "Nhập đủ 6 số OTP!", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
                return;
            }

            authViewModel.checkOtpForgot(email, otp).observe(getViewLifecycleOwner(), res -> {
                if ("OK".equals(res)) {
                    dialog.dismiss();
                    showNewPasswordStepDialog(email, otp);
                } else {
                    // Nếu vẫn lỗi kết nối thì check logcat, nhưng sau khi sửa router ở trên thì sẽ hết
                    FancyToast.makeText(requireContext(), res, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                }
            });
        });
    }

    // --- BƯỚC 3: NHẬP MẬT KHẨU MỚI ---
    private void showNewPasswordStepDialog(String email, String confirmedOtp) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_dialog_new_pass, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);
        dialog.show();

        TextInputEditText etNewPass = dialogView.findViewById(R.id.etNewPass);
        TextInputEditText etConfirmPass = dialogView.findViewById(R.id.etConfirmPass);
        Button btnChange = dialogView.findViewById(R.id.btnChangePass);

        btnChange.setOnClickListener(v -> {
            String newPass = etNewPass.getText().toString().trim();
            String confirmPass = etConfirmPass.getText().toString().trim();

            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                FancyToast.makeText(requireContext(), "Vui lòng nhập đầy đủ!", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
                return;
            }
            if (newPass.length() < 6) {
                FancyToast.makeText(requireContext(), "Mật khẩu phải từ 6 ký tự!", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
                return;
            }
            if (!newPass.equals(confirmPass)) {
                FancyToast.makeText(requireContext(), "Mật khẩu xác nhận không khớp!", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                return;
            }

            authViewModel.resetPassword(email, confirmedOtp, newPass).observe(getViewLifecycleOwner(), res -> {
                if ("OK".equals(res)) {
                    dialog.dismiss();
                    FancyToast.makeText(requireContext(), "Đổi mật khẩu thành công! Hãy đăng nhập ngay.", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, true).show();
                } else {
                    FancyToast.makeText(requireContext(), res, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                }
            });
        });
    }

    private String getUserIdFromToken(String token) {
        try {
            String[] split = token.split("\\.");
            String body = getJson(split[1]);
            JSONObject jsonObject = new JSONObject(body);
            return jsonObject.getString("id");
        } catch (Exception e) { return ""; }
    }
    private String getJson(String strEncoded) throws UnsupportedEncodingException {
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }
    private void showLoginErrorDialog() {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_error)
                .setTitle("Đăng nhập thất bại")
                .setDescription("Sai tài khoản hoặc mật khẩu.")
                .setPositiveButtonText("Thử lại", v -> {})
                .show();
    }
    private void checkAutoLogin() {
        SharedPreferences sp = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        if (sp.getBoolean("rememberMe", false) && sp.getString("accessToken", null) != null) goToHomeActivity();
    }
    private void goToHomeActivity() {
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        requireActivity().startActivity(intent);
        requireActivity().finish();
    }
}