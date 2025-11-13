package com.example.fa25_duan1.view.management;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.view.detail.DetailActivity;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.AccountManageAdapter;
import com.example.fa25_duan1.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AccountManageFragment extends Fragment {
    RecyclerView rvData;
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accountmanagement, container, false);

        return view;
    }
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Kiểm tra để tránh chèn nhiều lần khi rotate
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragment_filter, new AccountFilterFragment())
                    .commit();
        }

        List<User> users = Arrays.asList(
                new User(1, 0, "nguyenvana", "123456", "Nguyễn Văn A", -1),
                new User(2, 1, "tranthib", "abcdef", "Trần Thị B", -1),
                new User(3, 0, "lequangc", "qwerty", "Lê Quang C", -1),
                new User(4, 2, "phamminhd", "pass123", "Phạm Minh D", -1),
                new User(5, 1, "hoangthu", "admin2025", "Hoàng Thu", -1),
                new User(6, 0, "nguyentue", "hello123", "Nguyễn Tuệ", -1),
                new User(7, 0, "phamlong", "123qwe", "Phạm Long", -1),
                new User(8, 1, "trongnghia", "manager01", "Trọng Nghĩa", -1),
                new User(9, 2, "lethanh", "rootpass", "Lê Thành", -1),
                new User(10, 0, "vutruong", "truong123", "Vũ Trường", -1)
        );


        rvData = view.findViewById(R.id.rvData);
        AccountManageAdapter accountManageAdapter = new AccountManageAdapter(getContext(), new ArrayList<>(users), new AccountManageAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(User user) {
                Intent intent = new Intent(view.getContext(), UpdateActivity.class);
                intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Chỉnh sửa user");
                intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "account");
                startActivity(intent);
                Toast.makeText(getActivity(), "Thực hiện hành động edit" + user.getName(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onDeleteClick(User user) {
                Toast.makeText(getActivity(), "Thực hiện hành động delete" + user.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemClick(User user) {
                Toast.makeText(getActivity(), "Thực hiện hành động itemclick" + user.getName(), Toast.LENGTH_SHORT).show();
            }
        });
        rvData.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvData.setAdapter(accountManageAdapter);
    }
}
