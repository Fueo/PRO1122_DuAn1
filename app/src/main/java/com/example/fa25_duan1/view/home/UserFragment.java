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
import com.example.fa25_duan1.view.zalo.ZaloRedirectActivity;
import com.example.fa25_duan1.viewmodel.AuthViewModel;
import com.example.fa25_duan1.viewmodel.OrderViewModel;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.Map;

import io.github.cutelibs.cutedialog.CuteDialog;

public class UserFragment extends Fragment {
    LinearLayout rlProfile, rlHistory, lnEmailContainer;
    TextView tvName, tvRole, tvEmail, tvVerifyLabel;

    // Các TextView thống kê đơn hàng
    TextView tvOrderConfirm, tvOrderSuccess, tvOrderCancelled;

    ImageView ivProfile, ivVerifyStatus;
    Button btnLogout;

    AuthViewModel authViewModel;
    OrderViewModel orderViewModel;
    User currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        orderViewModel = new ViewModelProvider(requireActivity()).get(OrderViewModel.class);

        // Click menu profile
        rlProfile.setOnClickListener(v -> {
            Intent intent = new Intent(view.getContext(), DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Trang cá nhân");
            intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "profile");
            startActivity(intent);
        });

        // Click menu history
        rlHistory.setOnClickListener(v -> {
            Intent intent = new Intent(view.getContext(), ZaloRedirectActivity.class);
            intent.putExtra(ZaloRedirectActivity.EXTRA_HEADER_TITLE, "Lịch sử mua hàng");
            intent.putExtra(ZaloRedirectActivity.EXTRA_CONTENT_FRAGMENT, "orderhistory");
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> showLogoutDialogConfirm());

        lnEmailContainer.setOnClickListener(v -> {
            if (currentUser != null) {
                if (currentUser.isVerifiedEmail()) {
                    FancyToast.makeText(requireContext(), "Email đã được xác thực!", FancyToast.LENGTH_SHORT, FancyToast.INFO, true).show();
                } else {
                    showDialogConfirmSendMail();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
        loadOrderStats();
    }

    // [CẬP NHẬT] Tải thống kê đơn hàng với ApiResponse
    private void loadOrderStats() {
        orderViewModel.getStatusCount().observe(getViewLifecycleOwner(), apiResponse -> {
            if (apiResponse != null && apiResponse.isStatus() && apiResponse.getData() != null) {
                Map<String, Integer> stats = apiResponse.getData();

                // Dùng getOrDefault (Java 8+) hoặc check null thủ công
                int pendingCount = stats.containsKey("Pending") ? stats.get("Pending") : 0;
                int processingCount = stats.containsKey("Processing") ? stats.get("Processing") : 0;
                int deliveredCount = stats.containsKey("Delivered") ? stats.get("Delivered") : 0;
                int cancelledCount = stats.containsKey("Cancelled") ? stats.get("Cancelled") : 0;

                // Pending + Processing = Chờ xác nhận/Đang xử lý
                tvOrderConfirm.setText(String.valueOf(pendingCount + processingCount));

                // Delivered = Thành công
                tvOrderSuccess.setText(String.valueOf(deliveredCount));

                // Cancelled = Đã hủy
                tvOrderCancelled.setText(String.valueOf(cancelledCount));
            }
        });
    }

    // [CẬP NHẬT] Tải thông tin user với ApiResponse
    private void loadUserData() {
        authViewModel.getMyInfo().observe(getViewLifecycleOwner(), apiResponse -> {
            if (apiResponse != null && apiResponse.isStatus()) {
                currentUser = apiResponse.getData();
                if (currentUser != null) {
                    updateUI(currentUser);
                }
            } else {
                // Nếu token lỗi hoặc hết hạn, có thể logout hoặc hiện thông báo
                String msg = (apiResponse != null) ? apiResponse.getMessage() : "Lỗi tải thông tin";
                // Log.e("UserFragment", msg);
            }
        });
    }

    private void updateUI(User user) {
        String roleStr = user.getRole() == 0 ? "Khách hàng" : user.getRole() == 1 ? "Nhân viên" : "Admin";
        tvName.setText(user.getName());
        tvRole.setText(roleStr);
        tvEmail.setText(user.getEmail());

        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            Glide.with(requireActivity())
                    .load(user.getAvatar())
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder) // Thêm ảnh lỗi nếu load fail
                    .into(ivProfile);
        } else {
            ivProfile.setImageResource(R.drawable.ic_avatar_placeholder);
        }

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

        lnEmailContainer = view.findViewById(R.id.lnEmailContainer);
        ivVerifyStatus = view.findViewById(R.id.ivVerifyStatus);
        tvVerifyLabel = view.findViewById(R.id.tvVerifyLabel);

        tvOrderConfirm = view.findViewById(R.id.tvOrderConfirm);
        tvOrderSuccess = view.findViewById(R.id.tvOrderSuccess);
        tvOrderCancelled = view.findViewById(R.id.tvOrderCancelled);
    }

    // =========================================================================
    // KHU VỰC XỬ LÝ XÁC THỰC EMAIL (CẬP NHẬT API RESPONSE)
    // =========================================================================

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

        TextView tvContent = dialogView.findViewById(R.id.tvConfirmContent);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmSend);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelSend);

