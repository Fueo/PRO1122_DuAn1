package com.example.fa25_duan1.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DecimalFormat;
import java.util.List;
import com.example.fa25_duan1.model.Book;
import com.example.fa25_duan1.R;

public class BookGridAdapter extends RecyclerView.Adapter<BookGridAdapter.BookViewHolder> {

    private List<Book> mListBooks;

    private final DecimalFormat formatter = new DecimalFormat("#,### đ");

    public BookGridAdapter(List<Book> mListBooks) {
        this.mListBooks = mListBooks;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = mListBooks.get(position);
        if (book == null) return;



        holder.ivCover.setImageResource(book.getImageResId());
        holder.tvTitle.setText(book.getTitle());

        holder.tvSalePrice.setText(formatter.format(book.getSalePrice()));

        holder.tvOriginalPrice.setText(formatter.format(book.getOriginalPrice()));
        holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        holder.tvDiscount.setText(book.getDiscount());
        holder.tvViewCount.setText("Lượt xem: " + book.getViewCount());
        holder.tvLikeCount.setText("Lượt thích: " + book.getLikeCount());
    }

    @Override
    public int getItemCount() {
        return mListBooks != null ? mListBooks.size() : 0;
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover, ivFavorite;
        TextView tvTitle, tvSalePrice, tvOriginalPrice, tvDiscount, tvViewCount, tvLikeCount;
        Button btnBuyNow;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_book_cover);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
            tvTitle = itemView.findViewById(R.id.tv_book_title);
            tvSalePrice = itemView.findViewById(R.id.tv_sale_price);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvDiscount = itemView.findViewById(R.id.tv_discount);
            tvViewCount = itemView.findViewById(R.id.tv_view_count);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count);
            btnBuyNow = itemView.findViewById(R.id.btn_buy_now);
        }
    }
}