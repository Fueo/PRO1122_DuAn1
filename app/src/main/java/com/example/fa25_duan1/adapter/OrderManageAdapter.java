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

public class OrderManageAdapter extends RecyclerView.Adapter<OrderManageAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orders;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderManageAdapter(Context context, List<Order> orders, OnOrderClickListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
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

        DecimalFormat formatter = new DecimalFormat("###,###,###");

        // ========= HEADER ==========
        holder.tvOrderCode.setText(order.getId());
        holder.tvReceiverName.setText(order.getFullname());
        holder.tvPaymentMethod.setText(order.getPaymentMethod());
        holder.tvTotal.setText(formatter.format(order.getTotal()) + " đ");

        // [SỬA ĐỔI QUAN TRỌNG]: Xử lý ngày giờ từ UTC sang Local (UTC+7)
        holder.tvDate.setText(convertUtcToLocal(order.getDate()));

        holder.tvAddress.setText(order.getAddress());
        holder.tvPhone.setText(order.getPhone());
        holder.btnDetail.setText("Cập nhật đơn");
        // ========= STATUS UI ==========
        String status = order.getStatus() != null ? order.getStatus().toLowerCase().trim() : "";

        switch (status) {
            case "pending":
            case "chờ xác nhận":
                holder.tvStatus.setText("Chờ xác nhận");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_processing);
                holder.tvStatus.setTextColor(Color.parseColor("#0486E9"));
                break;

            case "processing":
            case "đang xử lý":
                holder.tvStatus.setText("Đang xử lý");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_processing);
                holder.tvStatus.setTextColor(Color.parseColor("#0486E9"));
                break;

            case "shipping":
            case "shipped":
            case "đang giao hàng":
                holder.tvStatus.setText("Đang giao");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_processing);
                holder.tvStatus.setTextColor(Color.parseColor("#FF8C00"));
                break;

            case "delivered":
            case "completed":
            case "hoàn thành":
                holder.tvStatus.setText("Hoàn thành");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_done);
                holder.tvStatus.setTextColor(Color.parseColor("#188038"));
                break;

            case "cancelled":
            case "đã hủy":
                holder.tvStatus.setText("Đã hủy");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_canceled);
                holder.tvStatus.setTextColor(Color.parseColor("#FF3B30"));
                break;

            default:
                holder.tvStatus.setText(status);
                break;
        }

        // ========= BUTTON VISIBILITY ==========
        holder.btnCancel.setVisibility(View.GONE);
        holder.btnBuyAgain.setVisibility(View.GONE);
        holder.btnDetail.setVisibility(View.VISIBLE);

        holder.btnDetail.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });

        // ========= ITEMS SẢN PHẨM ==========
        if (order.getOrderDetails() != null) {
            OrderItemAdapter itemAdapter = new OrderItemAdapter(order.getOrderDetails());
            holder.rvOrderItems.setAdapter(itemAdapter);
        }

        // ========= EXPAND / COLLAPSE ==========
        holder.layoutDetail.setExpanded(order.isExpanded(), false);
        updateToggleIcon(holder, order.isExpanded());

        View.OnClickListener expandListener = v -> {
            boolean willExpand = !order.isExpanded();
            order.setExpanded(willExpand);
            if (willExpand) holder.layoutDetail.expand();
            else holder.layoutDetail.collapse();
            updateToggleIcon(holder, willExpand);
        };

        holder.imgToggleHeader.setOnClickListener(expandListener);
        holder.imgToggleDetail.setOnClickListener(expandListener);
    }

    /**
     * Hàm chuyển đổi thời gian UTC từ Server sang giờ địa phương (Việt Nam)
     */
    private String convertUtcToLocal(String utcDateString) {
        if (utcDateString == null || utcDateString.isEmpty()) return "";

        try {
            // 1. Định dạng đầu vào (Input): UTC
            // Chuỗi server thường dạng: "2025-11-20T14:30:00.000Z"
            SimpleDateFormat inputFormat;
            if (utcDateString.contains(".")) {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            } else {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            }
            // QUAN TRỌNG: Phải set TimeZone input là UTC để máy hiểu đây là giờ quốc tế
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = inputFormat.parse(utcDateString);

            // 2. Định dạng đầu ra (Output): Giờ địa phương
            // Không setTimeZone cho outputFormat -> Nó sẽ tự lấy giờ trên máy (UTC+7)
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());

            return outputFormat.format(date);

        } catch (Exception e) {
            // Nếu lỗi parse (do server trả về format lạ), fallback về cắt chuỗi đơn giản
            e.printStackTrace();
            if (utcDateString.contains("T")) {
                return utcDateString.replace("T", " ").substring(0, 16);
            }
            return utcDateString;
        }
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