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

public class BookGridAdapter extends RecyclerView.Adapter<BookGridAdapter.BookViewHolder> {

    private List<Product> mListProducts;
    private List<String> favoriteIds = new ArrayList<>();
    private Context context;
    private final DecimalFormat formatter = new DecimalFormat("#,### đ");

    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(Product product);      // Xem chi tiết
        void onFavoriteClick(String productId); // Thả tim
        void onAddToCartClick(Product product); // Thêm vào giỏ (MỚI)
    }

    public BookGridAdapter(Context context, List<Product> mListProducts, OnItemClickListener listener) {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Product product = mListProducts.get(position);
        if (product == null) return;

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

        if (favoriteIds.contains(product.getId())) {
            holder.ivFavorite.setImageResource(R.drawable.ic_heart_filled_red);
        } else {
            holder.ivFavorite.setImageResource(R.drawable.ic_heart_outline_gray);
        }

        // Click Thêm vào giỏ
        holder.btnAddToCart.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onAddToCartClick(product);
            }
        });

        // Click Mua ngay
        holder.btnBuyNow.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(product);
            }
        });

        // Click Tim
        holder.ivFavorite.setOnClickListener(v -> {
            if (mListener != null) mListener.onFavoriteClick(product.getId());
        });

        // Click Item
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) mListener.onItemClick(product);
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
        MaterialButton btnAddToCart;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivImage);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
            tvTitle = itemView.findViewById(R.id.tvName);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvSalePrice = itemView.findViewById(R.id.tvPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvDiscount = itemView.findViewById(R.id.tv_discount);
            tvViewCount = itemView.findViewById(R.id.tv_view_count);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count);

            btnBuyNow = itemView.findViewById(R.id.btnEdit);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }

    // --- HÀM BẠN ĐANG THIẾU ĐÂY ---
    public void removeProductById(String productId) {
        if (mListProducts == null) return;

        for (int i = 0; i < mListProducts.size(); i++) {
            if (mListProducts.get(i).getId().equals(productId)) {
                mListProducts.remove(i);
                // Thông báo cho Adapter biết item tại vị trí i đã bị xóa
                notifyItemRemoved(i);
                // Cập nhật lại index của các item phía sau để tránh lỗi
                notifyItemRangeChanged(i, mListProducts.size());
                return;
            }
        }
    }
}