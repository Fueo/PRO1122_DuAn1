package com.example.fa25_duan1.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R; // Đảm bảo R.layout.item_categoryfilter tồn tại
import com.example.fa25_duan1.model.Category;

import java.util.List;

public class ProductFilterAdapter extends RecyclerView.Adapter<ProductFilterAdapter.CategoryFilterViewHolder> {

    private Context context;
    private List<Category> listCategory;
    private OnCategorySelectionListener listener;

    /**
     * Interface để truyền sự kiện chọn/bỏ chọn về Fragment/Activity.
     */
    public interface OnCategorySelectionListener {
        void onCategorySelected(Category category, boolean isChecked);
    }

    public ProductFilterAdapter(Context context, List<Category> listCategory, OnCategorySelectionListener listener) {
        this.context = context;
        this.listCategory = listCategory;
        this.listener = listener;
    }

    public void setData(List<Category> list) {
        this.listCategory = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryFilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Giả định layout item là R.layout.item_categoryfilter (hoặc R.layout.item_productfilter)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_productfilter, parent, false);
        return new CategoryFilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryFilterViewHolder holder, int position) {
        Category category = listCategory.get(position);
        if (category == null) return;

        // 1. Hiển thị dữ liệu
        holder.tvCategory.setText(category.getName());

        // 2. Thiết lập trạng thái CheckBox
        holder.cbCategory.setChecked(category.isSelected());

        // 3. Xử lý sự kiện khi người dùng tương tác
        // Sử dụng toàn bộ item view (LinearLayout) để bắt sự kiện click
        holder.itemView.setOnClickListener(v -> {
            boolean newState = !holder.cbCategory.isChecked();
            holder.cbCategory.setChecked(newState);
            category.setSelected(newState); // Cập nhật trạng thái trong model

            // Truyền sự kiện về listener
            if (listener != null) {
                listener.onCategorySelected(category, newState);
            }
        });

        // Xử lý sự kiện khi click trực tiếp vào CheckBox (để đảm bảo đồng bộ)
        holder.cbCategory.setOnCheckedChangeListener((buttonView, isChecked) -> {
            category.setSelected(isChecked); // Cập nhật trạng thái trong model
            if (listener != null) {
                listener.onCategorySelected(category, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listCategory != null ? listCategory.size() : 0;
    }

    /**
     * ViewHolder chứa các View con của mỗi item.
     */
    public class CategoryFilterViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbCategory;
        TextView tvCategory;
        // LinearLayout itemLayout; // Không cần nếu sử dụng itemView

        public CategoryFilterViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các View con theo ID trong layout XML bạn cung cấp
            cbCategory = itemView.findViewById(R.id.cbCategory);
            tvCategory = itemView.findViewById(R.id.tvCategory);
        }
    }
}