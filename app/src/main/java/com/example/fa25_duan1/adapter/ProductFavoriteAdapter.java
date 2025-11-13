package com.example.fa25_duan1.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.ProductFavorite;

import java.util.List;

public class ProductFavoriteAdapter extends RecyclerView.Adapter<ProductFavoriteAdapter.ProductFavoriteViewHolder> {

    private Context context;
    private List<ProductFavorite> productList;

    public ProductFavoriteAdapter(Context context, List<ProductFavorite> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductFavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_favorite, parent, false); // ĐÃ ĐỔI TÊN
        return new ProductFavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductFavoriteViewHolder holder, int position) {
        ProductFavorite product = productList.get(position);

        holder.tvBookTitle.setText(product.getTitle());
        holder.tvBookAuthor.setText(product.getAuthor());
        holder.tvCurrentPrice.setText(product.getCurrentPrice());
        holder.tvOriginalPrice.setText(product.getOriginalPrice());
        holder.tvDiscountPercent.setText(product.getDiscountPercent());
        holder.ivBookCover.setImageResource(product.getImageResId());

        holder.tvOriginalPrice.setPaintFlags(
                holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
        );

        holder.ivFavorite.setOnClickListener(v -> {
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductFavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBookCover, ivFavorite;
        TextView tvDiscountBadge, tvBookTitle, tvBookAuthor, tvCurrentPrice, tvOriginalPrice, tvDiscountPercent;

        public ProductFavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.iv_book_cover);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
            tvDiscountBadge = itemView.findViewById(R.id.tv_discount_badge);
            tvBookTitle = itemView.findViewById(R.id.tv_book_title);
            tvBookAuthor = itemView.findViewById(R.id.tv_book_author);
            tvCurrentPrice = itemView.findViewById(R.id.tv_current_price);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvDiscountPercent = itemView.findViewById(R.id.tv_discount_percent);
        }
    }
}