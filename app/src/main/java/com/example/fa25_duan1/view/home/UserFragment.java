package com.example.fa25_duan1.view.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.view.auth.AuthActivity;
import com.example.fa25_duan1.view.detail.DetailActivity;
import com.example.fa25_duan1.viewmodel.AuthViewModel;
import com.example.fa25_duan1.viewmodel.UserViewModel;
import com.shashank.sony.fancytoastlib.FancyToast;

import io.github.cutelibs.cutedialog.CuteDialog;

public class UserFragment extends Fragment {
    LinearLayout rlProfile, rlHistory, lnEmailContainer;
    TextView tvName, tvRole, tvEmail, tvVerifyLabel;
    ImageView ivProfile, ivVerifyStatus;
    Button btnLogout;

    AuthViewModel authViewModel;
    User currentUser; // Lưu user hiện tại

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        // Click menu profile
        rlProfile.setOnClickListener(v -> {
            Intent intent = new Intent(view.getContext(), DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Trang cá nhân");
            intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "profile");
            startActivity(intent);
        });

        // Click menu history
        rlHistory.setOnClickListener(v -> {
            Intent intent = new Intent(view.getContext(), DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Lịch sử mua hàng");
            intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "orderhistory");
            startActivity(intent);
        });

        // Click logout
        btnLogout.setOnClickListener(v -> showLogoutDialogConfirm());

        // --- SỰ KIỆN CLICK EMAIL ĐỂ XÁC THỰC ---
        lnEmailContainer.setOnClickListener(v -> {
            if (currentUser != null) {
                if (currentUser.isVerifiedEmail()) {
                    FancyToast.makeText(requireContext(), "Email đã được xác thực!", FancyToast.LENGTH_SHORT, FancyToast.INFO, true).show();
                } else {
                    showDialogConfirmSendMail(); // Gọi hàm hiển thị dialog đẹp
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        authViewModel.getMyInfo().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getData() != null) {
                currentUser = response.getData();
                updateUI(currentUser);
            }
        });
    }

    private void updateUI(User user) {
        String roleStr = user.getRole() == 0 ? "Khách hàng" : user.getRole() == 1 ? "Nhân viên" : "Admin";
        tvName.setText(user.getName());
        tvRole.setText(roleStr);
        tvEmail.setText(user.getEmail());

        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            Glide.with(requireActivity()).load(user.getAvatar()).placeholder(R.drawable.ic_avatar_placeholder).into(ivProfile);
        } else {
            ivProfile.setImageResource(R.drawable.ic_avatar_placeholder);
        }

