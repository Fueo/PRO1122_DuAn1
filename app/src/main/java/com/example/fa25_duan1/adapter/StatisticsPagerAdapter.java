package com.example.fa25_duan1.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;

public class StatisticsPagerAdapter extends RecyclerView.Adapter<StatisticsPagerAdapter.ViewHolder> {

    // Interface để Fragment cha gọi hàm setup biểu đồ
    public interface OnBindChartListener {
        void onBindRevenue(View view);
        void onBindOrders(View view);
        void onBindProducts(View view);
    }

    private final OnBindChartListener listener;

    public StatisticsPagerAdapter(OnBindChartListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        int layoutId = R.layout.item_tab_revenue; // Mặc định
        if (viewType == 1) layoutId = R.layout.item_tab_orders;
        else if (viewType == 2) layoutId = R.layout.item_tab_products;
        
        View view = inflater.inflate(layoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == 0) listener.onBindRevenue(holder.itemView);
        else if (position == 1) listener.onBindOrders(holder.itemView);
        else if (position == 2) listener.onBindProducts(holder.itemView);
    }

    @Override
    public int getItemCount() { return 3; }

    @Override
    public int getItemViewType(int position) { return position; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) { super(itemView); }
    }
}