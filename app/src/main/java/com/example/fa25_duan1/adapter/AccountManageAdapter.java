package com.example.fa25_duan1.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.MenuItem;
import com.example.fa25_duan1.model.User;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountManageAdapter extends RecyclerView.Adapter<AccountManageAdapter.ViewHolder>  {
    private final ArrayList<User> userList;
    private final Context context;

    public interface OnItemClickListener {
        void onEditClick(User user);
        void onDeleteClick(User user);
    }

    private final OnItemClickListener listener;

    public AccountManageAdapter(Context context, ArrayList<User> userList, OnItemClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivEdit, ivDelete;
        TextView tvName, tvTag, tvId;
        CircleImageView cvAvatar;
        public ViewHolder(View itemView) {
            super(itemView);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivDelete = itemView.findViewById(R.id.iv_delete);
            tvName = itemView.findViewById(R.id.tv_name);
            tvTag = itemView.findViewById(R.id.tv_tag);
            tvId = itemView.findViewById(R.id.tv_id);
            cvAvatar = itemView.findViewById(R.id.cv_avatar);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View view = inflater.inflate(R.layout.item_account, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = userList.get(position);
        String role = "";
        if (user.getRole() == 0) {
            role = "Khách hàng";
        }
        else if (user.getRole() == 1) {
            role = "Nhân viên";
        }
        else {
            role = "Admin";
        }

        holder.cvAvatar.setImageResource(user.getAvatarId());
        holder.tvName.setText(user.getName());
        holder.tvTag.setText(role);
        holder.tvId.setText(user.getUserID());

        holder.ivEdit.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null) {
                listener.onEditClick(userList.get(position));
            }
        });

        holder.ivDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null) {
                listener.onDeleteClick(userList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
