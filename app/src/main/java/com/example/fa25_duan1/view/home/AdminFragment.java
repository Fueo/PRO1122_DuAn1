package com.example.fa25_duan1.view.home;

import static com.example.fa25_duan1.data.MenuAdminData.getListMenuAdminData;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.ActionButtonAdapter;
import com.example.fa25_duan1.model.MenuItem;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.model.statistic.StatsDashboard;
import com.example.fa25_duan1.view.management.ManageActivity;
import com.example.fa25_duan1.viewmodel.AuthViewModel;
import com.example.fa25_duan1.viewmodel.StatisticViewModel; // [MỚI] Import StatisticViewModel

import java.util.ArrayList;
import java.util.Iterator;

public class AdminFragment extends Fragment {

    // Khai báo biến dữ liệu menu
    ArrayList<MenuItem> listButtonData;
    RecyclerView rvButton;
    ActionButtonAdapter adapter;

    // Khai báo các TextView thống kê
    private TextView tvTotalProduct;
    private TextView tvTotalCategory;
    private TextView tvTotalAccount;
    private TextView tvTotalInvoice;

    // Khai báo các ViewModel
    private AuthViewModel authViewModel;
    private StatisticViewModel statisticViewModel; // [MỚI] Thay thế các ViewModel lẻ

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupMenuRecyclerView();
        setupViewModels();
    }

    private void initViews(View view) {
        rvButton = view.findViewById(R.id.rvButton);
        tvTotalProduct = view.findViewById(R.id.tvTotalProduct);
        tvTotalCategory = view.findViewById(R.id.tvTotalCategory);
        tvTotalAccount = view.findViewById(R.id.tvTotalAccount);
        tvTotalInvoice = view.findViewById(R.id.tvTotalInvoice);
    }

    private void setupViewModels() {
        // --- AUTH VIEW MODEL (Để check quyền Role 1) ---
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        authViewModel.getMyInfo().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getData() != null) {
                User currentUser = response.getData();

                // Nếu là Nhân viên (Role = 1) -> Ẩn bớt chức năng quản lý sâu
                if (currentUser.getRole() == 1) {
                    filterMenuForEmployee();
                }
            }
        });

        // --- STATISTIC VIEW MODEL (Để lấy số liệu Dashboard) ---
        statisticViewModel = new ViewModelProvider(this).get(StatisticViewModel.class);

        // Gọi dữ liệu lần đầu
        loadData();
    }

    // Hàm gọi API lấy tổng quan dashboard (1 API trả về cả 4 số liệu)
    private void loadData() {
        if (statisticViewModel != null) {
            statisticViewModel.getDashboardStats().observe(getViewLifecycleOwner(), response -> {
                if (response != null && response.isStatus() && response.getData() != null) {
                    StatsDashboard stats = response.getData();

                    // Cập nhật UI
                    tvTotalProduct.setText(String.valueOf(stats.getTotalProducts()));
                    tvTotalCategory.setText(String.valueOf(stats.getTotalCategories()));
                    tvTotalAccount.setText(String.valueOf(stats.getTotalUsers()));
                    tvTotalInvoice.setText(String.valueOf(stats.getTotalOrders()));
                } else {
                    // Xử lý khi lỗi hoặc không có dữ liệu
                    tvTotalProduct.setText("0");
                    tvTotalCategory.setText("0");
                    tvTotalAccount.setText("0");
                    tvTotalInvoice.setText("0");
                }
            });
        }
    }

    private void filterMenuForEmployee() {
        if (listButtonData != null) {
            Iterator<MenuItem> iterator = listButtonData.iterator();
            while (iterator.hasNext()) {
                MenuItem item = iterator.next();
                // Ẩn Quản lý Account (5) và Thống kê chi tiết (6) đối với nhân viên
                if (item.getId() == 5 || item.getId() == 6) {
                    iterator.remove();
                }
            }
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void setupMenuRecyclerView() {
        listButtonData = getListMenuAdminData();
        GridLayoutManager gridLayoutManager1 = new GridLayoutManager(getActivity(), 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

        rvButton.setLayoutManager(gridLayoutManager1);
        adapter = new ActionButtonAdapter(getActivity(), listButtonData, item -> {
            Intent intent = new Intent(getActivity(), ManageActivity.class);
            switch (item.getId()) {
                case 0:
                    intent.putExtra(ManageActivity.EXTRA_CONTENT_FRAGMENT, "category");
                    break;
                case 1:
                    intent.putExtra(ManageActivity.EXTRA_CONTENT_FRAGMENT, "product");
                    break;
                case 2:
                    intent.putExtra(ManageActivity.EXTRA_CONTENT_FRAGMENT, "order");
                    break;
                case 3:
                    intent.putExtra(ManageActivity.EXTRA_CONTENT_FRAGMENT, "discount");
                    break;
                case 4:
                    intent.putExtra(ManageActivity.EXTRA_CONTENT_FRAGMENT, "author");
                    break;
                case 5:
                    intent.putExtra(ManageActivity.EXTRA_CONTENT_FRAGMENT, "account");
                    break;
                case 6:
                    intent.putExtra(ManageActivity.EXTRA_CONTENT_FRAGMENT, "statistic");
                    break;
            }
            startActivity(intent);
        });

        rvButton.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh lại dữ liệu mỗi khi quay lại màn hình này
        loadData();

        if (authViewModel != null) {
            authViewModel.getMyInfo();
        }
    }
}