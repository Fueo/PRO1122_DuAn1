package com.example.fa25_duan1.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.OrderDetail;

import java.text.DecimalFormat;
import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ItemViewHolder> {

    private List<OrderDetail> items;

    public OrderItemAdapter(List<OrderDetail> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_detail, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        OrderDetail item = items.get(position);
        if (item == null) return;

        // 1. Lấy context từ itemView để dùng cho Glide
        Context context = holder.itemView.getContext();

        holder.tvProductName.setText(item.getProductName());

        // 2. Hiển thị số lượng: "x 2"
        holder.tvProductQuantity.setText("x " + item.getQuantity());

        // 3. Hiển thị Thành tiền (Format đẹp: 100.000 đ)
        // Lưu ý: Dùng item.getTotalPrice() chứ không dùng getQuantity()
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        String priceString = formatter.format(item.getTotalPrice()) + " đ";
        holder.tvProductAmount.setText(priceString);

        // 4. Load ảnh (Sửa holder.imgBook -> holder.imgProduct)
        Glide.with(context)
                .load(item.getProductImage())
                .placeholder(R.drawable.ic_launcher_background) // (Tùy chọn) Ảnh chờ khi đang load
                .error(R.drawable.ic_launcher_background)       // (Tùy chọn) Ảnh lỗi
                .into(holder.imgProduct);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName, tvProductQuantity, tvProductAmount;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            // Đảm bảo ID trong file XML (item_order_products.xml) khớp với các ID này
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
            tvProductAmount = itemView.findViewById(R.id.tvProductAmount);
        }
    }
}