package com.example.fa25_duan1.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView; // 1. Thêm import ImageView
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
        holder.tvTitle.setText(item.getTitle());
        holder.tvPrice.setText(String.format("%,.0f VNĐ", item.getPrice() * item.getQuantity()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        // Load hình với Glide
        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.book_cover_placeholder)
                .into(holder.ivBookCover);

        // Xử lý nút Xóa
        holder.btnDelete.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION && listener != null) {
                listener.onDeleteClick(cartItems.get(currentPos), currentPos);
            }
        });

        // Xử lý nút Tăng
        holder.btnIncrease.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION && listener != null) {
                listener.onIncreaseClick(cartItems.get(currentPos), currentPos);
            }
        });

        // Xử lý nút Giảm
        holder.btnDecrease.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION && listener != null) {
                listener.onDecreaseClick(cartItems.get(currentPos), currentPos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        // 2. SỬA TẠI ĐÂY: Đổi ShapeableImageView thành ImageView
        ImageView ivBookCover;

        TextView tvTitle, tvPrice, tvQuantity;
        ImageButton btnDelete, btnIncrease, btnDecrease;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            // 3. Ánh xạ bình thường, không ép kiểu sai nữa
            ivBookCover = itemView.findViewById(R.id.iv_book_cover);

            tvTitle = itemView.findViewById(R.id.tv_book_title);
            tvPrice = itemView.findViewById(R.id.tv_book_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            btnDelete = itemView.findViewById(R.id.btn_delete_item);
            btnIncrease = itemView.findViewById(R.id.btn_increase_quantity);
            btnDecrease = itemView.findViewById(R.id.btn_decrease_quantity);
        }
    }

    // Interface xử lý sự kiện
    public interface OnCartItemClickListener {
        void onIncreaseClick(CartItem item, int position);
        void onDecreaseClick(CartItem item, int position);
        void onDeleteClick(CartItem item, int position);
    }
}