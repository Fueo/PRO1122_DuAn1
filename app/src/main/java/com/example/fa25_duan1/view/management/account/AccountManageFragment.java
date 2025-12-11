package com.example.fa25_duan1.view.management.account;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.AccountManageAdapter;
import com.example.fa25_duan1.model.ApiResponse; // IMPORT QUAN TRỌNG
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.view.management.UpdateActivity;
import com.example.fa25_duan1.viewmodel.UserViewModel;

import java.util.ArrayList;

// --- THƯ VIỆN UI ---
import com.shashank.sony.fancytoastlib.FancyToast;
import io.github.cutelibs.cutedialog.CuteDialog;

public class AccountManageFragment extends Fragment {

    private RecyclerView rvData;
    private Button btnAdd;
    private AccountManageAdapter accountManageAdapter;
    private UserViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accountmanagement, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Filter Fragment
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_filter, new AccountFilterFragment())
                    .commit();
        }

        rvData = view.findViewById(R.id.rvData);
        btnAdd = view.findViewById(R.id.btnAdd);

        // Setup Adapter
        accountManageAdapter = new AccountManageAdapter(getContext(), new ArrayList<>(), new AccountManageAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(User user) {
                openUpdateActivity(user);
            }

            @Override
            public void onDeleteClick(User user) {
                deleteUser(user);
            }

            @Override
            public void onItemClick(User user) {
                FancyToast.makeText(getContext(),
                        "Đã chọn: " + user.getName(),
                        FancyToast.LENGTH_SHORT,
                        FancyToast.INFO,
                        false).show();
            }
        });

        rvData.setLayoutManager(new LinearLayoutManager(getContext()));
        rvData.setAdapter(accountManageAdapter);

        // Setup ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // Observe List User (Phần này giữ nguyên vì ViewModel đã xử lý List<User> bên trong)
        viewModel.getDisplayedUsers().observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                accountManageAdapter.setData(users);
            }
        });

        btnAdd.setOnClickListener(v -> openUpdateActivity(null));
    }

    private void openUpdateActivity(User user) {
        Intent intent = new Intent(getContext(), UpdateActivity.class);
        intent.putExtra(UpdateActivity.EXTRA_HEADER_TITLE, "Thêm mới user");

        if (user != null) {
            intent.putExtra(UpdateActivity.EXTRA_HEADER_TITLE, "Chỉnh sửa user");
            intent.putExtra("Id", user.getUserID());
        }
        intent.putExtra(UpdateActivity.EXTRA_CONTENT_FRAGMENT, "account");

        startActivityForResult(intent, 1001);
    }

    private void deleteUser(User user) {
        if (user == null) return;

        // --- BƯỚC 1: LẤY ID CỦA USER ĐANG ĐĂNG NHẬP ---
        android.content.SharedPreferences sharedPreferences = requireActivity()
                .getSharedPreferences("MyPrefs", android.content.Context.MODE_PRIVATE);

        String currentUserId = sharedPreferences.getString("userId", "");

        // --- BƯỚC 2: KIỂM TRA TRÙNG KHỚP ---
        if (user.getUserID().equals(currentUserId)) {
            FancyToast.makeText(getContext(),
                    "Không thể xóa tài khoản đang đăng nhập!",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.ERROR,
                    false).show();
            return;
        }

        // --- BƯỚC 3: HIỆN DIALOG XÁC NHẬN ---
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_confirm)
                .setTitle("Xóa User")
                .setDescription("Bạn có chắc chắn muốn xóa user: " + user.getName() + "?")
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.gray_text)
                .setPositiveButtonText("Xóa", v -> {
                    performDelete(user);
                })
                .setNegativeButtonText("Hủy", v -> {
                })
                .show();
    }

    // --- [QUAN TRỌNG] CẬP NHẬT LOGIC HỨNG API RESPONSE ---
    private void performDelete(User user) {
        viewModel.deleteUser(user.getUserID()).observe(getViewLifecycleOwner(), response -> {
            // 1. Kiểm tra null (Lỗi mạng nghiêm trọng hoặc crash)
            if (response == null) {
                FancyToast.makeText(getContext(), "Lỗi không xác định!", FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
                return;
            }

            // 2. Kiểm tra status từ Backend
            if (response.isStatus()) {
                // --- THÀNH CÔNG ---
                viewModel.refreshData(); // Load lại danh sách

                // Lấy message thành công từ Backend (vd: "Xóa thành công")
                String msg = response.getMessage() != null ? response.getMessage() : "Đã xóa user thành công!";

                new CuteDialog.withIcon(requireActivity())
                        .setIcon(R.drawable.ic_dialog_success)
                        .setTitle("Thành công")
                        .setDescription(msg) // Hiển thị message từ Server

                        .setPrimaryColor(R.color.blue)
                        .setPositiveButtonColor(R.color.blue)
                        .setTitleTextColor(R.color.black)
                        .setDescriptionTextColor(R.color.gray_text)

                        .setPositiveButtonText("Đóng", v -> {
                        })
                        .hideNegativeButton(true)
                        .show();
            } else {
                // --- THẤT BẠI ---
                // Lấy message lỗi từ Backend (vd: "Bạn không được xóa Admin")
                String errorMsg = response.getMessage();
                if (errorMsg == null || errorMsg.isEmpty()) errorMsg = "Xóa thất bại! Vui lòng thử lại.";

                FancyToast.makeText(getContext(),
                        errorMsg,
                        FancyToast.LENGTH_SHORT,
                        FancyToast.ERROR,
                        false).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            viewModel.refreshData();
        }
    }
}