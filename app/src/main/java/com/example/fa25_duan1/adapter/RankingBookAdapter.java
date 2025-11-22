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
import com.example.fa25_duan1.model.Product;

import java.text.DecimalFormat;
import java.util.List;

public class RankingBookAdapter extends RecyclerView.Adapter<RankingBookAdapter.RankingViewHolder> {

    private Context context;
    private List<Product> mListProducts;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    // 1. Thêm Interface listener
    private OnItemClickListener mListener;
    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    // 2. Cập nhật Constructor
    public RankingBookAdapter(Context context, List<Product> mListProducts, OnItemClickListener listener) {
        this.context = context;
        this.mListProducts = mListProducts;
        this.mListener = listener; // Gán listener
    }

    // Constructor cũ (nếu muốn giữ lại để tránh lỗi ở chỗ khác chưa sửa)
    public RankingBookAdapter(Context context, List<Product> mListProducts) {
        this.context = context;
        this.mListProducts = mListProducts;
    }

    public void setProducts(List<Product> list) {
        this.mListProducts = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book_ranking, parent, false);
        return new RankingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankingViewHolder holder, int position) {
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

        holder.tvRank.setText(String.format("%02d", position + 1));
        holder.tvTitle.setText(product.getName());

        if (product.getAuthor() != null && product.getAuthor().getName() != null) {
            holder.tvAuthor.setText(product.getAuthor().getName());
        } else {
            holder.tvAuthor.setText("Đang cập nhật");
        }

        holder.tvLikeCount.setText(String.valueOf(product.getFavorite()));

        // 3. Bắt sự kiện Click
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListProducts != null ? mListProducts.size() : 0;
    }

    public static class RankingViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvRank, tvTitle, tvAuthor, tvLikeCount;

        public RankingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tv_rank_number);
            ivCover = itemView.findViewById(R.id.iv_book_cover);
            tvTitle = itemView.findViewById(R.id.tv_book_title);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count);
        }
    }
}