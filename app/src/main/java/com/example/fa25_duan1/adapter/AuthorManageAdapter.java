package com.example.fa25_duan1.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.Author;
import com.example.fa25_duan1.viewmodel.ProductViewModel;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AuthorManageAdapter extends RecyclerView.Adapter<AuthorManageAdapter.ViewHolder> {

    private List<Author> authorList;
    private final Context context;
    private final OnItemClickListener listener;

    // THÊM: ViewModel và LifecycleOwner
    private final ProductViewModel productViewModel;
    private final LifecycleOwner lifecycleOwner;

    public interface OnItemClickListener {
        void onEditClick(Author author);
        void onDeleteClick(Author author);
        void onItemClick(Author author);
    }

    // CẬP NHẬT: Constructor nhận thêm viewModel và lifecycleOwner
    public AuthorManageAdapter(Context context,
                               List<Author> authorList,
                               OnItemClickListener listener,
                               ProductViewModel productViewModel,
                               LifecycleOwner lifecycleOwner) {
        this.context = context;
        this.authorList = authorList;
        this.listener = listener;
        this.productViewModel = productViewModel;
        this.lifecycleOwner = lifecycleOwner;
    }

    public void setData(List<Author> authors) {
        this.authorList = authors;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivEdit, ivDelete;
        TextView tvName, tvInfo;
        CircleImageView cvAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            tvName = itemView.findViewById(R.id.tvName);
            tvInfo = itemView.findViewById(R.id.tvInfo);
            cvAvatar = itemView.findViewById(R.id.cvAvatar);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_author, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Author author = authorList.get(position);
        if (author == null) return;

        // 1. Load Avatar
        if (author.getAvatar() != null && !author.getAvatar().isEmpty()) {
            Glide.with(context)
                    .load(author.getAvatar())
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
                    .into(holder.cvAvatar);
        } else {
            holder.cvAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
        }

        // 2. Hiển thị Tên
        holder.tvName.setText(author.getName());

        // 3. XỬ LÝ SỐ LƯỢNG SÁCH (Gọi ViewModel)
        // Đặt text mặc định trước khi load xong
        holder.tvInfo.setText("Đang tải dữ liệu...");

        if (author.getAuthorID() != null) {
            // Gọi hàm lấy danh sách sách theo AuthorID đã viết ở bước trước
            productViewModel.getProductsByAuthor(author.getAuthorID()).observe(lifecycleOwner, products -> {
                // Kiểm tra ViewHolder còn khớp với position không để tránh lỗi hiển thị khi scroll nhanh
                if (holder.getAdapterPosition() == position) {
                    int count = (products != null) ? products.size() : 0;
                    holder.tvInfo.setText("Tác giả của " + count + " đầu sách");
                }
            });
        } else {
            holder.tvInfo.setText("Tác giả này chưa có viết tác phẩm nào");
        }

        // 4. Sự kiện Click
        holder.ivEdit.setOnClickListener(v -> listener.onEditClick(author));
        holder.ivDelete.setOnClickListener(v -> listener.onDeleteClick(author));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(author));
    }

    @Override
    public int getItemCount() {
        return authorList != null ? authorList.size() : 0;
    }
}