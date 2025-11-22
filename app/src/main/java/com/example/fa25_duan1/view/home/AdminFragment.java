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
import com.example.fa25_duan1.view.detail.DetailActivity;
import com.example.fa25_duan1.view.management.ManageActivity;
import com.example.fa25_duan1.viewmodel.CategoryViewModel;
import com.example.fa25_duan1.viewmodel.ProductViewModel;
import com.example.fa25_duan1.viewmodel.UserViewModel;

import java.util.ArrayList;

public class AdminFragment extends Fragment {

    // Khai báo biến dữ liệu menu
    ArrayList<MenuItem> listButtonData;
    RecyclerView rvButton;

    // Khai báo các TextView thống kê
    private TextView tvTotalProduct;
    private TextView tvTotalCategory;
    private TextView tvTotalAccount;
    // private TextView tvTotalInvoice; // Bỏ qua theo yêu cầu

    // Khai báo các ViewModel
    private ProductViewModel productViewModel;
    private CategoryViewModel categoryViewModel;
    private UserViewModel userViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        initViews(view);

        // 2. Thiết lập RecyclerView Menu (Code cũ giữ nguyên)
        setupMenuRecyclerView();

        // 3. Khởi tạo ViewModels và quan sát dữ liệu (Observe)
        setupViewModels();
    }

    private void initViews(View view) {
        rvButton = view.findViewById(R.id.rvButton);

        // Ánh xạ các TextView hiển thị số liệu từ XML
        tvTotalProduct = view.findViewById(R.id.tvTotalProduct);
        tvTotalCategory = view.findViewById(R.id.tvTotalCategory);
        tvTotalAccount = view.findViewById(R.id.tvTotalAccount);
        // tvTotalInvoice = view.findViewById(R.id.tvTotalInvoice);
    }

    private void setupViewModels() {
        // --- PRODUCT VIEW MODEL ---
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        // Lắng nghe danh sách sản phẩm để lấy tổng số lượng
        productViewModel.getDisplayedProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                tvTotalProduct.setText(String.valueOf(products.size()));
            } else {
                tvTotalProduct.setText("0");
            }
        });

        // --- CATEGORY VIEW MODEL ---
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        // Lắng nghe danh sách danh mục
        categoryViewModel.getDisplayedCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                tvTotalCategory.setText(String.valueOf(categories.size()));
            } else {
                tvTotalCategory.setText("0");
            }
        });

        // --- USER (ACCOUNT) VIEW MODEL ---
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        // Lắng nghe danh sách tài khoản
        userViewModel.getDisplayedUsers().observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                tvTotalAccount.setText(String.valueOf(users.size()));
            } else {
                tvTotalAccount.setText("0");
            }
        });
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
        ActionButtonAdapter adapter = new ActionButtonAdapter(getActivity(), listButtonData, item -> {
            Intent intent = new Intent(getActivity(), ManageActivity.class);
            switch (item.getId()) {
                case 0:
                    intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "category");
                    break;
                case 1:
                    intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "product");
                    break;
                case 2:
                    Toast.makeText(getActivity(), "Vào trang Invoice", Toast.LENGTH_SHORT).show();
                    return; // Thêm return để không start activity rỗng nếu chưa implement
                case 3:
                    Toast.makeText(getActivity(), "Vào trang Sales", Toast.LENGTH_SHORT).show();
                    return;
                case 4:
                    intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "author");
                    break;
                case 5:
                    intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "account");
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
        // Gọi refresh để đảm bảo số liệu cập nhật khi quay lại từ màn hình thêm/sửa/xóa
        if(productViewModel != null) productViewModel.refreshData();
        if(categoryViewModel != null) categoryViewModel.refreshData();
        if(userViewModel != null) userViewModel.refreshData();
    }
}