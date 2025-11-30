package com.example.fa25_duan1.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.Address;

import java.util.ArrayList;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private List<Address> mList;
    private final OnAddressActionListener mListener;

    // --- 1. THÊM BIẾN CỜ (Mặc định là true để hiện nút ở các màn hình khác) ---
    private boolean isShowActionButtons = true;

    public interface OnAddressActionListener {
        void onEdit(Address address);
        void onDelete(Address address);
        void onItemClick(Address address); // Đảm bảo có hàm này
    }

    public AddressAdapter(OnAddressActionListener listener) {
        this.mList = new ArrayList<>();
        this.mListener = listener;
    }

    // --- 2. HÀM SETTER ĐỂ CHỈNH CỜ ---
    public void setShowActionButtons(boolean show) {
        this.isShowActionButtons = show;
        notifyDataSetChanged();
    }

    public void setList(List<Address> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = mList.get(position);
        if (address == null) return;

        holder.tvName.setText(address.getName());
        holder.tvPhone.setText(address.getPhone());
        holder.tvAddress.setText(address.getAddress());
        holder.tvTag.setText(address.getTag());

        if (address.isDefault()) {
            holder.tvDefaultLabel.setVisibility(View.VISIBLE);
        } else {
            holder.tvDefaultLabel.setVisibility(View.GONE);
        }

        // --- 3. XỬ LÝ ẨN/HIỆN NÚT DỰA TRÊN CỜ ---
        if (isShowActionButtons) {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
        } else {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }

        // Các sự kiện nút bấm
        holder.btnEdit.setOnClickListener(v -> mListener.onEdit(address));
        holder.btnDelete.setOnClickListener(v -> mListener.onDelete(address));

        // --- 4. QUAN TRỌNG: BẮT SỰ KIỆN CLICK VÀO CẢ ITEM ĐỂ CHỌN ---
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                // Nếu đang ở chế độ chọn (ẩn nút), click vào item sẽ chọn địa chỉ đó
                mListener.onItemClick(address);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTag, tvPhone, tvAddress, tvDefaultLabel;
        ImageView btnEdit, btnDelete;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvTag = itemView.findViewById(R.id.tvTag);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDefaultLabel = itemView.findViewById(R.id.tvDefaultLabel);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}