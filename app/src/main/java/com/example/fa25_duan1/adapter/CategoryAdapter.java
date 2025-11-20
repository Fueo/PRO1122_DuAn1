package com.example.fa25_duan1.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<Category> mListCategories;
    private ICategoryClickListener iCategoryClickListener;

    public interface ICategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(Context context, List<Category> mListCategories, ICategoryClickListener listener) {
        this.context = context;
        this.mListCategories = mListCategories;
        this.iCategoryClickListener = listener;
    }

    // Hàm này quan trọng: Dùng để cập nhật dữ liệu khi API trả về
    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<Category> newList) {
        this.mListCategories = newList;
        notifyDataSetChanged();
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
        if (category == null) return;

        holder.tvCategoryName.setText(category.getName());

        // --- XỬ LÝ MÀU SẮC (ACTIVE / INACTIVE) ---
        if (category.isSelected()) {
            // Đang chọn: Màu Xanh + In đậm + Hiện gạch chân
            holder.tvCategoryName.setTextColor(ContextCompat.getColor(context, R.color.blue)); // Đảm bảo màu blue có trong colors.xml
            holder.tvCategoryName.setTypeface(null, Typeface.BOLD);
            holder.viewIndicator.setVisibility(View.VISIBLE);
        } else {
            // Không chọn: Màu Xám + Chữ thường + Ẩn gạch chân
            holder.tvCategoryName.setTextColor(ContextCompat.getColor(context, R.color.gray_text)); // Đảm bảo màu gray_text có trong colors.xml
            holder.tvCategoryName.setTypeface(null, Typeface.NORMAL);
            holder.viewIndicator.setVisibility(View.INVISIBLE);
        }

        // --- XỬ LÝ CLICK ---
        holder.itemView.setOnClickListener(v -> {
            // 1. Reset tất cả về false (tắt sáng)
            for (Category cat : mListCategories) {
                cat.setSelected(false);
            }
// 2. Set item hiện tại thành true (bật sáng)
            category.setSelected(true);

            // 3. Cập nhật lại giao diện
            notifyDataSetChanged();

            // 4. Gửi sự kiện click ra ngoài Fragment để lọc sách
            if (iCategoryClickListener != null) {
                iCategoryClickListener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListCategories != null ? mListCategories.size() : 0;
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        View viewIndicator;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            viewIndicator = itemView.findViewById(R.id.view_indicator);
        }
    }
}