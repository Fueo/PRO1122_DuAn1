package com.example.fa25_duan1.view.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.viewmodel.AuthViewModel;

public class ProfileFragment extends Fragment {
    TextView tvName, tvRole, tvPhone, tvEmail, tvAddress;
    ImageView ivProfile;
    AuthViewModel authViewModel;
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvName = view.findViewById(R.id.tvName);
        tvRole = view.findViewById(R.id.tvRole);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvAddress = view.findViewById(R.id.tvAddress);
        ivProfile = view.findViewById(R.id.ivProfile);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        authViewModel.getMyInfo().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getData() != null) {
                User user = response.getData();
                String role = user.getRole() == 0 ? "Khách hàng" : user.getRole() == 1 ? "Nhân viên" : "Admin";

                tvName.setText(user.getName());
                tvRole.setText(role);
                tvEmail.setText(user.getEmail());
                tvPhone.setText(
                        (user.getPhone() == null || user.getPhone().isEmpty())
                                ? "Chưa cập nhật"
                                : user.getPhone()
                );
                tvAddress.setText(
                        (user.getAddress() == null || user.getAddress().isEmpty())
                                ? "Chưa cập nhật"
                                : user.getAddress()
                );

                if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                    Glide.with(requireActivity())
                            .load(user.getAvatar())
                            .placeholder(R.drawable.ic_avatar_placeholder)
                            .error(R.drawable.ic_avatar_placeholder)
                            .into(ivProfile);
                } else {
                    ivProfile.setImageResource(R.drawable.ic_avatar_placeholder);
                }

            } else {
                Toast.makeText(requireContext(), "Không lấy được thông tin", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
