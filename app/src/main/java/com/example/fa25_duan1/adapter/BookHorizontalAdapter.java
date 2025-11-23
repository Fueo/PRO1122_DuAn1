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
import java.util.ArrayList;
import java.util.List;

public class BookHorizontalAdapter extends RecyclerView.Adapter<BookHorizontalAdapter.BookViewHolder> {

    private Context context;
    private List<Product> mListProducts;
    // 1. THÊM: List chứa các ID sản phẩm user đã thích
    private List<String> favoriteIds = new ArrayList<>();

    private final DecimalFormat formatter = new DecimalFormat("#,### đ");

    // Interface cho sự kiện click
    private OnItemClickListener mListener;

    // 2. CẬP NHẬT INTERFACE: Thêm onFavoriteClick
    public interface OnItemClickListener {
        void onItemClick(Product product);      // Xem chi tiết
        void onBuyClick(Product product);       // Mua ngay
        void onFavoriteClick(String productId); // Thả tim
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

    // 3. THÊM: Hàm cập nhật danh sách ID yêu thích
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

        // --- Set thông tin ---
        holder.tvTitle.setText(product.getName());

        if (product.getAuthor() != null && product.getAuthor().getName() != null) {
            holder.tvAuthor.setText(product.getAuthor().getName());
        } else {
            holder.tvAuthor.setText("Đang cập nhật");
        }

        holder.tvSalePrice.setText(formatter.format(product.getPrice()));

        // Ẩn các view chưa cần thiết
        holder.tvOriginalPrice.setVisibility(View.INVISIBLE);
        holder.tvDiscount.setVisibility(View.INVISIBLE);

        holder.tvViewCount.setText("Lượt xem: " + product.getView());
        holder.tvLikeCount.setText("Lượt thích: " + product.getFavorite());

        // --- 4. XỬ LÝ FAVORITE (MỚI) ---

        // Kiểm tra ID để hiển thị icon
        if (favoriteIds.contains(product.getId())) {
            holder.ivFavorite.setImageResource(R.drawable.ic_heart_filled_red);
        } else {
            holder.ivFavorite.setImageResource(R.drawable.ic_heart_outline_gray);
        }

        // Sự kiện click tim
        holder.ivFavorite.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onFavoriteClick(product.getId());
            }
        });

        // --- Sự kiện click khác ---

        // Click vào khung -> Xem chi tiết
        holder.itemView.setOnClickListener(v -> {
            if(mListener != null) mListener.onItemClick(product);
        });

        // Click vào nút Mua -> Gọi hàm onBuyClick
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

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            // Đảm bảo ID trong layout item_book_horizontal khớp với các ID này
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
        }
    }
}