package com.example.fa25_duan1.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.Category;

import java.util.List;

public class CategoryProductAdapter extends RecyclerView.Adapter<CategoryProductAdapter.CategoryViewHolder> {

    private Context context;
    private List<Category> categoryList;
    private OnCategoryClickListener listener;

    // Interface để bắn sự kiện click ra bên ngoài (Fragment/Activity)
    public interface OnCategoryClickListener {
        void onCategoryClick(Category category, int position);
    }
    public CategoryProductAdapter(Context context, List<Category> categoryList, OnCategoryClickListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
    }

    // Hàm cập nhật dữ liệu mới
    public void setCategoryList(List<Category> list) {
        this.categoryList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Lưu ý: Đặt tên file layout của bạn là item_category.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_product, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        if (category == null) return;

        holder.tvCategoryName.setText(category.getName());
        holder.tvCategoryName.setSelected(category.isSelected());

        holder.itemView.setOnClickListener(v -> {
            // Gửi sự kiện click ra ngoài Fragment để xử lý logic (Recommended pattern)
            if (listener != null) {
                listener.onCategoryClick(category, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (categoryList != null) {
            return categoryList.size();
        }
        return 0;
    }

    // Hàm logic để xử lý việc chỉ chọn 1 danh mục duy nhất
    private void updateSelection(int selectedPosition) {
        for (int i = 0; i < categoryList.size(); i++) {
            // Set true cho vị trí được click, false cho các vị trí còn lại
            categoryList.get(i).setSelected(i == selectedPosition);
        }
        // Cập nhật lại giao diện (Load lại RecyclerView để đổi màu)
        notifyDataSetChanged();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ ID từ file XML bạn cung cấp
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}