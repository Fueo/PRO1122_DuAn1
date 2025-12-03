package com.example.fa25_duan1.view.management.order;

import android.app.Activity;
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

import com.shashank.sony.fancytoastlib.FancyToast; // Có thể xóa nếu không dùng ở chỗ khác
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

    // UI
    private TextView tvOrderId, tvDate, tvFullname, tvPhone, tvAddress;
    private TextView tvPaymentMethod, tvNote, tvShippingFee, tvTotal;
    private NiceSpinner spinnerStatus;
    private RecyclerView rcvOrderDetails;
    private Button btnUpdateStatus;
    private OrderItemAdapter adapter;

    // Config
    private final List<String> statusList = new LinkedList<>(Arrays.asList(
            "Chờ xác nhận", "Đang xử lý", "Đang giao hàng", "Hoàn thành", "Đã hủy"
    ));
    private final DecimalFormat currencyFormat = new DecimalFormat("###,###,###");
    private final double SHIPPING_FEE = 25000;

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
        setupSpinner();

        if (getActivity() != null && getActivity().getIntent() != null) {
            orderId = getActivity().getIntent().getStringExtra("orderId");
        }

        if (orderId != null) {
            fetchOrderDetails(orderId);
        } else {
            // Lỗi khi không có ID -> Hiện dialog lỗi và đóng
            showErrorDialog("Lỗi: Không tìm thấy ID đơn hàng");
            btnUpdateStatus.setEnabled(false);
        }

        btnUpdateStatus.setOnClickListener(v -> updateOrderStatus());
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

    private void initViews(View view) {
        tvOrderId = view.findViewById(R.id.tvOrderId);
        tvDate = view.findViewById(R.id.tvDate);
        spinnerStatus = view.findViewById(R.id.spinnerStatus);
        tvFullname = view.findViewById(R.id.tvFullname);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvAddress = view.findViewById(R.id.tvAddress);
        rcvOrderDetails = view.findViewById(R.id.rcvOrderDetails);
        rcvOrderDetails.setLayoutManager(new LinearLayoutManager(getContext()));
        tvPaymentMethod = view.findViewById(R.id.tvPaymentMethod);
        tvNote = view.findViewById(R.id.tvNote);
        tvShippingFee = view.findViewById(R.id.tvShippingFee);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnUpdateStatus = view.findViewById(R.id.btnUpdateStatus);
    }

    private void setupSpinner() {
        spinnerStatus.attachDataSource(statusList);
    }

    private void populateData(Order order) {
        tvOrderId.setText("Mã đơn: " + order.getId());
        tvDate.setText("Ngày đặt: " + formatDateTime(order.getDate()));
        tvFullname.setText(order.getFullname());
        tvPhone.setText(order.getPhone());
        tvAddress.setText(order.getAddress());

        List<OrderDetail> details = order.getOrderDetails();
        if (details != null) {
            adapter = new OrderItemAdapter(details);
            rcvOrderDetails.setAdapter(adapter);
        }

        tvPaymentMethod.setText("Phương thức: " + order.getPaymentMethod());
        tvNote.setText("Ghi chú: " + (order.getNote() != null ? order.getNote() : "Không có"));

        double subTotal = order.getTotal();
        tvShippingFee.setText(currencyFormat.format(SHIPPING_FEE) + " đ");

        tvTotal.setText(currencyFormat.format(subTotal) + " đ");

        int statusIndex = getStatusIndex(order.getStatus());
        if (statusIndex != -1) spinnerStatus.setSelectedIndex(statusIndex);
    }

    private void updateOrderStatus() {
        if (currentOrder == null) return;

        int selectedIndex = spinnerStatus.getSelectedIndex();
        String serverStatusCode = mapStatusToServerCode(selectedIndex);

        viewModel.updateOrderStatus(currentOrder.getId(), serverStatusCode).observe(getViewLifecycleOwner(), response -> {
            // 1. Kiểm tra null response (Lỗi mạng/server sập)
            if (response == null) {
                showErrorDialog("Lỗi kết nối server");
                return;
            }

            // 2. Kiểm tra trạng thái logic (True/False)
            if (response.isStatus()) {
                // THÀNH CÔNG -> Hiện Dialog xanh
                showSuccessDialog("Cập nhật trạng thái đơn hàng thành công!");
                fetchOrderDetails(currentOrder.getId());
            } else {
                // THẤT BẠI (Do logic: Hết hàng, không cho phép hủy, v.v.)
                String backendMessage = response.getMessage();

                if (backendMessage == null || backendMessage.trim().isEmpty()) {
                    backendMessage = "Lỗi không xác định từ hệ thống";
                }

                // [SỬA ĐỔI] Dùng CuteDialog màu đỏ để báo lỗi thay vì Toast
                showErrorDialog(backendMessage);
            }
        });
    }

    // Dialog thành công (Màu xanh)
    private void showSuccessDialog(String message) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_success)
                .setTitle("Thành công")
                .setDescription(message)
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setPositiveButtonText("Đóng", v -> finishActivity())
                .hideNegativeButton(true)
                .show();
    }

    // [MỚI] Dialog báo lỗi (Màu đỏ) - Thay thế cho FancyToast
    private void showErrorDialog(String message) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_error) // Đảm bảo bạn có icon này
                .setTitle("Cập nhật thất bại")
                .setDescription(message)
                .setPrimaryColor(R.color.blue) // Đảm bảo có màu red trong colors.xml
                .setPositiveButtonColor(R.color.blue)
                .setPositiveButtonText("Đóng", v -> {})
                .hideNegativeButton(true)
                .show();
    }

    private void finishActivity() {
        if (getActivity() != null) {
            getActivity().setResult(Activity.RESULT_OK);
            getActivity().finish();
        }
    }

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