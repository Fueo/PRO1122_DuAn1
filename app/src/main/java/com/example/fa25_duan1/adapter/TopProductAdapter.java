package com.example.fa25_duan1.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.statistic.StatsProductOverview;

import java.util.ArrayList;
import java.util.List;

public class TopProductAdapter extends RecyclerView.Adapter<TopProductAdapter.ViewHolder> {

    private Context context;
    private List<StatsProductOverview.TopProduct> list;
    private int maxSoldValue = 1; // Dùng để tính tỷ lệ % cho ProgressBar

    public TopProductAdapter(Context context) {
        this.context = context;
        this.list = new ArrayList<>();
    }

    public void setList(List<StatsProductOverview.TopProduct> newList) {
        this.list = newList;
        
        // Tìm giá trị bán cao nhất để làm mốc 100% cho ProgressBar
        maxSoldValue = 0;
        if (list != null && !list.isEmpty()) {
            for (StatsProductOverview.TopProduct p : list) {
                if (p.getSold() > maxSoldValue) {
                    maxSoldValue = p.getSold();
                }
            }
        }
        if (maxSoldValue == 0) maxSoldValue = 1; // Tránh chia cho 0

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_top_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StatsProductOverview.TopProduct product = list.get(position);

        // 1. Số thứ tự (Rank)
        holder.tvRank.setText(String.valueOf(position + 1));
        
        // Tô màu cho Top 1, 2, 3 để nổi bật
        if (position == 0) holder.tvRank.setTextColor(Color.parseColor("#FFD700")); // Vàng
        else if (position == 1) holder.tvRank.setTextColor(Color.parseColor("#C0C0C0")); // Bạc
        else if (position == 2) holder.tvRank.setTextColor(Color.parseColor("#CD7F32")); // Đồng
        else holder.tvRank.setTextColor(Color.DKGRAY);

        // 2. Tên và Số lượng
        holder.tvName.setText(product.getName());
        holder.tvSales.setText(String.valueOf(product.getSold()));

        // 3. Xử lý ProgressBar (Giả lập biểu đồ)
        // Tính tỷ lệ: (Số bán / Max bán) * 100
        int progress = (int) (((float) product.getSold() / maxSoldValue) * 100);
        holder.pbSales.setProgress(progress);
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvSales;
        ProgressBar pbSales;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvSales = itemView.findViewById(R.id.tvSalesCount);
            pbSales = itemView.findViewById(R.id.pbSales);
        }
    }
}