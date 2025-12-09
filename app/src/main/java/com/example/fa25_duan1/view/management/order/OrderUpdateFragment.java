package com.example.fa25_duan1.view.management.order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.OrderItemAdapter;
import com.example.fa25_duan1.model.Order;
import com.example.fa25_duan1.model.OrderDetail;
import com.example.fa25_duan1.viewmodel.OrderViewModel;

import io.github.cutelibs.cutedialog.CuteDialog;
import org.angmarch.views.NiceSpinner;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class OrderUpdateFragment extends Fragment {

    private OrderViewModel viewModel;
    private Order currentOrder;
    private String orderId;

    // UI Components
    private TextView tvOrderId, tvDate, tvFullname, tvPhone, tvAddress;
    private TextView tvNote, tvShippingFee, tvTotal;

    // Spinners cho Admin sửa đổi
    private NiceSpinner spinnerStatus, spinnerPaymentMethod, spinnerPaymentStatus;

    private RecyclerView rcvOrderDetails;
    private Button btnUpdateStatus;
    private OrderItemAdapter adapter;

    // --- CONFIG DATA ---
    private final DecimalFormat currencyFormat = new DecimalFormat("###,###,###");
    private final double SHIPPING_FEE = 25000;

    // 1. Danh sách Trạng thái đơn hàng
    private final List<String> statusList = new LinkedList<>(Arrays.asList(
            "Chờ xác nhận", "Đang xử lý", "Đang giao hàng", "Hoàn thành", "Đã hủy"
    ));

    // 2.1. Danh sách Phương thức thanh toán (HIỂN THỊ TRÊN UI)
    private final List<String> paymentMethodDisplayList = new LinkedList<>(Arrays.asList(
            "Thanh toán khi nhận hàng (COD)",
            "Chuyển khoản ngân hàng",
            "Ví điện tử ZaloPay"
    ));

    // 2.2. Danh sách Mã Phương thức thanh toán (GỬI VỀ SERVER - Map 1:1 với list trên)
    private final List<String> paymentMethodCodeList = new LinkedList<>(Arrays.asList(
            "COD",
            "QR",
            "Zalopay"
    ));

    // 3. Danh sách Trạng thái thanh toán (isPaid)
    private final List<String> paymentStatusList = new LinkedList<>(Arrays.asList(
            "Chưa thanh toán", "Đã thanh toán"
    ));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orderupdate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(OrderViewModel.class);
        initViews(view);
        setupSpinners();

        if (getActivity() != null && getActivity().getIntent() != null) {
            orderId = getActivity().getIntent().getStringExtra("orderId");
        }

        if (orderId != null) {
            fetchOrderDetails(orderId);
        } else {
            showErrorDialog("Lỗi: Không tìm thấy ID đơn hàng");
            btnUpdateStatus.setEnabled(false);
        }

        // Bấm nút -> Hiện Dialog xác nhận -> Mới gọi API
        btnUpdateStatus.setOnClickListener(v -> showConfirmUpdateDialog());
    }

    private void initViews(View view) {
        tvOrderId = view.findViewById(R.id.tvOrderId);
        tvDate = view.findViewById(R.id.tvDate);
        tvFullname = view.findViewById(R.id.tvFullname);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvAddress = view.findViewById(R.id.tvAddress);

        // Spinners
        spinnerStatus = view.findViewById(R.id.spinnerStatus);
        spinnerPaymentMethod = view.findViewById(R.id.spinnerPaymentMethod);
        spinnerPaymentStatus = view.findViewById(R.id.spinnerPaymentStatus);

        tvNote = view.findViewById(R.id.tvNote);
        tvShippingFee = view.findViewById(R.id.tvShippingFee);
        tvTotal = view.findViewById(R.id.tvTotal);

        btnUpdateStatus = view.findViewById(R.id.btnUpdateStatus);

        rcvOrderDetails = view.findViewById(R.id.rcvOrderDetails);
        rcvOrderDetails.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupSpinners() {
        spinnerStatus.attachDataSource(statusList);

        // Gắn danh sách tiếng Việt vào UI Spinner
        spinnerPaymentMethod.attachDataSource(paymentMethodDisplayList);

        spinnerPaymentStatus.attachDataSource(paymentStatusList);
    }

    private void fetchOrderDetails(String id) {
        viewModel.getOrderById(id).observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isStatus() && response.getData() != null) {
                currentOrder = response.getData();
                populateData(currentOrder);
            } else {
                showErrorDialog("Không tải được thông tin đơn hàng");
                btnUpdateStatus.setEnabled(false);
            }
        });
    }

    private void populateData(Order order) {
        tvOrderId.setText("Mã đơn: " + order.getId());
        tvDate.setText("Ngày đặt: " + formatDateTime(order.getDate()));
        tvFullname.setText(order.getFullname());
        tvPhone.setText(order.getPhone());
        tvAddress.setText(order.getAddress());
        tvNote.setText("Ghi chú: " + (order.getNote() != null ? order.getNote() : "Không có"));

        List<OrderDetail> details = order.getOrderDetails();
        if (details != null) {
            adapter = new OrderItemAdapter(details);
            rcvOrderDetails.setAdapter(adapter);
        }

        double subTotal = order.getTotal();
        tvShippingFee.setText(currencyFormat.format(SHIPPING_FEE) + " đ");
        tvTotal.setText(currencyFormat.format(subTotal) + " đ");

        // --- SET DATA CHO 3 SPINNER ---

        // 1. Status
        int statusIndex = getStatusIndex(order.getStatus());
        if (statusIndex != -1) spinnerStatus.setSelectedIndex(statusIndex);

        // 2. Payment Method [ĐÃ SỬA ĐỔI]
        // Lấy code từ server (ví dụ: "QR") -> Tìm index tương ứng trong codeList
        String serverMethod = order.getPaymentMethod(); // "QR", "COD", "Zalopay"
        int methodIndex = 0; // Mặc định là 0 (COD)

        if (serverMethod != null) {
            for (int i = 0; i < paymentMethodCodeList.size(); i++) {
                if (paymentMethodCodeList.get(i).equalsIgnoreCase(serverMethod)) {
                    methodIndex = i;
                    break;
                }
            }
        }
        spinnerPaymentMethod.setSelectedIndex(methodIndex);

        // 3. Payment Status (isPaid)
        int paidIndex = order.isPaid() ? 1 : 0; // 0: Chưa TT, 1: Đã TT
        spinnerPaymentStatus.setSelectedIndex(paidIndex);
    }

    // --- LOGIC XÁC NHẬN VÀ CẬP NHẬT ---

    private void showConfirmUpdateDialog() {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_confirm)
                .setTitle("Xác nhận cập nhật")
                .setDescription("Bạn có chắc chắn muốn cập nhật thông tin đơn hàng này không?")
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonText("Cập nhật", v -> performUpdateAPI())
                .setNegativeButtonText("Hủy", v -> {})
                .show();
    }

    private void performUpdateAPI() {
        if (currentOrder == null) return;

        // 1. Lấy trạng thái đơn hàng
        int statusIdx = spinnerStatus.getSelectedIndex();
        String serverStatusCode = mapStatusToServerCode(statusIdx);

        // 2. Lấy phương thức thanh toán [ĐÃ SỬA ĐỔI]
        // Lấy index đang chọn trên UI -> Map sang Code server
        int methodIdx = spinnerPaymentMethod.getSelectedIndex();
        // Bảo vệ index out of bounds
        if (methodIdx < 0 || methodIdx >= paymentMethodCodeList.size()) methodIdx = 0;

        String selectedPaymentMethodCode = paymentMethodCodeList.get(methodIdx);

        // 3. Lấy trạng thái thanh toán (Boolean)
        int paidIdx = spinnerPaymentStatus.getSelectedIndex();
        boolean isPaid = (paidIdx == 1); // 1 = Đã thanh toán = true

        // Gọi API cập nhật Full
        // Lưu ý: Tham số thứ 3 giờ là code (QR/COD/Zalopay) chứ không phải tiếng Việt
        viewModel.updateOrderStatus(currentOrder.getId(), serverStatusCode, selectedPaymentMethodCode, isPaid)
                .observe(getViewLifecycleOwner(), response -> {
                    if (response == null) {
                        showErrorDialog("Lỗi kết nối server");
                        return;
                    }

                    if (response.isStatus()) {
                        showSuccessDialog("Cập nhật đơn hàng thành công!");
                        fetchOrderDetails(currentOrder.getId()); // Load lại để đồng bộ
                    } else {
                        String backendMessage = response.getMessage();
                        if (backendMessage == null || backendMessage.trim().isEmpty()) {
                            backendMessage = "Lỗi không xác định từ hệ thống";
                        }
                        showErrorDialog(backendMessage);
                    }
                });
    }

    // --- HELPER DIALOGS ---

    private void showSuccessDialog(String message) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_success)
                .setTitle("Thành công")
                .setDescription(message)
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonText("Đóng", v -> {})
                .hideNegativeButton(true)
                .show();
    }

    private void showErrorDialog(String message) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_error)
                .setTitle("Thất bại")
                .setDescription(message)
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonText("Đóng", v -> {})
                .hideNegativeButton(true)
                .show();
    }

    // --- HELPER UTILS ---

    private String formatDateTime(String inputDate) {
        if (inputDate == null) return "";
        try {
            SimpleDateFormat inputFormat = inputDate.contains(".")
                    ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    : new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = inputFormat.parse(inputDate);
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return inputDate.replace("T", " ").split("\\.")[0];
        }
    }

    private int getStatusIndex(String statusFromServer) {
        if (statusFromServer == null) return 0;
        String status = statusFromServer.toLowerCase().trim();
        if (status.equals("pending") || status.equals("wait_confirm") || status.equals("chờ xác nhận")) return 0;
        if (status.equals("processing") || status.equals("confirmed") || status.equals("đang xử lý")) return 1;
        if (status.equals("shipping") || status.equals("shipped") || status.contains("đang giao")) return 2;
        if (status.equals("delivered") || status.equals("completed") || status.equals("hoàn thành")) return 3;
        if (status.equals("cancelled") || status.equals("canceled") || status.equals("đã hủy")) return 4;
        return 0;
    }

    private String mapStatusToServerCode(int index) {
        switch (index) {
            case 0: return "Pending";
            case 1: return "Processing";
            case 2: return "Shipping";
            case 3: return "Delivered";
            case 4: return "Cancelled";
            default: return "Pending";
        }
    }
}