package com.example.fa25_duan1.view.management.order;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import io.github.cutelibs.cutedialog.CuteDialog;
import org.angmarch.views.NiceSpinner;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class OrderUpdateFragment extends Fragment {

    private OrderViewModel viewModel;
    private Order currentOrder;
    private String orderId; // Lưu ID nhận được

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

        // [SỬA ĐỔI]: Nhận ID từ Bundle
        if (getActivity().getIntent() != null) {
            orderId = getActivity().getIntent().getStringExtra("orderId");
        }

        if (orderId != null) {
            // [SỬA ĐỔI]: Gọi hàm load dữ liệu từ Server
            fetchOrderDetails(orderId);
        } else {
            FancyToast.makeText(getContext(), "Lỗi: Không tìm thấy ID đơn hàng", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
            btnUpdateStatus.setEnabled(false);
        }

        btnUpdateStatus.setOnClickListener(v -> updateOrderStatus());
    }

    // --- HÀM LOAD DỮ LIỆU TỪ SERVER ---
    private void fetchOrderDetails(String id) {
        // Hiển thị loading nếu cần
        viewModel.getOrderById(id).observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isStatus() && response.getData() != null) {
                // Lấy được dữ liệu mới nhất -> Gán vào currentOrder và hiển thị
                currentOrder = response.getData();
                populateData(currentOrder);
            } else {
                FancyToast.makeText(getContext(), "Không tải được thông tin đơn hàng", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
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
        tvDate.setText("Ngày đặt: " + order.getDate());
        tvFullname.setText(order.getFullname());
        tvPhone.setText(order.getPhone());
        tvAddress.setText(order.getAddress());

        // List sản phẩm (Đảm bảo model trả về List<OrderDetail>)
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
            if (response != null && response.getData() != null) {
                showSuccessDialog("Cập nhật trạng thái đơn hàng thành công!");
            } else {
                String msg = (response != null) ? response.getMessage() : "Cập nhật thất bại";
                FancyToast.makeText(getContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
            }
        });
    }

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

    private void finishActivity() {
        if (getActivity() != null) {
            getActivity().setResult(Activity.RESULT_OK);
            getActivity().finish();
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