package com.example.fa25_duan1.view.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
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
import androidx.lifecycle.Observer;
import androidx.viewpager2.widget.ViewPager2;
import androidx.lifecycle.ViewModelProvider;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.Auth.AuthResponse;
import com.example.fa25_duan1.view.home.HomeActivity;
import com.example.fa25_duan1.viewmodel.AuthViewModel;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

// IMPORT THƯ VIỆN UI MỚI
import com.shashank.sony.fancytoastlib.FancyToast;
import io.github.cutelibs.cutedialog.CuteDialog;

public class LoginFragment extends Fragment {
    private AuthViewModel viewModel;
    TextInputLayout tilUsername, tilPassword;
    EditText etUsername, etPassword;
    Button btnLogin;
    TextView tvSignUp;
    CheckBox cbRememberMe;

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
        cbRememberMe = view.findViewById(R.id.cbRememberMe);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 2. XỬ LÝ TỰ ĐỘNG ĐĂNG NHẬP
        checkAutoLogin();

        // 3. Sự kiện bấm nút Đăng nhập
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                FancyToast.makeText(requireContext(),
                        "Vui lòng nhập đầy đủ thông tin!",
                        FancyToast.LENGTH_SHORT,
                        FancyToast.WARNING,
                        true).show();
                return;
            }

            viewModel.login(username, password).observe(requireActivity(), new Observer<AuthResponse>() {
                @Override
                public void onChanged(AuthResponse authResponse) {
                    if (authResponse != null && authResponse.getAccessToken() != null) {

                        // --- QUAN TRỌNG: GIẢI MÃ TOKEN ĐỂ LẤY USER ID ---
                        String userId = getUserIdFromToken(authResponse.getAccessToken());
                        // -----------------------------------------------

                        // Lưu Token và UserId vào SharedPreferences
                        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        editor.putString("accessToken", authResponse.getAccessToken());
                        editor.putString("refreshToken", authResponse.getRefreshToken());

                        // Lưu userId để dùng cho việc so sánh quyền sửa/xóa sau này
                        editor.putString("userId", userId);

                        editor.putBoolean("rememberMe", cbRememberMe.isChecked());
                        editor.apply();

                        // SỬ DỤNG FANCY TOAST (SUCCESS)
                        FancyToast.makeText(requireContext(),
                                "Đăng nhập thành công!",
                                FancyToast.LENGTH_SHORT,
                                FancyToast.SUCCESS,
                                true).show();

                        goToHomeActivity();
                    } else {
                        // SỬ DỤNG CUTEDIALOG (ERROR)
                        showLoginErrorDialog();
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

    // --- HÀM GIẢI MÃ JWT ĐỂ LẤY ID ---
    private String getUserIdFromToken(String token) {
        try {
            // Token JWT có 3 phần tách nhau bởi dấu chấm: Header.Payload.Signature
            String[] split = token.split("\\.");
            // Payload nằm ở phần thứ 2 (index 1)
            String body = getJson(split[1]);

            // Parse JSON để lấy trường "id" (Backend bạn đặt là { id: user._id, ... })
            JSONObject jsonObject = new JSONObject(body);
            return jsonObject.getString("id");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // Hàm decode Base64
    private String getJson(String strEncoded) throws UnsupportedEncodingException {
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }
    // ----------------------------------

    private void showLoginErrorDialog() {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_error)
                .setTitle("Đăng nhập thất bại")
                .setDescription("Tài khoản hoặc mật khẩu không chính xác. Vui lòng kiểm tra lại.")
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.gray_text)
                .setPositiveButtonText("Thử lại", v -> {})
                .hideNegativeButton(true)
                .show();
    }

    private void checkAutoLogin() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean isRemembered = sharedPreferences.getBoolean("rememberMe", false);
        String accessToken = sharedPreferences.getString("accessToken", null);

        if (isRemembered && accessToken != null) {
            goToHomeActivity();
        }
    }

    private void goToHomeActivity() {
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        requireActivity().startActivity(intent);
        requireActivity().finish();
    }
}