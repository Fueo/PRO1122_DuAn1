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
import com.example.fa25_duan1.model.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountManageAdapter extends RecyclerView.Adapter<AccountManageAdapter.ViewHolder> {

    private List<User> userList;
    private final Context context;

    public interface OnItemClickListener {
        void onEditClick(User user);
        void onDeleteClick(User user);
        void onItemClick(User user);
    }

    private final OnItemClickListener listener;

    public AccountManageAdapter(Context context, List<User> userList, OnItemClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    public void setData(List<User> users){
        this.userList = users;
        notifyDataSetChanged();
    }

    public List<User> getUserList() {
        return userList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivEdit, ivDelete;
        TextView tvName, tvTag;
        CircleImageView cvAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivDelete = itemView.findViewById(R.id.iv_delete);
            tvName = itemView.findViewById(R.id.tv_name);
            tvTag = itemView.findViewById(R.id.tv_tag);
            cvAvatar = itemView.findViewById(R.id.cvAvatar);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_account, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = userList.get(position);
        // Load avatar
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            Glide.with(context)
                    .load(user.getAvatar())
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
                    .into(holder.cvAvatar);
        } else {
            holder.cvAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
        }

        // Hiển thị tên & role
        holder.tvName.setText(user.getName());
        String role = user.getRole() == 0 ? "Khách hàng" : user.getRole() == 1 ? "Nhân viên" : "Admin";
        holder.tvTag.setText(role);

        // Click events
        holder.ivEdit.setOnClickListener(v -> listener.onEditClick(user));
        holder.ivDelete.setOnClickListener(v -> listener.onDeleteClick(user));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(user));
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }
}
