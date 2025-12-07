package com.example.fa25_duan1.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton; // Dùng AppCompatButton cho các nút mới
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.Order;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orders;
    private OnOrderActionListener actionListener;

    public interface OnOrderActionListener {
        void onCancelOrder(String orderId);
        void onRepurchase(Order order);
        void onViewDetail(Order order);
        void onPayNowClick(Order order); // [MỚI] Callback cho nút Thanh toán
    }

    public OrderAdapter(Context context, List<Order> orders, OnOrderActionListener listener) {
        this.context = context;
        this.orders = orders;
        this.actionListener = listener;
    }

    public void setData(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        if (order == null) return;

        // --- 1. BIND DATA CƠ BẢN ---
        holder.tvOrderCode.setText(order.getId()); // Hoặc hiển thị transactionCode nếu muốn ngắn gọn
        holder.tvReceiverName.setText(order.getFullname());

        DecimalFormat formatter = new DecimalFormat("###,###,###");
        holder.tvTotal.setText(formatter.format(order.getTotal()) + " đ");

        holder.tvDate.setText(convertUtcToLocal(order.getDate()));
        holder.tvAddress.setText(order.getAddress());
        holder.tvPhone.setText(order.getPhone());
        holder.tvPaymentMethod.setText(order.getPaymentMethod());

        // --- 2. XỬ LÝ TRẠNG THÁI ĐƠN HÀNG (Status) ---
        String status = order.getStatus() != null ? order.getStatus().toLowerCase().trim() : "";

        // Reset mặc định các nút
        holder.btnCancel.setVisibility(View.GONE);
        holder.btnBuyAgain.setVisibility(View.GONE);
        holder.btnPayNow.setVisibility(View.GONE);

        switch (status) {
            case "pending":
            case "chờ xác nhận":
                holder.tvStatus.setText("Chờ xác nhận");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_processing);
                holder.tvStatus.setTextColor(Color.parseColor("#0486E9"));
                holder.btnCancel.setVisibility(View.VISIBLE); // Được hủy khi chưa xử lý
                break;

            case "processing":
            case "đang xử lý":
                holder.tvStatus.setText("Đang xử lý");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_processing);
                holder.tvStatus.setTextColor(Color.parseColor("#0486E9"));
                holder.btnCancel.setVisibility(View.VISIBLE);
                break;

            case "shipping":
            case "shipped":
            case "đang giao hàng":
            case "đang giao":
                holder.tvStatus.setText("Đang giao");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_processing);
                holder.tvStatus.setTextColor(Color.parseColor("#FF8C00"));
                // Không hiện nút hủy khi đang giao
                break;

            case "delivered":
            case "completed":
            case "hoàn thành":
                holder.tvStatus.setText("Hoàn thành");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_done);
                holder.tvStatus.setTextColor(Color.parseColor("#188038"));
                holder.btnBuyAgain.setVisibility(View.VISIBLE);
                break;

            case "cancelled":
            case "đã hủy":
                holder.tvStatus.setText("Đã hủy");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_canceled);
                holder.tvStatus.setTextColor(Color.parseColor("#FF3B30"));
                holder.btnBuyAgain.setVisibility(View.VISIBLE);
                break;

            default:
                holder.tvStatus.setText(status);
                break;
        }

        // --- 3. [MỚI] XỬ LÝ TRẠNG THÁI THANH TOÁN (IsPaid & Button PayNow) ---
        boolean isPaid = order.isPaid();
        String paymentMethod = order.getPaymentMethod(); // "Chuyển khoản ngân hàng" hoặc "Thanh toán khi nhận hàng"

        if (isPaid) {
            // Đã thanh toán -> Hiện Badge Xanh
            holder.tvPaymentStatus.setVisibility(View.VISIBLE);
            holder.tvPaymentStatus.setText("Đã thanh toán");
            holder.tvPaymentStatus.setTextColor(Color.parseColor("#2E7D32")); // Xanh lá đậm
            holder.btnPayNow.setVisibility(View.GONE); // Đã trả rồi thì ẩn nút thanh toán
        } else {
            // Chưa thanh toán
            if ("Chuyển khoản ngân hàng".equals(paymentMethod)) {
                // Hiện Badge Cam "Chờ thanh toán"
                holder.tvPaymentStatus.setVisibility(View.VISIBLE);
                holder.tvPaymentStatus.setText("Chờ thanh toán");
                holder.tvPaymentStatus.setTextColor(Color.parseColor("#EF6C00")); // Cam đậm

                // Logic hiện nút Thanh toán: Chỉ hiện khi Đơn đang Pending/Processing (chưa hủy, chưa xong)
                if ("pending".equals(status) || "chờ xác nhận".equals(status) || "processing".equals(status)) {
                    holder.btnPayNow.setVisibility(View.VISIBLE);
                }
            } else {
                // COD -> Ẩn Badge
                holder.tvPaymentStatus.setVisibility(View.GONE);
                holder.btnPayNow.setVisibility(View.GONE);
            }
        }

        // --- 4. SỰ KIỆN CLICK ---
        holder.btnCancel.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onCancelOrder(order.getId());
        });

        holder.btnBuyAgain.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onRepurchase(order);
        });

        holder.btnPayNow.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onPayNowClick(order);
        });

        holder.btnDetail.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onViewDetail(order);
        });

        // --- 5. EXPANDABLE LIST ---
        if (order.getOrderDetails() != null) {
            OrderItemAdapter itemAdapter = new OrderItemAdapter(order.getOrderDetails());
            holder.rvOrderItems.setAdapter(itemAdapter);
        }

        holder.layoutDetail.setExpanded(order.isExpanded(), false);
        updateToggleIcon(holder, order.isExpanded());

        View.OnClickListener expandListener = v -> toggleExpand(holder, order);
        holder.imgToggleHeader.setOnClickListener(expandListener);
        holder.imgToggleDetail.setOnClickListener(expandListener);
    }

    private String convertUtcToLocal(String utcDateString) {
        if (utcDateString == null || utcDateString.isEmpty()) return "";
        try {
            SimpleDateFormat inputFormat;
            if (utcDateString.contains(".")) {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            } else {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            }
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = inputFormat.parse(utcDateString);
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            if (utcDateString.contains("T")) {
                return utcDateString.replace("T", " ").split("\\.")[0];
            }
            return utcDateString;
        }
    }

    private void toggleExpand(OrderViewHolder holder, Order order) {
        boolean willExpand = !order.isExpanded();
        order.setExpanded(willExpand);
        if (willExpand) {
            holder.layoutDetail.expand();
        } else {
            holder.layoutDetail.collapse();
        }
        updateToggleIcon(holder, willExpand);
    }

    private void updateToggleIcon(OrderViewHolder holder, boolean isExpanded) {
        if (isExpanded) {
            holder.imgToggleHeader.setVisibility(View.GONE);
            holder.imgToggleDetail.setVisibility(View.VISIBLE);
        } else {
            holder.imgToggleHeader.setVisibility(View.VISIBLE);
            holder.imgToggleDetail.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return orders == null ? 0 : orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutHeader;
        ExpandableLayout layoutDetail;

        // TextViews
        TextView tvOrderCode, tvReceiverName, tvPaymentMethod, tvTotal, tvStatus, tvDate, tvAddress, tvPhone;
        TextView tvPaymentStatus; // [MỚI] Badge trạng thái thanh toán

        ImageView imgToggleHeader, imgToggleDetail;
        RecyclerView rvOrderItems;

        // Buttons (Dùng AppCompatButton cho đồng bộ)
        AppCompatButton btnCancel, btnBuyAgain, btnDetail, btnPayNow; // [MỚI] btnPayNow

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutHeader = itemView.findViewById(R.id.layoutHeader);
            layoutDetail = itemView.findViewById(R.id.layoutDetail);

            tvOrderCode = itemView.findViewById(R.id.tvOrderCode);
            tvReceiverName = itemView.findViewById(R.id.tvReceiverName);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvPhone = itemView.findViewById(R.id.tvPhone);

            // [MỚI] Ánh xạ Badge
            tvPaymentStatus = itemView.findViewById(R.id.tvPaymentStatus);

            imgToggleHeader = itemView.findViewById(R.id.imgToggleHeader);
            imgToggleDetail = itemView.findViewById(R.id.imgToggleDetail);

            rvOrderItems = itemView.findViewById(R.id.rvOrderItems);
            rvOrderItems.setLayoutManager(new LinearLayoutManager(itemView.getContext()));

            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnBuyAgain = itemView.findViewById(R.id.btnBuyAgain);
            btnDetail = itemView.findViewById(R.id.btnDetail);

            // [MỚI] Ánh xạ Nút Thanh toán
            btnPayNow = itemView.findViewById(R.id.btnPayNow);
        }
    }
}