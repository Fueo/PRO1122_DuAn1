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

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orders;

    public OrderAdapter(Context context, List<Order> orders) {
        this.context = context;
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        if (order == null) return;

        // ================= HEADER TEXT =================
        holder.tvOrderCode.setText(order.getOrderCode());
        holder.tvPaymentMethod.setText(order.getPaymentMethod());
        holder.tvTotal.setText(order.getTotal());
        holder.tvDate.setText(order.getDate());
        holder.tvAddress.setText(order.getAddress());
        holder.tvPhone.setText(order.getPhone());

        // =============== TRẠNG THÁI ===============
        String status = order.getStatus();
        if ("processing".equals(status)) {
            holder.tvStatus.setText("Đang xử lý");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_processing);
            holder.tvStatus.setTextColor(Color.parseColor("#0486E9"));
        } else if ("done".equals(status)) {
            holder.tvStatus.setText("Đã hoàn thành");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_done);
            holder.tvStatus.setTextColor(Color.parseColor("#188038"));
        } else if ("canceled".equals(status)) {
            holder.tvStatus.setText("Đã hủy");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_canceled);
            holder.tvStatus.setTextColor(Color.parseColor("#FF3B30"));
        }

        // ================= RECYCLER VIEW ITEMS =================
        // chỉ gán adapter 1 lần DUY NHẤT
        if (holder.rvOrderItems.getAdapter() == null) {
            holder.rvOrderItems.setAdapter(new OrderItemAdapter(order.getProductList()));
        }

        // ================= ĐỒNG BỘ EXPANDED ================
        holder.layoutDetail.setExpanded(order.isExpanded(), false);

        // cập nhật mũi tên
        if (order.isExpanded()) {
            holder.imgToggleHeader.setVisibility(View.GONE);
            holder.imgToggleDetail.setVisibility(View.VISIBLE);
        } else {
            holder.imgToggleHeader.setVisibility(View.VISIBLE);
            holder.imgToggleDetail.setVisibility(View.GONE);
        }

        // ================= CLICK MỞ =================
        View.OnClickListener expandListener = v -> {
            if (!order.isExpanded()) {
                order.setExpanded(true);
                holder.layoutDetail.expand();       // animate mượt
            }
        };

        holder.layoutHeader.setOnClickListener(expandListener);
        holder.imgToggleHeader.setOnClickListener(expandListener);

        // ================= CLICK ĐÓNG =================
        holder.imgToggleDetail.setOnClickListener(v -> {
            if (order.isExpanded()) {
                order.setExpanded(false);
                holder.layoutDetail.collapse();
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders == null ? 0 : orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {

        LinearLayout layoutHeader;
        ExpandableLayout layoutDetail;
        TextView tvOrderCode, tvPaymentMethod, tvTotal, tvStatus, tvDate, tvAddress, tvPhone;
        ImageView imgToggleHeader, imgToggleDetail;
        RecyclerView rvOrderItems;
        Button btnBuyAgain;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutHeader = itemView.findViewById(R.id.layoutHeader);
            layoutDetail = itemView.findViewById(R.id.layoutDetail);

            tvOrderCode = itemView.findViewById(R.id.tvOrderCode);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvPhone = itemView.findViewById(R.id.tvPhone);

            imgToggleHeader = itemView.findViewById(R.id.imgToggleHeader);
            imgToggleDetail = itemView.findViewById(R.id.imgToggleDetail);

            rvOrderItems = itemView.findViewById(R.id.rvOrderItems);
            btnBuyAgain = itemView.findViewById(R.id.btnBuyAgain);

            // Quan trọng: LayoutManager chỉ gán 1 lần
            rvOrderItems.setLayoutManager(
                    new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.VERTICAL, false)
            );

            // Listener cập nhật UI khi animation xong
            layoutDetail.setOnExpansionUpdateListener((fraction, state) -> {
                if (state == ExpandableLayout.State.EXPANDED) {
                    imgToggleHeader.setVisibility(View.GONE);
                    imgToggleDetail.setVisibility(View.VISIBLE);
                } else if (state == ExpandableLayout.State.COLLAPSED) {
                    imgToggleHeader.setVisibility(View.VISIBLE);
                    imgToggleDetail.setVisibility(View.GONE);
                }
            });
        }
    }
}
