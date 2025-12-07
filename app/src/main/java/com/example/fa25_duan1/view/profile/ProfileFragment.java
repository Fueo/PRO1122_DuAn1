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
import android.widget.Toast;

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

public class ProfileFragment extends Fragment {
    TextView tvName, tvRole, tvEmail, tvAddress, tvChangePassword;
    ImageView ivProfile, btnEdit;

    AuthViewModel authViewModel;
    User currentUser; // Lưu user hiện tại để check trạng thái verify

    // Lưu lại hành động người dùng muốn làm để thực hiện sau khi OTP đúng
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
        btnEdit.setOnClickListener(v -> {
            handleSecureAction("Thông tin cá nhân", "updateinfo");
        });

        // 2. Đổi mật khẩu
        tvChangePassword.setOnClickListener(v -> {
            handleSecureAction("Thay đổi mật khẩu", "changepassword");
        });

        // 3. Sổ địa chỉ (Thường không cần bảo mật gắt gao, cho qua luôn)
        tvAddress.setOnClickListener(v -> {
            openActivity("Sổ địa chỉ", "address");
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        if (authViewModel.getMyInfo() != null) {
            authViewModel.getMyInfo().removeObservers(getViewLifecycleOwner());
        }

        authViewModel.getMyInfo().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getData() != null) {
                currentUser = response.getData(); // Lưu lại user
                updateUI(currentUser);
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
        // Lưu lại đích đến
        this.pendingHeaderTitle = header;
        this.pendingContentFragment = content;

        if (currentUser != null && currentUser.isVerifiedEmail()) {
            // Nếu đã xác thực -> Bắt buộc nhập OTP
            showConfirmSendOtpDialog();
        } else {
            // Chưa xác thực -> Cho qua (hoặc chặn tùy logic của bạn)
            // Ở đây mình cho qua để họ vào sửa email cho đúng
            executePendingAction();
        }
    }

    private void executePendingAction() {
        openActivity(pendingHeaderTitle, pendingContentFragment);
        // Reset
        pendingHeaderTitle = "";
        pendingContentFragment = "";
    }

    // Bước 1: Hỏi user có muốn nhận OTP không
    private void showConfirmSendOtpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_dialog_confirm_send, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        TextView tvContent = dialogView.findViewById(R.id.tvConfirmContent);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmSend);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelSend);

        tvContent.setText("Vì lý do bảo mật, vui lòng xác thực OTP qua email:\n" + currentUser.getEmail() + "\nđể tiếp tục chỉnh sửa.");

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            sendOtpToEmail();
        });
    }

    // Bước 2: Gọi API gửi OTP (API mới: send-update-profile-otp)
    private void sendOtpToEmail() {
        FancyToast.makeText(requireContext(), "Đang gửi mã...", FancyToast.LENGTH_SHORT, FancyToast.INFO, false).show();

        authViewModel.sendUpdateProfileOtp().observe(getViewLifecycleOwner(), msg -> {
            if ("OK".equals(msg)) {
                FancyToast.makeText(requireContext(), "Đã gửi mã OTP!", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();
                showInputOtpDialog();
            } else {
                FancyToast.makeText(requireContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
            }
        });
    }

    // Bước 3: Hiện ô nhập OTP và kiểm tra
    private void showInputOtpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_dialog_verify_otp, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false); // Bắt buộc nhập hoặc hủy
        dialog.show();

        EditText etOtpInput = dialogView.findViewById(R.id.etOtpInput);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmOtp);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelOtp);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String otp = etOtpInput.getText().toString().trim();
            if (otp.length() < 6) {
                FancyToast.makeText(requireContext(), "Nhập đủ 6 số", FancyToast.LENGTH_SHORT, FancyToast.WARNING, false).show();
                return;
            }

            // Gọi API Pre-check OTP (API mới: check-otp-valid)
            authViewModel.checkOtpValid(otp).observe(getViewLifecycleOwner(), msg -> {
                if ("OK".equals(msg)) {
                    dialog.dismiss();
                    FancyToast.makeText(requireContext(), "Xác thực thành công!", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();

                    // QUAN TRỌNG: Mở màn hình đích
                    executePendingAction();
                } else {
                    FancyToast.makeText(requireContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
                }
            });
        });
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

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