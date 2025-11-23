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

public class BookGridAdapter extends RecyclerView.Adapter<BookGridAdapter.BookViewHolder> {

    private List<Product> mListProducts;
    // 1. THÊM: List chứa các ID sản phẩm mà User đã thích
    private List<String> favoriteIds = new ArrayList<>();

    private Context context;
    private final DecimalFormat formatter = new DecimalFormat("#,### đ");

    // Khai báo Interface
    private OnItemClickListener mListener;

    // 2. CẬP NHẬT INTERFACE: Thêm hàm onFavoriteClick
    public interface OnItemClickListener {
        void onItemClick(Product product);      // Click vào cả item để xem chi tiết
        void onFavoriteClick(String productId); // Click vào trái tim để like/unlike
    }

    public BookGridAdapter(Context context, List<Product> mListProducts, OnItemClickListener listener) {
        this.context = context;
        this.mListProducts = mListProducts;
        this.mListener = listener;
    }

    // Hàm cập nhật danh sách sản phẩm
    public void setProducts(List<Product> list) {
        this.mListProducts = list;
        notifyDataSetChanged();
    }

    // 3. THÊM: Hàm cập nhật danh sách ID yêu thích từ ViewModel
    public void setFavoriteIds(List<String> ids) {
        this.favoriteIds = ids != null ? ids : new ArrayList<>();
        notifyDataSetChanged(); // Load lại giao diện để tô màu tim
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
                    .placeholder(R.drawable.book_cover_placeholder) // Đảm bảo bạn có ảnh này trong drawable
                    .error(R.drawable.book_cover_placeholder)
                    .into(holder.ivCover);
        } else {
            holder.ivCover.setImageResource(R.drawable.book_cover_placeholder);
        }

        // --- Set thông tin text ---
        holder.tvTitle.setText(product.getName());

        if (product.getAuthor() != null && product.getAuthor().getName() != null) {
            holder.tvAuthor.setText(product.getAuthor().getName());
        } else {
            holder.tvAuthor.setText("Đang cập nhật");
        }

        holder.tvSalePrice.setText(formatter.format(product.getPrice()));

        // Ẩn/Hiện giá gốc (Logic tùy chỉnh của bạn)
        holder.tvOriginalPrice.setVisibility(View.INVISIBLE);
        holder.tvDiscount.setVisibility(View.INVISIBLE);

        holder.tvViewCount.setText("Lượt xem: " + product.getView());
        holder.tvLikeCount.setText("Lượt thích: " + product.getFavorite());

        // --- 4. LOGIC HIỂN THỊ TRÁI TIM (QUAN TRỌNG) ---
        // Kiểm tra xem ID sách này có nằm trong danh sách User đã thích không
        if (favoriteIds.contains(product.getId())) {
            // Đã thích -> Tim đỏ
            // (Đảm bảo bạn đã có icon ic_heart_red hoặc tên tương tự)
            holder.ivFavorite.setImageResource(R.drawable.ic_heart_filled_red);
        } else {
            // Chưa thích -> Tim rỗng (xám)
            holder.ivFavorite.setImageResource(R.drawable.ic_heart_outline_gray);
        }

        // --- 5. SỰ KIỆN CLICK ---

        // Click vào trái tim
        holder.ivFavorite.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onFavoriteClick(product.getId());
            }
        });

        // Click vào toàn bộ Item (để xem chi tiết)
        holder.itemView.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.onItemClick(product);
            }
        });

        // Click nút Mua ngay (Tùy logic, ở đây mình cho vào xem chi tiết luôn)
        holder.btnBuyNow.setOnClickListener(v -> {
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
        TextView tvTitle, tvAuthor, tvSalePrice, tvOriginalPrice, tvDiscount, tvViewCount, tvLikeCount;
        Button btnBuyNow;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivImage);

            // 6. SỬA LỖI: Ánh xạ đúng ID trong XML (iv_favorite) thay vì ivDelete
            ivFavorite = itemView.findViewById(R.id.iv_favorite);

            tvTitle = itemView.findViewById(R.id.tvName);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvSalePrice = itemView.findViewById(R.id.tvPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvDiscount = itemView.findViewById(R.id.tv_discount);
            tvViewCount = itemView.findViewById(R.id.tv_view_count);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count);
            btnBuyNow = itemView.findViewById(R.id.btnEdit);
        }
    }

    public void removeProductById(String productId) {
        if (mListProducts == null) return;

        for (int i = 0; i < mListProducts.size(); i++) {
            if (mListProducts.get(i).getId().equals(productId)) {
                mListProducts.remove(i);
                notifyItemRemoved(i);
                notifyItemRangeChanged(i, mListProducts.size());
                return;
            }
        }
    }
}