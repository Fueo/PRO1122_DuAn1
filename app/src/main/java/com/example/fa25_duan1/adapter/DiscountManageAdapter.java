package com.example.fa25_duan1.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.Discount;

import java.util.List;

public class DiscountManageAdapter extends RecyclerView.Adapter<DiscountManageAdapter.DiscountViewHolder> {

    private Context context;
    private List<Discount> list;
    private OnDiscountActionListener listener;

    public interface OnDiscountActionListener {
        void onEditClick(Discount discount);
        void onDeleteClick(Discount discount);
    }

    public DiscountManageAdapter(Context context, List<Discount> list, OnDiscountActionListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    public void setData(List<Discount> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DiscountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_discount, parent, false);
        return new DiscountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscountViewHolder holder, int position) {
        Discount discount = list.get(position);
        if (discount == null) return;

        holder.tvName.setText(discount.getDiscountName());
        holder.tvValue.setText("Giảm " + (int)discount.getDiscountRate() + "%"); // Ép kiểu về int cho đẹp

        // Format ngày tháng từ String ISO sang dd/MM/yyyy cho dễ đọc
        holder.tvExpiry.setText("HSD: " + formatDate(discount.getEndDate()));

        holder.ivEdit.setOnClickListener(v -> listener.onEditClick(discount));
        holder.ivDelete.setOnClickListener(v -> listener.onDeleteClick(discount));
    }

    private String formatDate(String dateString) {
        try {
            // Giả sử server trả về format ISO 8601, ví dụ: "2025-11-20T00:00:00.000Z"
            // Cần parse đúng format server gửi về. Ở đây tôi demo format đơn giản.
            // Nếu bạn dùng thư viện Gson/Moshi nó có thể tự parse Date, nếu là String thì xử lý tay:
            if(dateString == null) return "";
            // Cắt chuỗi lấy phần ngày tháng cơ bản nếu server gửi format dài
            return dateString.split("T")[0];
        } catch (Exception e) {
            return dateString;
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public class DiscountViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvValue, tvExpiry;
        ImageView ivEdit, ivDelete;

        public DiscountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_discount_code);
            tvValue = itemView.findViewById(R.id.tv_discount_value);
            tvExpiry = itemView.findViewById(R.id.tv_discount_expiry);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }
    }
}