        tvContent.setText("Hệ thống sẽ gửi mã OTP 6 số đến email:\n" + currentUser.getEmail() + "\nBạn có muốn tiếp tục?");

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();

            // [CẬP NHẬT LOGIC]
            authViewModel.sendVerifyEmail(currentUser.getUserID()).observe(getViewLifecycleOwner(), apiResponse -> {
                if (apiResponse != null && apiResponse.isStatus()) {
                    FancyToast.makeText(requireContext(), "Đã gửi mã xác nhận!", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
                    showInputOTPDialog();
                } else {
                    String msg = (apiResponse != null) ? apiResponse.getMessage() : "Lỗi kết nối";
                    FancyToast.makeText(requireContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                }
            });
        });
    }

    private void showInputOTPDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_dialog_verify_otp, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.setCancelable(false);
        dialog.show();

        EditText etOtpInput = dialogView.findViewById(R.id.etOtpInput);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmOtp);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelOtp);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String otp = etOtpInput.getText().toString().trim();

            if (otp.length() < 6) {
                FancyToast.makeText(requireContext(), "Vui lòng nhập đủ 6 số!", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
                return;
            }

            // [CẬP NHẬT LOGIC]
            authViewModel.verifyEmailOTP(currentUser.getUserID(), otp).observe(getViewLifecycleOwner(), apiResponse -> {
                if (apiResponse != null && apiResponse.isStatus()) {
                    dialog.dismiss();
                    FancyToast.makeText(requireContext(), "Xác thực thành công!", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, true).show();
                    loadUserData(); // Load lại data để update icon tích xanh
                } else {
                    String msg = (apiResponse != null) ? apiResponse.getMessage() : "Mã OTP không đúng";
                    FancyToast.makeText(requireContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                    etOtpInput.setText("");
                }
            });
        });
    }

    // =========================================================================
    // KHU VỰC XỬ LÝ ĐĂNG XUẤT (CẬP NHẬT API RESPONSE)
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
            authViewModel.logout(refreshToken).observe(getViewLifecycleOwner(), apiResponse -> {
                // Dù API thành công hay thất bại (do mạng...), Client vẫn nên logout để bảo mật
                handleLogoutSuccess(sharedPref);
            });
        } else {
            handleLogoutSuccess(sharedPref);
        }
    }

    private void handleLogoutSuccess(SharedPreferences sharedPref) {
        sharedPref.edit().clear().apply();
        FancyToast.makeText(requireContext(), "Đăng xuất thành công!", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();

        Intent intent = new Intent(requireActivity(), AuthActivity.class);
        // Xóa backstack để không back lại được màn hình user
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}