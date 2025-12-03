package com.example.fa25_duan1.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;

public class OrderManageAdapter extends RecyclerView.Adapter<OrderManageAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orders;
    private OnOrderClickListener listener;

    // ================= CALLBACK ================
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

        if (order.getDate() != null && order.getDate().contains("T")) {
            holder.tvDate.setText(order.getDate().split("T")[0]);
        } else holder.tvDate.setText(order.getDate());

        holder.tvAddress.setText(order.getAddress());
        holder.tvPhone.setText(order.getPhone());

        // ========= STATUS UI ==========
        String status = order.getStatus();
        if (status == null) status = "";

        switch (status.toLowerCase()) {
            case "pending":
                holder.tvStatus.setText("Chờ xác nhận");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_processing);
                holder.tvStatus.setTextColor(Color.parseColor("#0486E9"));
                break;

            case "processing":
                holder.tvStatus.setText("Đang xử lý");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_processing);
                holder.tvStatus.setTextColor(Color.parseColor("#0486E9"));
                break;

            case "shipping":
            case "shipped":
                holder.tvStatus.setText("Đang giao");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_processing);
                holder.tvStatus.setTextColor(Color.parseColor("#FF8C00"));
                break;

            case "delivered":
            case "completed":
                holder.tvStatus.setText("Hoàn thành");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_done);
                holder.tvStatus.setTextColor(Color.parseColor("#188038"));
                break;

            case "cancelled":
                holder.tvStatus.setText("Đã hủy");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_canceled);
                holder.tvStatus.setTextColor(Color.parseColor("#FF3B30"));
                break;
        }

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

        holder.layoutHeader.setOnClickListener(expandListener);
        holder.imgToggleHeader.setOnClickListener(expandListener);
        holder.imgToggleDetail.setOnClickListener(expandListener);

        // ========= CLICK VÀO ITEM (ADMIN) ==========
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOrderClick(order);
        });
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

    // ===================== VIEW HOLDER =====================
    static class OrderViewHolder extends RecyclerView.ViewHolder {

        LinearLayout layoutHeader;
        ExpandableLayout layoutDetail;

        TextView tvOrderCode, tvPaymentMethod, tvTotal, tvStatus, tvDate, tvAddress, tvPhone, tvReceiverName;
        ImageView imgToggleHeader, imgToggleDetail;
        RecyclerView rvOrderItems;

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
        }
    }
}
