package com.example.fa25_duan1.view.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.view.detail.DetailActivity;
import com.example.fa25_duan1.viewmodel.AuthViewModel;
import com.shashank.sony.fancytoastlib.FancyToast;

import io.github.cutelibs.cutedialog.CuteDialog;

public class ProfileFragment extends Fragment {
    TextView tvName, tvRole, tvEmail, tvAddress, tvChangePassword;
    ImageView ivProfile, btnEdit;

    AuthViewModel authViewModel;
    User currentUser;

    // Lưu lại hành động chờ (Pending Action)
    private String pendingHeaderTitle = "";
    private String pendingContentFragment = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 1. Sửa thông tin cá nhân
        btnEdit.setOnClickListener(v -> handleSecureAction("Thông tin cá nhân", "updateinfo"));

        // 2. Đổi mật khẩu
        tvChangePassword.setOnClickListener(v -> handleSecureAction("Thay đổi mật khẩu", "changepassword"));

        // 3. Sổ địa chỉ (Không cần OTP)
        tvAddress.setOnClickListener(v -> openActivity("Sổ địa chỉ", "address"));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }

    // [CẬP NHẬT] Load profile với ApiResponse
    private void loadUserProfile() {
        // Xóa observer cũ để tránh duplicate nếu gọi nhiều lần trong onResume
        authViewModel.getMyInfo().removeObservers(getViewLifecycleOwner());

        authViewModel.getMyInfo().observe(getViewLifecycleOwner(), apiResponse -> {
            if (apiResponse != null && apiResponse.isStatus()) {
                currentUser = apiResponse.getData();
                if (currentUser != null) {
                    updateUI(currentUser);
                }
            } else {
                // Có thể xử lý lỗi load profile ở đây nếu cần
            }
        });
    }

    private void updateUI(User user) {
        String role = user.getRole() == 0 ? "Khách hàng" : user.getRole() == 1 ? "Nhân viên" : "Admin";
        tvName.setText(user.getName());
        tvRole.setText(role);
        tvEmail.setText(user.getEmail());

        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            Glide.with(requireActivity())
                    .load(user.getAvatar())
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
                    .into(ivProfile);
        } else {
            ivProfile.setImageResource(R.drawable.ic_avatar_placeholder);
        }
    }

    // =========================================================================
    // LOGIC BẢO MẬT (SECURITY CHECK)
    // =========================================================================

    private void handleSecureAction(String header, String content) {
        this.pendingHeaderTitle = header;
        this.pendingContentFragment = content;

        if (currentUser != null && currentUser.isVerifiedEmail()) {
            // Đã xác thực -> Bắt buộc nhập OTP
            showConfirmSendOtpDialog();
        } else {
            // Chưa xác thực -> Cho phép đi tiếp
            executePendingAction();
        }
    }

    private void executePendingAction() {
        openActivity(pendingHeaderTitle, pendingContentFragment);
        pendingHeaderTitle = "";
        pendingContentFragment = "";
    }

    // Bước 1: Hỏi user có muốn nhận OTP không
    private void showConfirmSendOtpDialog() {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_info)
                .setTitle("Yêu cầu xác thực")
                .setDescription("Vì lý do bảo mật, hệ thống sẽ gửi mã OTP đến email: " + currentUser.getEmail())
                .setPositiveButtonText("Gửi mã", v -> sendOtpToEmail())
                .setNegativeButtonText("Hủy", v -> {})
                .show();
    }

    // Bước 2: Gọi API gửi OTP (Cập nhật ApiResponse)
    private void sendOtpToEmail() {
        CuteDialog.withIcon loading = showLoadingDialog();

        authViewModel.sendUpdateProfileOtp().observe(getViewLifecycleOwner(), apiResponse -> {
            loading.dismiss();

            if (apiResponse != null && apiResponse.isStatus()) {
                FancyToast.makeText(requireContext(), "Đã gửi mã OTP!", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
                showInputOtpDialog();
            } else {
                String msg = (apiResponse != null) ? apiResponse.getMessage() : "Lỗi gửi mã";
                showErrorDialog(msg);
            }
        });
    }

    // Bước 3: Hiện ô nhập OTP và kiểm tra (Cập nhật ApiResponse)
    private void showInputOtpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_dialog_verify_otp, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);
        dialog.show();

        EditText etOtpInput = dialogView.findViewById(R.id.etOtpInput);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmOtp);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelOtp);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String otp = etOtpInput.getText().toString().trim();
            if (otp.length() < 6) {
                FancyToast.makeText(requireContext(), "Nhập đủ 6 số", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
                return;
            }

            // Gọi API kiểm tra OTP
            authViewModel.checkOtpValid(otp).observe(getViewLifecycleOwner(), apiResponse -> {
                if (apiResponse != null && apiResponse.isStatus()) {
                    dialog.dismiss();
                    FancyToast.makeText(requireContext(), "Xác thực thành công!", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();

                    // MỞ MÀN HÌNH ĐÍCH
                    executePendingAction();
                } else {
                    String msg = (apiResponse != null) ? apiResponse.getMessage() : "Mã OTP không đúng";
                    FancyToast.makeText(requireContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                    etOtpInput.setText(""); // Xóa để nhập lại
                }
            });
        });
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private CuteDialog.withIcon showLoadingDialog() {
        CuteDialog.withIcon dialog = new CuteDialog.withIcon(requireActivity())
                .setTitle("Đang xử lý...")
                .setDescription("Vui lòng đợi giây lát")
                .hidePositiveButton(true)
                .hideNegativeButton(true);
        dialog.show();
        return dialog;
    }

    private void showErrorDialog(String msg) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_error)
                .setTitle("Lỗi")
                .setDescription(msg)
                .setPositiveButtonText("Đóng", v -> {})
                .show();
    }

    private void initViews(View view) {
        tvName = view.findViewById(R.id.tvName);
        tvRole = view.findViewById(R.id.tvRole);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvAddress = view.findViewById(R.id.tvAddress);
        ivProfile = view.findViewById(R.id.ivProfile);
        tvChangePassword = view.findViewById(R.id.tvChangePassword);
        btnEdit = view.findViewById(R.id.btnEdit);
    }

    private void openActivity(String header, String content) {
        Intent intent = new Intent(getContext(), DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, header);
        intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, content);
        startActivity(intent);
    }
}