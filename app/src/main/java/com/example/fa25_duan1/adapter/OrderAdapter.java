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
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orders;
    private OnOrderActionListener actionListener;

    public interface OnOrderActionListener {
        void onCancelOrder(String orderId);
    }

    public OrderAdapter(Context context, List<Order> orders, OnOrderActionListener listener) {
        this.context = context;
        this.orders = orders;
        this.actionListener = listener;
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

        // 1. HEADER INFO
        // Kiểm tra null cho orderId để tránh crash
        holder.tvOrderCode.setText(order.getId());
        holder.tvReceiverName.setText(order.getFullname());
        holder.tvPaymentMethod.setText(order.getPaymentMethod());

        DecimalFormat formatter = new DecimalFormat("###,###,###");
        holder.tvTotal.setText(formatter.format(order.getTotal()) + " đ");

        if (order.getDate() != null && order.getDate().contains("T")) {
            holder.tvDate.setText(order.getDate().split("T")[0]);
        } else {
            holder.tvDate.setText(order.getDate());
        }

        holder.tvAddress.setText(order.getAddress());
        holder.tvPhone.setText(order.getPhone());

        // 2. TRẠNG THÁI & NÚT BẤM (CẬP NHẬT STYLE)
        String status = order.getStatus();

        // Mặc định ẩn nút
        holder.btnAction.setVisibility(View.GONE);
        holder.btnAction.setOnClickListener(null);

        if ("Đang xử lý".equals(status)) {
            // --- Trạng thái: ĐANG XỬ LÝ ---
            holder.tvStatus.setText("Đang xử lý");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_processing);
            holder.tvStatus.setTextColor(Color.parseColor("#0486E9"));

            // Hiển thị nút HỦY ĐƠN (Style Đỏ)
            holder.btnAction.setVisibility(View.VISIBLE);
            holder.btnAction.setText("Hủy đơn hàng");

            // [SỬA] Dùng setBackgroundResource để giữ bo góc
            holder.btnAction.setBackgroundResource(R.drawable.bg_btn_cancel);
            holder.btnAction.setTextColor(Color.WHITE); // Chữ trắng trên nền đỏ

            holder.btnAction.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onCancelOrder(order.getId());
            });

        } else if ("Đã hủy".equals(status)) {
            // --- Trạng thái: ĐÃ HỦY ---
            holder.tvStatus.setText("Đã hủy");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_canceled);
            holder.tvStatus.setTextColor(Color.parseColor("#FF3B30"));

            // (Tùy chọn) Nếu muốn hiện nút "Mua lại"

            holder.btnAction.setVisibility(View.VISIBLE);
            holder.btnAction.setText("Mua lại");
            holder.btnAction.setBackgroundResource(R.drawable.bg_btn_buy_again);
            holder.btnAction.setTextColor(Color.parseColor("#0486E9"));

        } else {
            // --- Trạng thái: HOÀN THÀNH / KHÁC ---
            holder.tvStatus.setText("Đã hoàn thành"); // Hoặc hiển thị status gốc
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_done);
            holder.tvStatus.setTextColor(Color.parseColor("#188038"));

            // (Tùy chọn) Hiện nút Mua lại cho đơn hoàn thành
        }

        // 3. ITEMS RECYCLERVIEW
        // Luôn tạo mới adapter để tránh lỗi hiển thị sai item khi tái sử dụng ViewHolder
        if (order.getOrderDetails() != null) {
            OrderItemAdapter itemAdapter = new OrderItemAdapter(order.getOrderDetails());
            holder.rvOrderItems.setAdapter(itemAdapter);
        }

        // 4. EXPANDABLE LOGIC
        holder.layoutDetail.setExpanded(order.isExpanded(), false);
        updateToggleIcon(holder, order.isExpanded());

        View.OnClickListener expandListener = v -> {
            boolean willExpand = !order.isExpanded();
            order.setExpanded(willExpand);

            if (willExpand) {
                holder.layoutDetail.expand();
            } else {
                holder.layoutDetail.collapse();
            }
            updateToggleIcon(holder, willExpand);
        };

        holder.layoutHeader.setOnClickListener(expandListener);
        holder.imgToggleHeader.setOnClickListener(expandListener);
        holder.imgToggleDetail.setOnClickListener(expandListener);
    }

    // Hàm phụ để cập nhật icon mũi tên cho gọn code
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
        // ... (Giữ nguyên như cũ)
        LinearLayout layoutHeader;
        ExpandableLayout layoutDetail;
        TextView tvOrderCode, tvPaymentMethod, tvTotal, tvStatus, tvDate, tvAddress, tvPhone, tvReceiverName;
        ImageView imgToggleHeader, imgToggleDetail;
        RecyclerView rvOrderItems;
        Button btnAction;

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
            // tvReceiverName = itemView.findViewById(R.id.tvReceiverName); // Nếu bạn đã thêm vào XML

            imgToggleHeader = itemView.findViewById(R.id.imgToggleHeader);
            imgToggleDetail = itemView.findViewById(R.id.imgToggleDetail);

            rvOrderItems = itemView.findViewById(R.id.rvOrderItems);
            btnAction = itemView.findViewById(R.id.btnBuyAgain); // ID trong XML là btnBuyAgain

            rvOrderItems.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        }
    }
}