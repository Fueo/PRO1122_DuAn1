package com.example.fa25_duan1.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.Book;

import java.util.List;

public class RankingBookAdapter extends RecyclerView.Adapter<RankingBookAdapter.RankingViewHolder> {

    private List<Book> mListBooks;

    public RankingBookAdapter(List<Book> mListBooks) {
        this.mListBooks = mListBooks;
    }

    @NonNull
    @Override
    public RankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book_ranking, parent, false);
        return new RankingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankingViewHolder holder, int position) {
        Book book = mListBooks.get(position);

        holder.tvRankNumber.setText(String.format("%02d", position + 1));

        holder.ivCover.setImageResource(book.getImageResId());
        holder.tvTitle.setText(book.getTitle());
        holder.tvAuthor.setText(book.getAuthor());
        holder.tvLikeCount.setText(String.valueOf(book.getLikeCount()));
    }

    @Override
    public int getItemCount() {
        return mListBooks != null ? mListBooks.size() : 0;
    }

    public static class RankingViewHolder extends RecyclerView.ViewHolder {
        TextView tvRankNumber, tvTitle, tvAuthor, tvLikeCount;
        ImageView ivCover;

        public RankingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRankNumber = itemView.findViewById(R.id.tv_rank_number);
            tvTitle = itemView.findViewById(R.id.tv_book_title);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count);
            ivCover = itemView.findViewById(R.id.iv_book_cover);
        }
    }
}
