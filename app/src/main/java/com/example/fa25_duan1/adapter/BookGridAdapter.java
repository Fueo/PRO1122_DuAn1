package com.example.fa25_duan1.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.Product;

import java.text.DecimalFormat;
import java.util.List;

public class BookGridAdapter extends RecyclerView.Adapter<BookGridAdapter.BookViewHolder> {

    private List<Product> mListProducts;
    private Context context;
    private final DecimalFormat formatter = new DecimalFormat("#,### đ");

    // 1. Khai báo biến Interface
    private OnItemClickListener mListener;

    // 2. Định nghĩa Interface
    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    // 3. Cập nhật Constructor để nhận Listener
    public BookGridAdapter(Context context, List<Product> mListProducts, OnItemClickListener listener) {
        this.context = context;
        this.mListProducts = mListProducts;
        this.mListener = listener;
    }

    // Hàm cập nhật dữ liệu mới
    public void setProducts(List<Product> list) {
        this.mListProducts = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Product product = mListProducts.get(position);
        if (product == null) return;

        // --- Load ảnh ---
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            Glide.with(context)
                    .load(product.getImage())
                    .placeholder(R.drawable.book_cover_placeholder)
                    .error(R.drawable.book_cover_placeholder)
                    .into(holder.ivCover);
        } else {
            holder.ivCover.setImageResource(R.drawable.book_cover_placeholder);
        }

        // --- Set tên sách ---
        holder.tvTitle.setText(product.getName());

        // --- MỚI: Set tên tác giả ---
        if (product.getAuthor() != null && product.getAuthor().getName() != null) {
            holder.tvAuthor.setText(product.getAuthor().getName());
        } else {
            holder.tvAuthor.setText("Đang cập nhật");
        }

        // --- Set giá bán ---
        holder.tvSalePrice.setText(formatter.format(product.getPrice()));

        // Ẩn giá gốc và giảm giá (hoặc bạn có thể xử lý logic hiển thị ở đây nếu muốn)
        holder.tvOriginalPrice.setVisibility(View.INVISIBLE);
        holder.tvDiscount.setVisibility(View.INVISIBLE);

        // --- Set thống kê ---
        String luotxem = "Lượt xem: " + product.getView();
        String luotthich = "Lượt thích: " + product.getFavorite();
        holder.tvViewCount.setText(luotxem);
        holder.tvLikeCount.setText(luotthich);

        // 4. Bắt sự kiện click vào toàn bộ Item
        holder.itemView.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.onItemClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListProducts != null ? mListProducts.size() : 0;
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover, ivFavorite;
        // Thêm tvAuthor vào khai báo
        TextView tvTitle, tvAuthor, tvSalePrice, tvOriginalPrice, tvDiscount, tvViewCount, tvLikeCount;
        Button btnBuyNow;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivImage);
            ivFavorite = itemView.findViewById(R.id.ivDelete);

            tvTitle = itemView.findViewById(R.id.tvName);
            // Ánh xạ tvAuthor từ layout
            tvAuthor = itemView.findViewById(R.id.tvAuthor);

            tvSalePrice = itemView.findViewById(R.id.tvPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvDiscount = itemView.findViewById(R.id.tv_discount);
            tvViewCount = itemView.findViewById(R.id.tv_view_count);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count);
            btnBuyNow = itemView.findViewById(R.id.btnEdit);
        }
    }
}