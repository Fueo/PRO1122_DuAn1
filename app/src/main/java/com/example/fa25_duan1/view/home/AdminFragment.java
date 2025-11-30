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
import com.example.fa25_duan1.view.management.ManageActivity;
import com.example.fa25_duan1.viewmodel.AuthViewModel;
import com.example.fa25_duan1.viewmodel.CategoryViewModel;
import com.example.fa25_duan1.viewmodel.ProductViewModel;
import com.example.fa25_duan1.viewmodel.UserViewModel;

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

    // Khai báo các ViewModel
    private ProductViewModel productViewModel;
    private CategoryViewModel categoryViewModel;
    private UserViewModel userViewModel;
    private AuthViewModel authViewModel;

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
    }

    private void setupViewModels() {
        // --- AUTH VIEW MODEL ---
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        authViewModel.getMyInfo().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getData() != null) {
                User currentUser = response.getData();

                // Nếu là Nhân viên (Role = 1)
                if (currentUser.getRole() == 1) {
                    // LƯU Ý: Không ẩn tvTotalAccount nữa vì Role 1 được phép xem số lượng
                    // tvTotalAccount.setVisibility(View.GONE); <--- Đã bỏ dòng này

                    // Chỉ ẩn Menu chức năng (để không vào được trang quản lý chi tiết)
                    filterMenuForEmployee();
                }
            }
        });

        // --- PRODUCT VIEW MODEL ---
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        productViewModel.getDisplayedProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                tvTotalProduct.setText(String.valueOf(products.size()));
            } else {
                tvTotalProduct.setText("0");
            }
        });

        // --- CATEGORY VIEW MODEL ---
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        categoryViewModel.getDisplayedCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                tvTotalCategory.setText(String.valueOf(categories.size()));
            } else {
                tvTotalCategory.setText("0");
            }
        });

        // --- USER (ACCOUNT) VIEW MODEL ---
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // CŨ: Lấy danh sách (Role 1 gọi cái này sẽ bị lỗi 403 Forbidden)
        // userViewModel.getDisplayedUsers().observe(...)

        // MỚI: Gọi API lấy tổng số lượng (Role 1 và 2 đều dùng được)
        loadTotalAccount();
    }

    // Hàm riêng để gọi API lấy số lượng Account
    private void loadTotalAccount() {
        if (userViewModel != null) {
            userViewModel.getTotalAccount().observe(getViewLifecycleOwner(), count -> {
                if (count != null) {
                    tvTotalAccount.setText(String.valueOf(count));
                } else {
                    tvTotalAccount.setText("0");
                }
            });
        }
    }

    private void filterMenuForEmployee() {
        if (listButtonData != null) {
            Iterator<MenuItem> iterator = listButtonData.iterator();
            while (iterator.hasNext()) {
                MenuItem item = iterator.next();
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
                    Toast.makeText(getActivity(), "Vào trang Invoice", Toast.LENGTH_SHORT).show();
                    return;
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
                    Toast.makeText(getActivity(), "Vào trang Statistic", Toast.LENGTH_SHORT).show();
                    return;
            }
            startActivity(intent);
        });

        rvButton.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(productViewModel != null) productViewModel.refreshData();
        if(categoryViewModel != null) categoryViewModel.refreshData();

        // KHÔNG gọi userViewModel.refreshData() vì hàm đó gọi getAllUsers() => Role 1 sẽ bị crash/lỗi mạng
        // if(userViewModel != null) userViewModel.refreshData();

        // Thay vào đó, gọi lại hàm lấy số lượng
        loadTotalAccount();

        if(authViewModel != null) authViewModel.getMyInfo();
    }
}