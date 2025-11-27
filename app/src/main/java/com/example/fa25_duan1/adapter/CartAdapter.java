package com.example.fa25_duan1.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartItems;
    private OnCartItemClickListener listener;

    public CartAdapter(Context context, List<CartItem> cartItems, OnCartItemClickListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart_product, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        // Sử dụng các Getter thông minh đã viết trong Model
        holder.tvTitle.setText(item.getTitle());
        holder.tvPrice.setText(String.format("%,.0f VNĐ", item.getPrice() * item.getQuantity()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.book_cover_placeholder)
                .into(holder.ivBookCover);

        // Sự kiện click
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(item, holder.getBindingAdapterPosition());
        });

        holder.btnIncrease.setOnClickListener(v -> {
            if (listener != null) listener.onIncreaseClick(item, holder.getBindingAdapterPosition());
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (listener != null) listener.onDecreaseClick(item, holder.getBindingAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBookCover; // Đã sửa thành ImageView thường
        TextView tvTitle, tvPrice, tvQuantity;
        ImageButton btnDelete, btnIncrease, btnDecrease;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.iv_book_cover);
            tvTitle = itemView.findViewById(R.id.tv_book_title);
            tvPrice = itemView.findViewById(R.id.tv_book_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            btnDelete = itemView.findViewById(R.id.btn_delete_item);
            btnIncrease = itemView.findViewById(R.id.btn_increase_quantity);
            btnDecrease = itemView.findViewById(R.id.btn_decrease_quantity);
        }
    }

    public interface OnCartItemClickListener {
        void onIncreaseClick(CartItem item, int position);
        void onDecreaseClick(CartItem item, int position);
        void onDeleteClick(CartItem item, int position);
    }
}