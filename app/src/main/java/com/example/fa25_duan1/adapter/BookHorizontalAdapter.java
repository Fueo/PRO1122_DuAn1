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

public class BookHorizontalAdapter extends RecyclerView.Adapter<BookHorizontalAdapter.BookViewHolder> {

    private Context context;
    private List<Product> mListProducts;
    private final DecimalFormat formatter = new DecimalFormat("#,### đ");

    // Interface cho sự kiện click
    private OnItemClickListener mListener;
    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public BookHorizontalAdapter(Context context, List<Product> mListProducts, OnItemClickListener listener) {
        this.context = context;
        this.mListProducts = mListProducts;
        this.mListener = listener;
    }

    public void setProducts(List<Product> list) {
        this.mListProducts = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book_horizontal, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Product product = mListProducts.get(position);
        if (product == null) return;

        // 1. Load ảnh bằng Glide
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            Glide.with(context)
                    .load(product.getImage())
                    .placeholder(R.drawable.book_cover_placeholder)
                    .error(R.drawable.book_cover_placeholder)
                    .into(holder.ivCover);
        } else {
            holder.ivCover.setImageResource(R.drawable.book_cover_placeholder);
        }

        // 2. Set tên sách
        holder.tvTitle.setText(product.getName());

        // 3. MỚI: Set tên tác giả
        if (product.getAuthor() != null && product.getAuthor().getName() != null) {
            holder.tvAuthor.setText(product.getAuthor().getName());
        } else {
            holder.tvAuthor.setText("Đang cập nhật");
        }

        // 4. Set giá
        holder.tvSalePrice.setText(formatter.format(product.getPrice()));

        // 5. Ẩn các trường không dùng
        holder.tvOriginalPrice.setVisibility(View.INVISIBLE);
        holder.tvDiscount.setVisibility(View.INVISIBLE);

        // 6. Set view/like
        holder.tvViewCount.setText("Lượt xem: " + product.getView());
        holder.tvLikeCount.setText("Lượt thích: " + product.getFavorite());

        // 7. Sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if(mListener != null) mListener.onItemClick(product);
        });

        holder.btnBuyNow.setOnClickListener(v -> {
            if(mListener != null) mListener.onItemClick(product);
        });
    }

    @Override
    public int getItemCount() {
        return mListProducts != null ? mListProducts.size() : 0;
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover, ivFavorite;
        // Thêm tvAuthor vào ViewHolder
        TextView tvTitle, tvAuthor, tvSalePrice, tvOriginalPrice, tvDiscount, tvViewCount, tvLikeCount;
        Button btnBuyNow;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_book_cover);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);

            tvTitle = itemView.findViewById(R.id.tv_book_title);
            // Ánh xạ TextView Tác giả từ layout XML mới sửa
            tvAuthor = itemView.findViewById(R.id.tv_book_author);

            tvSalePrice = itemView.findViewById(R.id.tv_sale_price);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvDiscount = itemView.findViewById(R.id.tv_discount);
            tvViewCount = itemView.findViewById(R.id.tv_view_count);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count);
            btnBuyNow = itemView.findViewById(R.id.btn_buy_now);
        }
    }
}