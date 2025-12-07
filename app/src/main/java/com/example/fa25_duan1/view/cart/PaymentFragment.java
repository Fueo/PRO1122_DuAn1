package com.example.fa25_duan1.view.cart;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.view.home.HomeActivity;
import com.example.fa25_duan1.viewmodel.OrderViewModel;
import com.google.android.material.button.MaterialButton;
import com.shashank.sony.fancytoastlib.FancyToast;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.text.DecimalFormat;

import io.github.cutelibs.cutedialog.CuteDialog;

public class PaymentFragment extends Fragment {

    // --- CẤU HÌNH NGÂN HÀNG ---
    private static final String BANK_ID = "ACB";
    private static final String ACCOUNT_NO = "22528647";
    private static final String ACCOUNT_NAME = "Liêu Thiên Hạo";
    private static final String TEMPLATE = "compact2";

    // --- Views ---
    private TextView tvTotalAmount, tvBankInfo, tvTransferContent; // Thêm tvTransferContent
    private ImageView imgQrCode, imgArrowTerms, btnCopy; // Thêm btnCopy
    private RelativeLayout layoutTermsHeader;
    private ExpandableLayout expandableTerms;
    private CheckBox cbConfirm;
    private MaterialButton btnCancel, btnImPaid;

    // --- ViewModel & Logic Polling ---
    private OrderViewModel orderViewModel;
    private Handler statusHandler;
    private Runnable statusRunnable;
    private boolean isTransactionSuccess = false;

    // --- Data Variables ---
    private String orderId;
    private long totalAmount;
    private String transCode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Lấy dữ liệu từ Intent
        if (getActivity() != null && getActivity().getIntent() != null) {
            Intent intent = requireActivity().getIntent();
            orderId = intent.getStringExtra("ORDER_ID");
            totalAmount = intent.getLongExtra("TOTAL_AMOUNT", 0);
            transCode = intent.getStringExtra("TRANS_CODE");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderViewModel = new ViewModelProvider(requireActivity()).get(OrderViewModel.class);

        initViews(view);
        setupData();
        setupEvents();

        startCheckingPaymentStatus();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopCheckingPaymentStatus();
    }

    private void initViews(View view) {
        tvTotalAmount = view.findViewById(R.id.tv_total_amount);
        tvBankInfo = view.findViewById(R.id.tv_bank_info);
        tvTransferContent = view.findViewById(R.id.tv_transfer_content); // [MỚI]

        imgQrCode = view.findViewById(R.id.img_qr_code);
        btnCopy = view.findViewById(R.id.btn_copy); // [MỚI]

        layoutTermsHeader = view.findViewById(R.id.layout_terms_header);
        expandableTerms = view.findViewById(R.id.expandable_terms);
        imgArrowTerms = view.findViewById(R.id.img_arrow_terms);

        cbConfirm = view.findViewById(R.id.cb_confirm);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnImPaid = view.findViewById(R.id.btn_im_paid);
    }

    private void setupData() {
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        tvTotalAmount.setText(formatter.format(totalAmount).replace(",", ".") + " VNĐ");

        // Hiển thị nội dung chuyển khoản
        if (transCode != null) {
            tvTransferContent.setText(transCode);
        }

        loadQrCode();
    }

    private void loadQrCode() {
        if (transCode == null || transCode.isEmpty()) return;

        String qrUrl = "https://img.vietqr.io/image/" + BANK_ID + "-" + ACCOUNT_NO + "-" + TEMPLATE + ".png" +
                "?amount=" + 2000 +
                "&addInfo=" + transCode +
                "&accountName=" + ACCOUNT_NAME;

        Glide.with(this)
                .load(qrUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(imgQrCode);

        tvBankInfo.setText(BANK_ID + " - " + ACCOUNT_NO + "\n" + ACCOUNT_NAME);
    }

    private void setupEvents() {
        // [MỚI] Xử lý sự kiện Copy
        btnCopy.setOnClickListener(v -> {
            if (transCode != null && !transCode.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Transaction Code", transCode);
                clipboard.setPrimaryClip(clip);
                FancyToast.makeText(getContext(), "Đã sao chép nội dung!", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();
            }
        });

        // Expandable Layout
        layoutTermsHeader.setOnClickListener(v -> {
            if (expandableTerms.isExpanded()) {
                expandableTerms.collapse();
                imgArrowTerms.animate().rotation(0).setDuration(300).start();
            } else {
                expandableTerms.expand();
                imgArrowTerms.animate().rotation(180).setDuration(300).start();
            }
        });

        // Checkbox
        cbConfirm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnImPaid.setEnabled(isChecked);
            btnImPaid.setAlpha(isChecked ? 1.0f : 0.5f);
        });

        btnCancel.setOnClickListener(v -> navigateToHome());

        btnImPaid.setOnClickListener(v -> checkOrderPaidOnce());
    }

    // =========================================================================
    // LOGIC POLLING
    // =========================================================================

    private void startCheckingPaymentStatus() {
        statusHandler = new Handler(Looper.getMainLooper());
        statusRunnable = new Runnable() {
            @Override
            public void run() {
                if (orderId == null || isTransactionSuccess) return;

                orderViewModel.getOrderById(orderId).observe(getViewLifecycleOwner(), response -> {
                    if (response != null && response.isStatus() && response.getData() != null) {
                        if (response.getData().isPaid()) {
                            isTransactionSuccess = true;
                            stopCheckingPaymentStatus();
                            showSuccessDialog();
                        }
                    }
                });

                if (!isTransactionSuccess) {
                    statusHandler.postDelayed(this, 3000);
                }
            }
        };
        statusHandler.post(statusRunnable);
    }

    private void stopCheckingPaymentStatus() {
        if (statusHandler != null && statusRunnable != null) {
            statusHandler.removeCallbacks(statusRunnable);
        }
    }

    private void checkOrderPaidOnce() {
        orderViewModel.getOrderById(orderId).observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isStatus() && response.getData() != null) {
                if (response.getData().isPaid()) {
                    showSuccessDialog();
                } else {
                    new CuteDialog.withIcon(requireActivity())
                            .setIcon(R.drawable.ic_dialog_info)
                            .setTitle("Đang chờ tiền về")
                            .setDescription("Hệ thống chưa nhận được giao dịch.\nVui lòng chờ thêm giây lát.")
                            .setPrimaryColor(R.color.blue)
                            .setPositiveButtonText("Đóng", v -> {})
                            .hideNegativeButton(true)
                            .show();
                }
            }
        });
    }

    // =========================================================================
    // DIALOG & NAVIGATION
    // =========================================================================

    private void showSuccessDialog() {
        if (getContext() == null) return;

        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_success)
                .setTitle("Thanh toán thành công!")
                .setDescription("Hệ thống đã nhận được tiền.\nĐơn hàng đang được xử lý.")
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonText("Về trang chủ", v -> navigateToHome())
                .hideNegativeButton(true)
                .show();
    }

    private void navigateToHome() {
        Intent intent = new Intent(requireContext(), HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        if (getActivity() != null) getActivity().finish();
    }
}