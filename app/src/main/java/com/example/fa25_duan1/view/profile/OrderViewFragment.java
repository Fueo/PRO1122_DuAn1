package com.example.fa25_duan1.view.profile;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

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
import com.shashank.sony.fancytoastlib.FancyToast;

import org.angmarch.views.NiceSpinner;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone; // [QUAN TRỌNG] Import TimeZone

public class OrderViewFragment extends Fragment {

    private OrderViewModel viewModel;
    private Order currentOrder;
    private String orderId;

    // UI
    private TextView tvOrderId, tvDate, tvFullname, tvPhone, tvAddress, tvStatusTitle;
    private TextView tvPaymentMethod, tvNote, tvShippingFee, tvTotal;
    private NiceSpinner spinnerStatus;
    private Button btnUpdateStatus;
    private RecyclerView rcvOrderDetails;
    private OrderItemAdapter adapter;

    private final DecimalFormat currencyFormat = new DecimalFormat("###,###,###");
    private final double SHIPPING_FEE = 25000;

    // 1. Tạo danh sách đầy đủ
    private final List<String> statusList = new LinkedList<>(Arrays.asList(
            "Chờ xác nhận",
            "Đang xử lý",
            "Đang giao hàng",
            "Hoàn thành",
            "Đã hủy"
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

        if (getActivity() != null && getActivity().getIntent() != null) {
            orderId = getActivity().getIntent().getStringExtra("orderId");
        }

        if (orderId != null) {
            fetchOrderDetails(orderId);
        } else {
            FancyToast.makeText(getContext(), "Lỗi: ID đơn hàng không tồn tại", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
        }
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
    private void initViews(View view) {
        tvOrderId = view.findViewById(R.id.tvOrderId);
        tvStatusTitle = view.findViewById(R.id.tvStatusTitle);
        tvDate = view.findViewById(R.id.tvDate);
        spinnerStatus = view.findViewById(R.id.spinnerStatus);

        btnUpdateStatus = view.findViewById(R.id.btnUpdateStatus);

        // 2. Ẩn nút cập nhật
        if (btnUpdateStatus != null) {
            btnUpdateStatus.setVisibility(View.GONE);
        }

        // 3. LOCK SPINNER
        spinnerStatus.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        tvFullname = view.findViewById(R.id.tvFullname);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvAddress = view.findViewById(R.id.tvAddress);

        rcvOrderDetails = view.findViewById(R.id.rcvOrderDetails);
        rcvOrderDetails.setLayoutManager(new LinearLayoutManager(getContext()));

        tvPaymentMethod = view.findViewById(R.id.tvPaymentMethod);
        tvNote = view.findViewById(R.id.tvNote);
        tvShippingFee = view.findViewById(R.id.tvShippingFee);
        tvTotal = view.findViewById(R.id.tvTotal);
    }

    private void populateData(Order order) {
        tvOrderId.setText("Mã đơn: " + order.getId());

        // Gọi hàm formatDateTime đã sửa
        tvDate.setText("Ngày đặt: " + formatDateTime(order.getDate()));

        tvFullname.setText(order.getFullname());
        tvPhone.setText(order.getPhone());
        tvAddress.setText(order.getAddress());

        if (order.getOrderDetails() != null) {
            adapter = new OrderItemAdapter(order.getOrderDetails());
            rcvOrderDetails.setAdapter(adapter);
        }
        tvStatusTitle.setText("Trạng thái đơn");
        tvPaymentMethod.setText("Phương thức: " + order.getPaymentMethod());
        tvNote.setText("Ghi chú: " + (order.getNote() != null ? order.getNote() : "Không có"));

        tvShippingFee.setText(currencyFormat.format(SHIPPING_FEE) + " đ");
        tvTotal.setText(currencyFormat.format(order.getTotal()) + " đ");

        // --- XỬ LÝ SPINNER ---
        spinnerStatus.attachDataSource(statusList);
        int index = getStatusIndex(order.getStatus());
        if (index != -1) {
            spinnerStatus.setSelectedIndex(index);
        }
    }

    // [CẬP NHẬT] Hàm xử lý chuyển đổi UTC -> Local Time (UTC+7)
    private String formatDateTime(String inputDate) {
        if (inputDate == null) return "";
        try {
            SimpleDateFormat inputFormat;
            // Xác định định dạng đầu vào từ Server
            if (inputDate.contains(".")) {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            } else {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            }

            // [QUAN TRỌNG] Bắt buộc set TimeZone input là UTC
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = inputFormat.parse(inputDate);

            // Định dạng đầu ra: Không set TimeZone -> Tự lấy giờ Local (VN)
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
            return outputFormat.format(date);

        } catch (Exception e) {
            // Fallback nếu lỗi format
            return inputDate.replace("T", " ").split("\\.")[0];
        }
    }

    private int getStatusIndex(String statusFromServer) {
        if (statusFromServer == null) return 0;
        String status = statusFromServer.toLowerCase().trim();
        if (status.equals("pending") || status.equals("chờ xác nhận")) return 0;
        if (status.equals("processing") || status.equals("confirmed") || status.equals("đang xử lý")) return 1;
        if (status.equals("shipping") || status.equals("shipped") || status.equals("đang giao hàng")) return 2;
        if (status.equals("delivered") || status.equals("completed") || status.equals("hoàn thành")) return 3;
        if (status.equals("cancelled") || status.equals("đã hủy")) return 4;
        return 0;
    }
}