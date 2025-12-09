package com.example.fa25_duan1.view.profile;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.example.fa25_duan1.viewmodel.OrderViewModel;
import com.shashank.sony.fancytoastlib.FancyToast;

import org.angmarch.views.NiceSpinner;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class OrderViewFragment extends Fragment {

    private OrderViewModel viewModel;
    private Order currentOrder;
    private String orderId;

    // UI
    private TextView tvOrderId, tvDate, tvFullname, tvPhone, tvAddress, tvStatusTitle;
    private TextView tvNote, tvShippingFee, tvTotal;

    // Spinner
    private NiceSpinner spinnerStatus, spinnerPaymentMethod, spinnerPaymentStatus;
    private Button btnUpdateStatus;

    private RecyclerView rcvOrderDetails;
    private OrderItemAdapter adapter;

    private final DecimalFormat currencyFormat = new DecimalFormat("###,###,###");
    private final double SHIPPING_FEE = 25000;

    // 1. Danh sách Trạng thái đơn hàng (User chỉ xem, không sửa được)
    private final List<String> statusList = new LinkedList<>(Arrays.asList(
            "Chờ xác nhận", "Đang xử lý", "Đang giao hàng", "Hoàn thành", "Đã hủy"
    ));

    // 2.1. Danh sách Phương thức thanh toán (HIỂN THỊ TIẾNG VIỆT)
    private final List<String> paymentMethodDisplayList = new LinkedList<>(Arrays.asList(
            "Thanh toán khi nhận hàng (COD)",
            "Chuyển khoản ngân hàng",
            "Ví điện tử ZaloPay"
    ));

    // 2.2. Danh sách Mã Phương thức thanh toán (SERVER CODE - Map 1:1 với list trên)
    private final List<String> paymentMethodCodeList = new LinkedList<>(Arrays.asList(
            "COD",
            "QR",
            "Zalopay"
    ));

    // 3. Danh sách Trạng thái thanh toán (User chỉ xem)
    private final List<String> paymentStatusList = new LinkedList<>(Arrays.asList(
            "Chưa thanh toán", "Đã thanh toán"
    ));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Lưu ý: Đảm bảo layout này có đủ các ID tương ứng
        return inflater.inflate(R.layout.fragment_orderupdate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(OrderViewModel.class);

        initViews(view);
        setupSpinners(); // Cài đặt datasource cho spinner
        setupEvents();   // Cài đặt sự kiện click

        if (getActivity() != null && getActivity().getIntent() != null) {
            orderId = getActivity().getIntent().getStringExtra("orderId");
        }

        if (orderId != null) {
            fetchOrderDetails(orderId);
        } else {
            FancyToast.makeText(getContext(), "Lỗi: ID đơn hàng không tồn tại", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViews(View view) {
        tvOrderId = view.findViewById(R.id.tvOrderId);
        tvStatusTitle = view.findViewById(R.id.tvStatusTitle);
        tvDate = view.findViewById(R.id.tvDate);

        spinnerStatus = view.findViewById(R.id.spinnerStatus);
        spinnerPaymentMethod = view.findViewById(R.id.spinnerPaymentMethod);
        spinnerPaymentStatus = view.findViewById(R.id.spinnerPaymentStatus);

        btnUpdateStatus = view.findViewById(R.id.btnUpdateStatus);

        tvFullname = view.findViewById(R.id.tvFullname);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvAddress = view.findViewById(R.id.tvAddress);

        rcvOrderDetails = view.findViewById(R.id.rcvOrderDetails);
        rcvOrderDetails.setLayoutManager(new LinearLayoutManager(getContext()));

        tvNote = view.findViewById(R.id.tvNote);
        tvShippingFee = view.findViewById(R.id.tvShippingFee);
        tvTotal = view.findViewById(R.id.tvTotal);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupSpinners() {
        // 1. Status: Attach data & Lock interaction (User ko đc sửa trạng thái đơn)
        spinnerStatus.attachDataSource(statusList);
        spinnerStatus.setOnTouchListener((v, event) -> true);

        // 2. Payment Method: Attach Display List (Tiếng Việt)
        spinnerPaymentMethod.attachDataSource(paymentMethodDisplayList);
        // Interaction sẽ được bật/tắt tùy theo trạng thái đơn hàng ở hàm populateData

        // 3. Payment Status: Attach data & Lock interaction (User ko đc sửa trạng thái đã tt hay chưa)
        spinnerPaymentStatus.attachDataSource(paymentStatusList);
        spinnerPaymentStatus.setOnTouchListener((v, event) -> true);
    }

    private void fetchOrderDetails(String id) {
        viewModel.getOrderById(id).observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isStatus() && response.getData() != null) {
                currentOrder = response.getData();
                populateData(currentOrder);
            } else {
                FancyToast.makeText(getContext(), "Lỗi tải dữ liệu", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void populateData(Order order) {
        tvOrderId.setText("Mã đơn: " + order.getId());
        tvDate.setText("Ngày đặt: " + formatDateTime(order.getDate()));
        tvFullname.setText(order.getFullname());
        tvPhone.setText(order.getPhone());
        tvAddress.setText(order.getAddress());

        if (order.getOrderDetails() != null) {
            adapter = new OrderItemAdapter(order.getOrderDetails());
            rcvOrderDetails.setAdapter(adapter);
        }

        tvStatusTitle.setText("Trạng thái đơn");
        tvNote.setText("Ghi chú: " + (order.getNote() != null ? order.getNote() : "Không có"));
        tvShippingFee.setText(currencyFormat.format(SHIPPING_FEE) + " đ");
        tvTotal.setText(currencyFormat.format(order.getTotal()) + " đ");

        // --- 1. Set giá trị cho Spinner Status ---
        int statusIndex = getStatusIndex(order.getStatus());
        if (statusIndex != -1) spinnerStatus.setSelectedIndex(statusIndex);

        // --- 2. Set giá trị cho Spinner Payment Method (MAP CODE -> INDEX) ---
        String serverMethod = order.getPaymentMethod(); // "QR", "COD", "Zalopay"
        int methodIndex = 0; // Default COD
        if (serverMethod != null) {
            for (int i = 0; i < paymentMethodCodeList.size(); i++) {
                if (paymentMethodCodeList.get(i).equalsIgnoreCase(serverMethod)) {
                    methodIndex = i;
                    break;
                }
            }
        }
        spinnerPaymentMethod.setSelectedIndex(methodIndex);

        // --- 3. Set giá trị cho Spinner Payment Status ---
        int paidIndex = order.isPaid() ? 1 : 0;
        spinnerPaymentStatus.setSelectedIndex(paidIndex);

        // --- 4. LOGIC ẨN/HIỆN NÚT CẬP NHẬT & KHÓA SPINNER ---
        // Chỉ cho phép user đổi phương thức thanh toán khi đơn là "Pending"
        if ("Pending".equalsIgnoreCase(order.getStatus()) || "Wait_confirm".equalsIgnoreCase(order.getStatus())) {
            btnUpdateStatus.setVisibility(View.VISIBLE);

            // Unlock Spinner Payment Method
            spinnerPaymentMethod.setOnTouchListener(null);
            spinnerPaymentMethod.setEnabled(true);
        } else {
            // Đơn đã xử lý -> Khóa hết
            btnUpdateStatus.setVisibility(View.GONE);

            // Lock Spinner
            spinnerPaymentMethod.setOnTouchListener((v, event) -> true);
        }
    }

    private void setupEvents() {
        btnUpdateStatus.setOnClickListener(v -> {
            if (currentOrder == null) return;

            // 1. Lấy index đang chọn trên UI
            int selectedIndex = spinnerPaymentMethod.getSelectedIndex();

            // 2. Map Index -> Code Server (QR/COD/Zalopay)
            if (selectedIndex < 0 || selectedIndex >= paymentMethodCodeList.size()) selectedIndex = 0;
            String selectedCode = paymentMethodCodeList.get(selectedIndex);

            // Kiểm tra xem có thay đổi không
            if (selectedCode.equalsIgnoreCase(currentOrder.getPaymentMethod())) {
                FancyToast.makeText(getContext(), "Phương thức thanh toán chưa thay đổi", FancyToast.LENGTH_SHORT, FancyToast.INFO, true).show();
                return;
            }

            // 3. Gọi API Update (Gửi Code đi)
            viewModel.updatePaymentMethod(currentOrder.getId(), selectedCode)
                    .observe(getViewLifecycleOwner(), response -> {
                        if (response != null && response.isStatus()) {
                            FancyToast.makeText(getContext(), "Cập nhật thành công!", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
                            fetchOrderDetails(currentOrder.getId()); // Refresh lại data
                        } else {
                            String msg = response != null ? response.getMessage() : "Cập nhật thất bại";
                            FancyToast.makeText(getContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                        }
                    });
        });
    }

    private String formatDateTime(String inputDate) {
        if (inputDate == null) return "";
        try {
            SimpleDateFormat inputFormat;
            if (inputDate.contains(".")) {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            } else {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            }
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
        if (status.equals("pending") || status.equals("chờ xác nhận")) return 0;
        if (status.equals("processing") || status.equals("confirmed") || status.equals("đang xử lý")) return 1;
        if (status.equals("shipping") || status.equals("shipped") || status.contains("đang giao")) return 2;
        if (status.equals("delivered") || status.equals("completed") || status.equals("hoàn thành")) return 3;
        if (status.equals("cancelled") || status.equals("đã hủy")) return 4;
        return 0;
    }
}