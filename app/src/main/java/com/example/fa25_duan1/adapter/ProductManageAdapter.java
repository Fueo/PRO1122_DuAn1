package com.example.fa25_duan1.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.Product;
// Thư viện Glide/Picasso (giả định) để load ảnh
// import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductManageAdapter extends RecyclerView.Adapter<ProductManageAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> listProduct;
    private OnProductActionListener listener;

    /**
     * Interface để truyền sự kiện Edit và Delete về Fragment/Activity.
     */
    public interface OnProductActionListener {
        void onEditClick(Product product);
        void onDeleteClick(Product product);
    }

    public ProductManageAdapter(Context context, List<Product> listProduct, OnProductActionListener listener) {
        this.context = context;
        this.listProduct = listProduct;
        this.listener = listener;
    }

    public void setData(List<Product> list) {
        this.listProduct = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Giả định R.layout.item_product_manage là tên layout CardView bạn cung cấp
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_productmanage, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = listProduct.get(position);
        if (product == null) return;

        if (product.getImage() != null && !product.getImage().isEmpty()) {
            Glide.with(context)
                    .load(product.getImage())
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.book_cover_placeholder)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.book_cover_placeholder);
        }
        holder.tvName.setText(product.getName());

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedPrice = format.format(product.getPrice()).replace("₫", " đ");
        holder.tvPrice.setText(formattedPrice);

        holder.tvQuantity.setText(String.valueOf(product.getQuantity()));

        if (!product.isStatus()) {
            holder.tvName.setTextColor(context.getResources().getColor(R.color.gray_text));
        } else {
            holder.tvName.setTextColor(context.getResources().getColor(R.color.black));
        }

        if (listener != null) {
            holder.btnEdit.setOnClickListener(v -> listener.onEditClick(product));
            holder.ivDelete.setOnClickListener(v -> listener.onDeleteClick(product));
        }
    }

    @Override
    public int getItemCount() {
        return listProduct != null ? listProduct.size() : 0;
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        ImageView ivDelete;
        TextView tvName;
        TextView tvPrice;
        Button btnEdit;
        TextView tvQuantity;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
        }
    }
}