package com.example.fa25_duan1.view.management.account;

import android.app.Activity;
import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.AccountManageAdapter;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.view.dialog.ConfirmDialogFragment;
import com.example.fa25_duan1.view.dialog.NotificationDialogFragment;
import com.example.fa25_duan1.viewmodel.UserViewModel;
import com.example.fa25_duan1.view.management.UpdateActivity;

import java.util.ArrayList;

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

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_filter, new AccountFilterFragment())
                .commit();

        rvData = view.findViewById(R.id.rvData);
        btnAdd = view.findViewById(R.id.btnAdd);

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
                Toast.makeText(getActivity(), "Click vào: " + user.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        rvData.setLayoutManager(new LinearLayoutManager(getContext()));
        rvData.setAdapter(accountManageAdapter);

        // Sử dụng ViewModel scoped to Activity nếu muốn chia sẻ dữ liệu giữa Fragment
// Trong Fragment
        viewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        viewModel.getDisplayedUsers().observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                accountManageAdapter.setData(users); // adapter sẽ notifyDataSetChanged()
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

        ConfirmDialogFragment dialog = new ConfirmDialogFragment(
                "Xóa user",
                "Xác nhận xoá user có tên " + user.getName(),
                new ConfirmDialogFragment.OnConfirmListener() {
                    @Override
                    public void onConfirmed() {
                        // Gọi API và observe kết quả
                        viewModel.deleteUser(user.getUserID()).observe(getViewLifecycleOwner(), success -> {
                            if (success != null && success) {
                                // Nếu API báo thành công, tải lại toàn bộ danh sách
                                viewModel.refreshData();

                                // Hiển thị thông báo thành công
                                NotificationDialogFragment dialogFragment = NotificationDialogFragment.newInstance(
                                        "Thành công",
                                        "Bạn đã xóa user thành công",
                                        "Đóng",
                                        NotificationDialogFragment.TYPE_SUCCESS,
                                        () -> {}
                                );
                                dialogFragment.show(getParentFragmentManager(), "SuccessDialog");
                            } else {
                                // Xử lý khi xóa thất bại (optional)
                                Toast.makeText(getContext(), "Xóa thất bại", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
        );
        dialog.show(getParentFragmentManager(), "ConfirmDialog");
    }

    // Không cần gọi loadUsers() ở onActivityResult vì LiveData tự cập nhật
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            viewModel.refreshData();
        }
    }
}
