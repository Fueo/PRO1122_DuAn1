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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.Order;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat; // Import Date parsing
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone; // Import TimeZone

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orders;
    private OnOrderActionListener actionListener;

    public interface OnOrderActionListener {
        void onCancelOrder(String orderId);
        void onRepurchase(Order order);
        void onViewDetail(Order order);
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

        // --- BIND DATA CƠ BẢN ---
        holder.tvOrderCode.setText(order.getId());
        holder.tvReceiverName.setText(order.getFullname());

        DecimalFormat formatter = new DecimalFormat("###,###,###");
        holder.tvTotal.setText(formatter.format(order.getTotal()) + " đ");

        // [SỬA ĐỔI]: Dùng hàm convertUtcToLocal thay vì cắt chuỗi
        holder.tvDate.setText(convertUtcToLocal(order.getDate()));

        holder.tvAddress.setText(order.getAddress());
        holder.tvPhone.setText(order.getPhone());
        holder.tvPaymentMethod.setText(order.getPaymentMethod());

        // --- XỬ LÝ TRẠNG THÁI ---
        String status = order.getStatus() != null ? order.getStatus().toLowerCase().trim() : "";

        holder.btnCancel.setVisibility(View.GONE);
        holder.btnBuyAgain.setVisibility(View.GONE);
        holder.btnCancel.setOnClickListener(null);
        holder.btnBuyAgain.setOnClickListener(null);

        switch (status) {
            case "pending":
            case "chờ xác nhận":
                holder.tvStatus.setText("Chờ xác nhận");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_processing);
                holder.tvStatus.setTextColor(Color.parseColor("#0486E9"));
                holder.btnCancel.setVisibility(View.VISIBLE);
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
                holder.btnCancel.setVisibility(View.GONE);
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

        // --- SỰ KIỆN CLICK CÁC NÚT ---
        holder.btnCancel.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onCancelOrder(order.getId());
        });

        holder.btnBuyAgain.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onRepurchase(order);
        });

        holder.btnDetail.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onViewDetail(order);
            }
        });

        // --- EXPANDABLE LOGIC (Chỉ dùng cho mũi tên) ---
        if (order.getOrderDetails() != null) {
            OrderItemAdapter itemAdapter = new OrderItemAdapter(order.getOrderDetails());
            holder.rvOrderItems.setAdapter(itemAdapter);
        }

        holder.layoutDetail.setExpanded(order.isExpanded(), false);
        updateToggleIcon(holder, order.isExpanded());

        View.OnClickListener expandListener = v -> toggleExpand(holder, order);

        // Chỉ click vào mũi tên mới xổ xuống danh sách con
        holder.imgToggleHeader.setOnClickListener(expandListener);
        holder.imgToggleDetail.setOnClickListener(expandListener);
    }

    /**
     * Hàm chuyển đổi thời gian UTC từ Server sang giờ địa phương
     */
    private String convertUtcToLocal(String utcDateString) {
        if (utcDateString == null || utcDateString.isEmpty()) return "";

        try {
            // Định dạng đầu vào (Input): UTC
            SimpleDateFormat inputFormat;
            if (utcDateString.contains(".")) {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            } else {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            }
            // QUAN TRỌNG: Input là UTC
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = inputFormat.parse(utcDateString);

            // Định dạng đầu ra (Output): Giờ địa phương (tự lấy theo máy)
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());

            return outputFormat.format(date);

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback nếu lỗi
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
        TextView tvOrderCode, tvPaymentMethod, tvTotal, tvStatus, tvDate, tvAddress, tvPhone, tvReceiverName;
        ImageView imgToggleHeader, imgToggleDetail;
        RecyclerView rvOrderItems;
        Button btnCancel, btnBuyAgain, btnDetail;

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
            imgToggleHeader = itemView.findViewById(R.id.imgToggleHeader);
            imgToggleDetail = itemView.findViewById(R.id.imgToggleDetail);
            rvOrderItems = itemView.findViewById(R.id.rvOrderItems);
            rvOrderItems.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnBuyAgain = itemView.findViewById(R.id.btnBuyAgain);
            btnDetail = itemView.findViewById(R.id.btnDetail);
        }
    }
}