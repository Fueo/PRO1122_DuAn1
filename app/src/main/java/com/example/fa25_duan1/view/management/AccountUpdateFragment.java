package com.example.fa25_duan1.view.management;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.viewmodel.UserViewModel;

public class AccountUpdateFragment extends Fragment {
    RecyclerView rvData;
    Spinner spRole;
    EditText edtUsername, edtPassword, edtNewPassword, edtConfirmPassword, edtFullName, edtPhone, edtEmail, edtAddress;
    ImageView ivProfile;
    private UserViewModel viewModel;
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accountupdate, container, false);

        return view;
    }
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UserViewModel.class);
        edtUsername = view.findViewById(R.id.edtUsername);
        edtPassword = view.findViewById(R.id.edtPassword);
        edtNewPassword = view.findViewById(R.id.edtNewPassword);
        edtConfirmPassword = view.findViewById(R.id.edtConfirmPassword);
        edtFullName = view.findViewById(R.id.edtFullName);
        edtPhone = view.findViewById(R.id.edtPhone);
        edtEmail = view.findViewById(R.id.edtEmail);
        edtAddress = view.findViewById(R.id.edtAddress);
        ivProfile = view.findViewById(R.id.ivProfile);

// 1. Lấy ID được truyền từ màn hình danh sách
        String userId = getActivity().getIntent().getStringExtra("Id");

        if (userId != null) {
            edtUsername.setEnabled(false);
            edtUsername.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
            // 2. Gọi ViewModel để lấy thông tin chi tiết
            viewModel.getUserByID(userId).observe(this, user -> {
                if (user != null) {
                    // 3. Điền dữ liệu vào form
                    spRole.setSelection(user.getRole());
                    edtUsername.setText(user.getUsername());
                    edtFullName.setText(user.getName());
                    edtEmail.setText(user.getEmail());
                    edtPhone.setText(user.getPhone());
                    edtAddress.setText(user.getAddress());

                    if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                        Glide.with(getContext())
                                .load(user.getAvatar())
                                .placeholder(R.drawable.ic_avatar_placeholder)
                                .error(R.drawable.ic_avatar_placeholder)
                                .into(ivProfile);
                    } else {
                        ivProfile.setImageResource(R.drawable.ic_avatar_placeholder);
                    }
                    // Nếu có xử lý avatar
                    // Glide.with(this).load(user.getAvatar()).into(imgAvatar);
                } else {
                    Toast.makeText(getActivity(), "Lỗi: Không tìm thấy user này", Toast.LENGTH_SHORT).show();
                }
            });
        }

        String[] items = {"Khách hàng", "Nhân viên", "Admin"};
        spRole = view.findViewById(R.id.spRole);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getContext(), R.layout.item_spinner, items) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view.findViewById(R.id.tvSpinnerItem);

                return view;
            }
        };
        spRole.setAdapter(adapter);
    }
}
