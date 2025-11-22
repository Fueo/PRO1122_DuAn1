package com.example.fa25_duan1.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> mListCategories;
    // 1. Khai báo Interface để gửi sự kiện click ra ngoài
    private OnCategoryClickListener mListener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    // 2. Cập nhật Constructor để nhận Listener
    public CategoryAdapter(List<Category> mListCategories, OnCategoryClickListener listener) {
        this.mListCategories = mListCategories;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = mListCategories.get(position);
        holder.tvName.setText(category.getName());

        // Xử lý màu sắc dựa trên trạng thái selected
        if (category.isSelected()) {
            holder.viewIndicator.setVisibility(View.VISIBLE);
            holder.tvName.setTextColor(Color.parseColor("#2196F3")); // Màu xanh
        } else {
            holder.viewIndicator.setVisibility(View.INVISIBLE);
            holder.tvName.setTextColor(Color.parseColor("#757575")); // Màu xám
        }

        // 3. Bắt sự kiện Click vào item
        holder.itemView.setOnClickListener(v -> {
            // Đặt tất cả các item khác thành false (bỏ chọn)
            for (Category item : mListCategories) {
                item.setSelected(false);
            }
            // Đặt item hiện tại thành true (được chọn)
            category.setSelected(true);

            // Cập nhật lại giao diện
            notifyDataSetChanged();

            // Gửi category được chọn về Fragment để xử lý
            if (mListener != null) {
                mListener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListCategories != null ? mListCategories.size() : 0;
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        View viewIndicator;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_category_name);
            viewIndicator = itemView.findViewById(R.id.view_indicator);
        }
    }
}