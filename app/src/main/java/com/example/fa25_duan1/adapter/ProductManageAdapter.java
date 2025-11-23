package com.example.fa25_duan1.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductManageAdapter extends RecyclerView.Adapter<ProductManageAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> listProduct;
    private OnProductActionListener listener;

    public interface OnProductActionListener {
        void onEditClick(Product product);
        void onDeleteClick(Product product);
    }

    public ProductManageAdapter(Context context, List<Product> listProduct, OnProductActionListener listener) {
        this.context = context;
        this.listProduct = listProduct;
        this.listener = listener;
    }

    public void setData(List<Product> list) {
        this.listProduct = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_productmanage, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = listProduct.get(position);
        if (product == null) return;

        // 1. Load Ảnh
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            Glide.with(context)
                    .load(product.getImage())
                    .placeholder(R.drawable.book_cover_placeholder) // Đảm bảo bạn có ảnh này
                    .error(R.drawable.book_cover_placeholder)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.book_cover_placeholder);
        }

        // 2. Tên và Giá
        holder.tvName.setText(product.getName());
        holder.tvAuthor.setText(product.getAuthor().getName());

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedPrice = format.format(product.getPrice()).replace("₫", " đ");
        holder.tvPrice.setText(formattedPrice);

        // 3. Số lượng tồn kho
        holder.tvQuantity.setText(String.valueOf(product.getQuantity()));

        // 4. XỬ LÝ TRẠNG THÁI (STATUS) --- ĐÃ SỬA ---
        if (product.isStatus()) {
            // Trường hợp: Đang bán
            holder.tvStatus.setText("Đang bán");
            // Set màu xanh dương như trong layout cũ (#0486E9)
            holder.tvStatus.setTextColor(Color.parseColor("#0486E9"));
            // Set background xanh nhạt (giữ nguyên background gốc trong xml)
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_processing);

            // Tên sách màu đen bình thường
            holder.tvName.setTextColor(context.getResources().getColor(R.color.black));
        } else {
            // Trường hợp: Ngừng kinh doanh
            holder.tvStatus.setText("Ngừng kinh doanh");
            // Set màu đỏ để cảnh báo
            holder.tvStatus.setTextColor(Color.RED);
            // Bạn có thể tạo thêm drawable bg_status_stopped màu đỏ nhạt nếu muốn,
            // tạm thời dùng chung background nhưng đổi màu chữ.
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_processing);

            // Tên sách màu xám để thể hiện bị vô hiệu hóa
            holder.tvName.setTextColor(context.getResources().getColor(R.color.gray_text));
        }

        // 5. Sự kiện Click
        if (listener != null) {
            holder.btnEdit.setOnClickListener(v -> listener.onEditClick(product));
            holder.ivDelete.setOnClickListener(v -> listener.onDeleteClick(product));
        }
    }

    @Override
    public int getItemCount() {
        return listProduct != null ? listProduct.size() : 0;
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        ImageView ivDelete;
        TextView tvName;
        TextView tvPrice;
        Button btnEdit;
        TextView tvQuantity;
        TextView tvStatus; // --- KHAI BÁO THÊM ---
        TextView tvAuthor;
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
        }
    }
}