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
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class BookHorizontalAdapter extends RecyclerView.Adapter<BookHorizontalAdapter.BookViewHolder> {

    private Context context;
    private List<Product> mListProducts;
    private List<String> favoriteIds = new ArrayList<>();
    private final DecimalFormat formatter = new DecimalFormat("#,### đ");

    private OnItemClickListener mListener;

    // 1. CẬP NHẬT INTERFACE: Thêm onAddToCartClick
    public interface OnItemClickListener {
        void onItemClick(Product product);      // Xem chi tiết
        void onBuyClick(Product product);       // Mua ngay
        void onFavoriteClick(String productId); // Thả tim
        void onAddToCartClick(Product product); // Thêm vào giỏ (MỚI)
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

    public void setFavoriteIds(List<String> ids) {
        this.favoriteIds = ids != null ? ids : new ArrayList<>();
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

        // Load ảnh
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            Glide.with(context)
                    .load(product.getImage())
                    .placeholder(R.drawable.book_cover_placeholder)
                    .error(R.drawable.book_cover_placeholder)
                    .into(holder.ivCover);
        } else {
            holder.ivCover.setImageResource(R.drawable.book_cover_placeholder);
        }

        holder.tvTitle.setText(product.getName());
        if (product.getAuthor() != null) {
            holder.tvAuthor.setText(product.getAuthor().getName());
        } else {
            holder.tvAuthor.setText("Đang cập nhật");
        }
        holder.tvSalePrice.setText(formatter.format(product.getPrice()));

        holder.tvViewCount.setText("Lượt xem: " + product.getView());
        holder.tvLikeCount.setText("Lượt thích: " + product.getFavorite());

        // Xử lý Tim
        if (favoriteIds.contains(product.getId())) {
            holder.ivFavorite.setImageResource(R.drawable.ic_heart_filled_red);
        } else {
            holder.ivFavorite.setImageResource(R.drawable.ic_heart_outline_gray);
        }

        // --- CÁC SỰ KIỆN CLICK ---

        // 1. Thêm vào giỏ (Nút mới)
        holder.btnAddToCart.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onAddToCartClick(product);
            }
        });

        holder.ivFavorite.setOnClickListener(v -> {
            if (mListener != null) mListener.onFavoriteClick(product.getId());
        });

        holder.itemView.setOnClickListener(v -> {
            if(mListener != null) mListener.onItemClick(product);
        });

        holder.btnBuyNow.setOnClickListener(v -> {
            if(mListener != null) mListener.onBuyClick(product);
        });
    }

    @Override
    public int getItemCount() {
        return mListProducts != null ? mListProducts.size() : 0;
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover, ivFavorite;
        TextView tvTitle, tvAuthor, tvSalePrice, tvOriginalPrice, tvDiscount, tvViewCount, tvLikeCount;
        Button btnBuyNow;
        MaterialButton btnAddToCart; // Khai báo nút mới

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_book_cover);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
            tvTitle = itemView.findViewById(R.id.tv_book_title);
            tvAuthor = itemView.findViewById(R.id.tv_book_author);
            tvSalePrice = itemView.findViewById(R.id.tv_sale_price);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvDiscount = itemView.findViewById(R.id.tv_discount);
            tvViewCount = itemView.findViewById(R.id.tv_view_count);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count);

            btnBuyNow = itemView.findViewById(R.id.btn_buy_now);

            // 2. Ánh xạ nút Thêm vào giỏ (ID trong XML item_book_horizontal.xml)
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
        }
    }
}