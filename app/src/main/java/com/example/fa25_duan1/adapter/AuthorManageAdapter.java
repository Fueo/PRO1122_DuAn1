package com.example.fa25_duan1.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.Author;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AuthorManageAdapter extends RecyclerView.Adapter<AuthorManageAdapter.ViewHolder> {

    private List<Author> authorList;
    private final Context context;
    private final OnItemClickListener listener;

    // Interface để handle sự kiện click ra bên ngoài (Fragment/Activity)
    public interface OnItemClickListener {
        void onEditClick(Author author);
        void onDeleteClick(Author author);
        void onItemClick(Author author);
    }

    public AuthorManageAdapter(Context context, List<Author> authorList, OnItemClickListener listener) {
        this.context = context;
        this.authorList = authorList;
        this.listener = listener;
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
            // Ánh xạ ID theo file xml bạn cung cấp
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            tvName = itemView.findViewById(R.id.tvName);
            tvInfo = itemView.findViewById(R.id.tvInfo); // Thay vì tvTag
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
                    .placeholder(R.drawable.ic_avatar_placeholder) // Đảm bảo bạn có ảnh này
                    .error(R.drawable.ic_avatar_placeholder)
                    .into(holder.cvAvatar);
        } else {
            holder.cvAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
        }

        // 2. Hiển thị Tên
        holder.tvName.setText(author.getName());

        // 3. Hiển thị Info (Description)
        String description = "Tác giả của 5 đầu sách";
        holder.tvInfo.setText(description);

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