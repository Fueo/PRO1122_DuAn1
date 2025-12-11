package com.example.fa25_duan1.view.auth;

import android.app.AlertDialog;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.auth.AuthResponse; // Import model AuthResponse
import com.example.fa25_duan1.view.home.HomeActivity;
import com.example.fa25_duan1.viewmodel.AuthViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.shashank.sony.fancytoastlib.FancyToast;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import io.github.cutelibs.cutedialog.CuteDialog;

public class LoginFragment extends Fragment {
    private AuthViewModel authViewModel;

    // View Components
    TextInputLayout tilUsername, tilPassword;
    EditText etUsername, etPassword;
    Button btnLogin;
    TextView tvSignUp, tvForgotPassword;
    CheckBox cbRememberMe;

    // Google Login Components
    CardView cvGoogleLogin;
    private GoogleSignInClient mGoogleSignInClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 1. Cấu hình Google Sign In
        configureGoogleSignIn();

        // 2. Kiểm tra tự động đăng nhập
        checkAutoLogin();

        // 3. Sự kiện Đăng nhập thường
        btnLogin.setOnClickListener(v -> handleNormalLogin());

        // 4. Sự kiện Đăng nhập Google
        cvGoogleLogin.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        // 5. Chuyển trang Đăng ký
        tvSignUp.setOnClickListener(v -> {
            ViewPager2 viewPager = requireActivity().findViewById(R.id.view_pager_auth);
            if (viewPager != null) viewPager.setCurrentItem(1);
        });

        // 6. Quên mật khẩu
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void initViews(View view) {
        tilUsername = view.findViewById(R.id.tilUsername);
        tilPassword = view.findViewById(R.id.tilPassword);
        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvSignUp = view.findViewById(R.id.tvSignUp);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);
        cbRememberMe = view.findViewById(R.id.cbRememberMe);
        cvGoogleLogin = view.findViewById(R.id.cvGoogleLogin);
    }

    // =================================================================================
    // [ĐÃ SỬA] XỬ LÝ ĐĂNG NHẬP THƯỜNG (Hiện message từ Backend)
    // =================================================================================
    private void handleNormalLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (username.isEmpty() || password.isEmpty()) {
            FancyToast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin!", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
            return;
        }

        // Hiện loading
        CuteDialog.withIcon loadingDialog = showLoadingDialog();

        // Quan sát ApiResponse<AuthResponse>
        authViewModel.login(username, password).observe(getViewLifecycleOwner(), apiResponse -> {
            loadingDialog.dismiss(); // Tắt loading

            if (apiResponse == null) {
                showLoginErrorDialog("Lỗi không xác định.");
                return;
            }

            if (apiResponse.isStatus()) {
                // === THÀNH CÔNG ===
                AuthResponse data = apiResponse.getData();
                if (data != null) {
                    saveUserAndGoHome(data.getAccessToken(), data.getRefreshToken());
                }
            } else {
                // === THẤT BẠI ===
                // Hiển thị message lỗi chính xác từ Server trả về
                showLoginErrorDialog(apiResponse.getMessage());
            }
        });
    }

    // =================================================================================
    // KHU VỰC XỬ LÝ ĐĂNG NHẬP GOOGLE
    // =================================================================================

    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                handleSignInResult(task);
            }
    );

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            Log.d("GoogleLogin", "Token: " + idToken);
            loginWithGoogleToServer(idToken);
        } catch (ApiException e) {
            Log.w("GoogleLogin", "signInResult:failed code=" + e.getStatusCode());
            FancyToast.makeText(requireContext(), "Đăng nhập Google thất bại (Code: " + e.getStatusCode() + ")", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
        }
    }

    // [ĐÃ SỬA] Xử lý Login Google với ApiResponse
    private void loginWithGoogleToServer(String idToken) {
        CuteDialog.withIcon loadingDialog = showLoadingDialog();

        authViewModel.loginGoogle(idToken).observe(getViewLifecycleOwner(), apiResponse -> {
            loadingDialog.dismiss();

            if (apiResponse == null) {
                showLoginErrorDialog("Lỗi kết nối tới server.");
                return;
            }

            if (apiResponse.isStatus()) {
                // === THÀNH CÔNG ===
                AuthResponse data = apiResponse.getData();
                if (data != null) {
                    saveUserAndGoHome(data.getAccessToken(), data.getRefreshToken());
                }
            } else {
                // === THẤT BẠI ===
                showLoginErrorDialog(apiResponse.getMessage());
            }
        });
    }

    // =================================================================================
    // CÁC HÀM TIỆN ÍCH CHUNG
    // =================================================================================

    private CuteDialog.withIcon showLoadingDialog() {
        CuteDialog.withIcon dialog = new CuteDialog.withIcon(requireActivity())
                .setTitle("Đang xử lý...")
                .setDescription("Vui lòng đợi giây lát")
                .hidePositiveButton(true)
                .hideNegativeButton(true);
        dialog.show();
        return dialog;
    }

    private void saveUserAndGoHome(String accessToken, String refreshToken) {
        String userId = getUserIdFromToken(accessToken);
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("accessToken", accessToken);
        editor.putString("refreshToken", refreshToken);
        editor.putString("userId", userId);
        editor.putBoolean("rememberMe", cbRememberMe.isChecked());
        editor.apply();

        FancyToast.makeText(requireContext(), "Đăng nhập thành công!", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
        goToHomeActivity();
    }

    private void showLoginErrorDialog(String message) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_error)
                .setTitle("Đăng nhập thất bại")
                .setDescription(message) // Message từ server sẽ hiện ở đây
                .setPositiveButtonText("Thử lại", v -> {})
                .show();
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

    private void checkAutoLogin() {
        SharedPreferences sp = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        if (sp.getBoolean("rememberMe", false) && sp.getString("accessToken", null) != null) goToHomeActivity();
    }

    private void goToHomeActivity() {
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        requireActivity().startActivity(intent);
        requireActivity().finish();
    }

    // =================================================================================
    // [ĐÃ SỬA] KHU VỰC QUÊN MẬT KHẨU (CẬP NHẬT THEO API RESPONSE)
    // =================================================================================

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

            authViewModel.forgotPassword(username, email).observe(getViewLifecycleOwner(), apiResponse -> {
                if (apiResponse != null && apiResponse.isStatus()) {
                    dialog.dismiss();
                    FancyToast.makeText(requireContext(), "Đã gửi mã OTP về Email!", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
                    showOtpStepDialog(email);
                } else {
                    String msg = apiResponse != null ? apiResponse.getMessage() : "Lỗi kết nối";
                    FancyToast.makeText(requireContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                }
            });
        });
    }

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

            authViewModel.checkOtpForgot(email, otp).observe(getViewLifecycleOwner(), apiResponse -> {
                if (apiResponse != null && apiResponse.isStatus()) {
                    dialog.dismiss();
                    showNewPasswordStepDialog(email, otp);
                } else {
                    String msg = apiResponse != null ? apiResponse.getMessage() : "Mã OTP không đúng";
                    FancyToast.makeText(requireContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                }
            });
        });
    }

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

            authViewModel.resetPassword(email, confirmedOtp, newPass).observe(getViewLifecycleOwner(), apiResponse -> {
                if (apiResponse != null && apiResponse.isStatus()) {
                    dialog.dismiss();
                    FancyToast.makeText(requireContext(), "Đổi mật khẩu thành công! Hãy đăng nhập ngay.", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, true).show();
                } else {
                    String msg = apiResponse != null ? apiResponse.getMessage() : "Lỗi đổi mật khẩu";
                    FancyToast.makeText(requireContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                }
            });
        });
    }
}