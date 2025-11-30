package com.example.fa25_duan1.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.Product;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductSelectionAdapter extends RecyclerView.Adapter<ProductSelectionAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    // Sử dụng Set để lưu ID các sản phẩm được chọn (tránh trùng lặp và tra cứu nhanh)
    private final Set<String> selectedIds = new HashSet<>();
    private OnSelectionChangeListener listener;

    // Interface để báo về Fragment số lượng đang chọn
    public interface OnSelectionChangeListener {
        void onSelectionChanged(int count, Set<String> selectedIds);
    }

    public ProductSelectionAdapter(Context context, OnSelectionChangeListener listener) {
        this.context = context;
        this.listener = listener;
        this.productList = new ArrayList<>();
    }

    public void setList(List<Product> list) {
        this.productList = list;
        notifyDataSetChanged();
    }

    // --- HÀM BẠN CẦN BỔ SUNG Ở ĐÂY ---
    public void setSelectedIds(List<String> ids) {
        selectedIds.clear();
        if (ids != null) {
            selectedIds.addAll(ids);
        }
        notifyDataSetChanged();

        // Trigger listener để update UI bên ngoài (VD: nút xác nhận)
        if (listener != null) {
            listener.onSelectionChanged(selectedIds.size(), selectedIds);
        }
    }
    // ---------------------------------

    // Hàm lấy danh sách ID đã chọn để gửi đi
    public List<String> getSelectedProductIds() {
        return new ArrayList<>(selectedIds);
    }

    // Hàm xử lý "Chọn tất cả" từ Fragment cha
    public void toggleSelectAll(boolean isSelected) {
        selectedIds.clear();
        if (isSelected) {
            for (Product p : productList) {
                if (p.getId() != null) {
                    selectedIds.add(p.getId());
                }
            }
        }
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged(selectedIds.size(), selectedIds);
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_select, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        if (product == null) return;

        // 1. Set thông tin cơ bản
        holder.tvName.setText(product.getName());

        // Format tiền tệ
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        String priceStr = formatter.format(product.getPrice()) + " đ";
        holder.tvPrice.setText(priceStr);

        // Load ảnh bằng Glide
        Glide.with(context)
                .load(product.getImage())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.imgProduct);

        // 2. Xử lý trạng thái Checkbox
        holder.cbSelect.setOnCheckedChangeListener(null);

        boolean isSelected = selectedIds.contains(product.getId());
        holder.cbSelect.setChecked(isSelected);

        // 3. Sự kiện Click Checkbox
        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedIds.add(product.getId());
            } else {
                selectedIds.remove(product.getId());
            }
            if (listener != null) {
                listener.onSelectionChanged(selectedIds.size(), selectedIds);
            }
        });

        // 4. Sự kiện Click vào cả dòng item cũng toggle checkbox
        holder.itemView.setOnClickListener(v -> {
            holder.cbSelect.toggle();
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice;
        CheckBox cbSelect;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            cbSelect = itemView.findViewById(R.id.cbSelect);
        }
    }
}