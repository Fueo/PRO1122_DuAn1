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

    public CategoryAdapter(List<Category> mListCategories) {
        this.mListCategories = mListCategories;
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

        if (category.isSelected()) {
            holder.viewIndicator.setVisibility(View.VISIBLE);
            holder.tvName.setTextColor(Color.parseColor("#2196F3"));
        } else {
            holder.viewIndicator.setVisibility(View.INVISIBLE);
            holder.tvName.setTextColor(Color.parseColor("#757575"));
        }


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