        // --- CHECK TRẠNG THÁI XÁC THỰC ---
        if (user.isVerifiedEmail()) {
            ivVerifyStatus.setVisibility(View.VISIBLE);
            tvVerifyLabel.setVisibility(View.GONE);
        } else {
            ivVerifyStatus.setVisibility(View.GONE);
            tvVerifyLabel.setVisibility(View.VISIBLE);
            tvVerifyLabel.setText("(Chưa xác thực - Nhấn để gửi mã)");
        }
    }

    private void initViews(View view) {
        rlProfile = view.findViewById(R.id.rlProfile);
        rlHistory = view.findViewById(R.id.rlHistory);
        btnLogout = view.findViewById(R.id.btnLogout);
        tvName = view.findViewById(R.id.tvName);
        tvRole = view.findViewById(R.id.tvRole);
        tvEmail = view.findViewById(R.id.tvEmail);
        ivProfile = view.findViewById(R.id.ivProfile);

        // Các view mới cho phần xác thực Email
        lnEmailContainer = view.findViewById(R.id.lnEmailContainer);
        ivVerifyStatus = view.findViewById(R.id.ivVerifyStatus);
        tvVerifyLabel = view.findViewById(R.id.tvVerifyLabel);
    }

    // =========================================================================
    // KHU VỰC XỬ LÝ XÁC THỰC EMAIL
    // =========================================================================

    // 1. Dialog Xác nhận gửi mã (GIAO DIỆN ĐẸP)
    private void showDialogConfirmSendMail() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_dialog_confirm_send, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.setCancelable(false);
        dialog.show();

        // Ánh xạ View
        TextView tvContent = dialogView.findViewById(R.id.tvConfirmContent);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmSend);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelSend);

        // Set nội dung chi tiết (hiện rõ email cho uy tín)
        tvContent.setText("Hệ thống sẽ gửi mã OTP 6 số đến email:\n" + currentUser.getEmail() + "\nBạn có muốn tiếp tục?");

        // Sự kiện Hủy
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Sự kiện Gửi
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss(); // Đóng dialog xác nhận trước

            // Gọi API gửi mã
            authViewModel.sendVerifyEmail(currentUser.getUserID()).observe(getViewLifecycleOwner(), res -> {
                if ("OK".equals(res)) {
                    FancyToast.makeText(requireContext(), "Đã gửi mã xác nhận!", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
                    // Mở tiếp Dialog nhập OTP
                    showInputOTPDialog();
                } else {
                    FancyToast.makeText(requireContext(), res, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                }
            });
        });
    }

    // 2. Dialog nhập OTP (GIAO DIỆN ĐẸP)
    private void showInputOTPDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        // Inflate layout tùy chỉnh chúng ta đã tạo
        View dialogView = inflater.inflate(R.layout.layout_dialog_verify_otp, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Làm nền dialog trong suốt để thấy được bo góc của CardView
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.setCancelable(false); // Bắt buộc user phải bấm Hủy hoặc Xác nhận
        dialog.show();

        // Ánh xạ View trong Dialog
        EditText etOtpInput = dialogView.findViewById(R.id.etOtpInput);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmOtp);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelOtp);

        // Xử lý nút Hủy
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Xử lý nút Xác nhận
        btnConfirm.setOnClickListener(v -> {
            String otp = etOtpInput.getText().toString().trim();

            if (otp.length() < 6) {
                FancyToast.makeText(requireContext(), "Vui lòng nhập đủ 6 số!", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
                return;
            }

            // Gọi API xác thực
            authViewModel.verifyEmailOTP(currentUser.getUserID(), otp).observe(getViewLifecycleOwner(), res -> {
                if ("OK".equals(res)) {
                    dialog.dismiss();
                    FancyToast.makeText(requireContext(), "Xác thực thành công!", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, true).show();
                    // Load lại dữ liệu để cập nhật giao diện (hiện tích xanh)
                    loadUserData();
                } else {
                    FancyToast.makeText(requireContext(), res, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                    etOtpInput.setText(""); // Xóa mã sai để nhập lại
                }
            });
        });
    }

    // =========================================================================
    // KHU VỰC XỬ LÝ ĐĂNG XUẤT
    // =========================================================================

    private void showLogoutDialogConfirm() {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_info)
                .setTitle("Đăng xuất")
                .setDescription("Bạn có chắc chắn muốn đăng xuất?")
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setPositiveButtonText("Đăng xuất", v -> performLogout())
                .setNegativeButtonText("Hủy", v -> {})
                .show();
    }

    private void performLogout() {
        SharedPreferences sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String refreshToken = sharedPref.getString("refreshToken", null);
        if (refreshToken != null) {
            authViewModel.logout(refreshToken).observe(getViewLifecycleOwner(), response -> handleLogoutSuccess(sharedPref));
        } else {
            handleLogoutSuccess(sharedPref);
        }
    }

    private void handleLogoutSuccess(SharedPreferences sharedPref) {
        sharedPref.edit().clear().apply();
        FancyToast.makeText(requireContext(), "Đăng xuất thành công!", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
        startActivity(new Intent(requireActivity(), AuthActivity.class));
        requireActivity().finish();
    }
}