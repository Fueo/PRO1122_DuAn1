package com.example.fa25_duan1.view.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.viewmodel.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class ChangePasswordFragment extends Fragment {

    private TextInputEditText edtPassword, edtNewPassword, edtConfirmPassword;
    private Button btnSave;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_changepassword_employ, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        edtPassword = view.findViewById(R.id.edtPassword);
        edtNewPassword = view.findViewById(R.id.edtNewPassword);
        edtConfirmPassword = view.findViewById(R.id.edtConfirmPassword);
        btnSave = view.findViewById(R.id.btnSave);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        // 2. Xử lý lưu
        btnSave.setOnClickListener(v -> {
            String currentPass = edtPassword.getText() != null ? edtPassword.getText().toString().trim() : "";
            String newPass = edtNewPassword.getText() != null ? edtNewPassword.getText().toString().trim() : "";
            String confirmPass = edtConfirmPassword.getText() != null ? edtConfirmPassword.getText().toString().trim() : "";

            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPass.length() < 6) {
                Toast.makeText(getContext(), "Mật khẩu mới phải >= 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(confirmPass)) {
                Toast.makeText(getContext(), "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            authViewModel.changePassword(currentPass, newPass).observe(getViewLifecycleOwner(), response -> {
                if (response != null) {
                    Toast.makeText(getContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    edtPassword.setText("");
                    edtNewPassword.setText("");
                    edtConfirmPassword.setText("");
                    requireActivity().finish();
                } else {
                    Toast.makeText(getContext(), "Đổi thất bại (Mật khẩu cũ không đúng)", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}