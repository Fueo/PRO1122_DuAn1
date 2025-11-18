package com.example.fa25_duan1.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.Banner;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private List<Banner> mListBanner;

    public BannerAdapter(List<Banner> mListBanner) {
        this.mListBanner = mListBanner;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Banner banner = mListBanner.get(position);
        if (banner == null) return;

        holder.ivBanner.setImageResource(banner.getImageResId());
        holder.tvTitle.setText(banner.getTitle());
        holder.tvAuthor.setText(banner.getAuthor());
    }

    @Override
    public int getItemCount() {
        return mListBanner != null ? mListBanner.size() : 0;
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivBanner;
        private TextView tvTitle, tvAuthor;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBanner = itemView.findViewById(R.id.iv_banner_image);
            tvTitle = itemView.findViewById(R.id.tv_banner_title);
            tvAuthor = itemView.findViewById(R.id.tv_banner_author);
        }
    }
}