package com.example.fa25_duan1.view.management;

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
import com.example.fa25_duan1.view.detail.DetailActivity;
import com.example.fa25_duan1.view.home.HeaderHomeFragment;
import com.example.fa25_duan1.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.List;

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
                handleAddEditUser(user);
            }

            @Override
            public void onDeleteClick(User user) {
                handleDeleteUser(user);
            }

            @Override
            public void onItemClick(User user) {
                Toast.makeText(getActivity(), "Click vào: " + user.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        rvData.setLayoutManager(new LinearLayoutManager(getContext()));
        rvData.setAdapter(accountManageAdapter);

        viewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // GET all users
        viewModel.getAllUsers().observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                accountManageAdapter.setData(users);
            } else {
                Toast.makeText(getContext(), "Lấy danh sách user thất bại", Toast.LENGTH_SHORT).show();
            }
        });

        btnAdd.setOnClickListener(v -> handleAddEditUser(null));
    }

    private void handleAddEditUser(User user) {
        Intent intent = new Intent(getContext(), UpdateActivity.class);
        if (user != null) {
            intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Chỉnh sửa user");
            intent.putExtra("Id", user.getUserID());
        } else {
            intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Thêm mới user");
        }
        intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "account");
        startActivity(intent);
    }

    private void handleDeleteUser(User user) {
        if (user == null) return;

        viewModel.deleteUser(user.getUserID()).observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(getContext(), "Xóa thành công", Toast.LENGTH_SHORT).show();

                List<User> updatedList = new ArrayList<>(accountManageAdapter.getUserList());
                updatedList.remove(user);
                accountManageAdapter.setData(updatedList);
            } else {
                Toast.makeText(getContext(), "Xóa thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
