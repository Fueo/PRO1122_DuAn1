package com.example.fa25_duan1.view.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
// import android.widget.LinearLayout; // Không cần thiết nữa nếu dùng View chung

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.BookGridAdapter;
import com.example.fa25_duan1.model.Product;
import com.example.fa25_duan1.view.detail.ProductListFilterFragment;
import com.example.fa25_duan1.viewmodel.ProductViewModel;

import java.util.ArrayList;
import java.util.List;

public class ProductFragment extends Fragment {

    private RecyclerView rvProducts;
    private View layoutEmpty; // SỬA: Đổi từ LinearLayout thành View để an toàn với thẻ <include>
    private View fragmentFilterContainer; // Thêm biến để ẩn hiện khung Filter
    private BookGridAdapter bookGridAdapter;
    private ProductViewModel productViewModel;
    private String searchQuery = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product, container, false);

        initViews(view);

        // Nhận dữ liệu tìm kiếm từ Bundle
        if (getArguments() != null) {
            searchQuery = getArguments().getString("search_query");
        }

        setupLogicBasedOnSearch(savedInstanceState);
        setupProductGrid();
        setupViewModel();

        return view;
    }

    private void initViews(View view) {
        rvProducts = view.findViewById(R.id.rv_products);
        layoutEmpty = view.findViewById(R.id.layout_empty); // Ánh xạ layout empty
        fragmentFilterContainer = view.findViewById(R.id.fragment_filter); // Ánh xạ khung filter
    }

    private void setupLogicBasedOnSearch(Bundle savedInstanceState) {
        if (searchQuery != null && !searchQuery.isEmpty()) {
            // TRƯỜNG HỢP TÌM KIẾM:
            // Ẩn khung Filter đi để dành chỗ hiển thị kết quả tìm kiếm
            if (fragmentFilterContainer != null) {
                fragmentFilterContainer.setVisibility(View.GONE);
            }
        } else {
            // TRƯỜNG HỢP MẶC ĐỊNH:
            // Hiện khung Filter
            if (fragmentFilterContainer != null) {
                fragmentFilterContainer.setVisibility(View.VISIBLE);
            }
            // Nạp Fragment Filter vào FrameLayout
            if (savedInstanceState == null) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_filter, new ProductListFilterFragment())
                        .commit();
            }
        }
    }

    private void setupProductGrid() {
        bookGridAdapter = new BookGridAdapter(getContext(), new ArrayList<>(), product -> {
            // Xử lý khi click vào item sách
            Toast.makeText(requireActivity(), "Xem: " + product.getName(), Toast.LENGTH_SHORT).show();
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvProducts.setLayoutManager(gridLayoutManager);
        rvProducts.setAdapter(bookGridAdapter);
    }

    private void setupViewModel() {
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        if (searchQuery != null && !searchQuery.isEmpty()) {
            // Gọi API Search
            productViewModel.searchProductsByNameApi(searchQuery).observe(getViewLifecycleOwner(), products -> {
                checkDataAndShow(products);
            });
        } else {
            // Gọi API lấy danh sách hiển thị (đã qua lọc hoặc mặc định)
            productViewModel.getDisplayedProducts().observe(getViewLifecycleOwner(), products -> {
                checkDataAndShow(products);
            });
        }
    }

    // Hàm xử lý ẩn hiện layout Empty/RecyclerView
    private void checkDataAndShow(List<Product> products) {
        if (products == null || products.isEmpty()) {
            // KHÔNG CÓ DỮ LIỆU:
            rvProducts.setVisibility(View.GONE);    // Ẩn danh sách
            layoutEmpty.setVisibility(View.VISIBLE); // Hiện layout empty

            // Set list rỗng để tránh lỗi adapter
            bookGridAdapter.setProducts(new ArrayList<>());
        } else {
            // CÓ DỮ LIỆU:
            rvProducts.setVisibility(View.VISIBLE); // Hiện danh sách
            layoutEmpty.setVisibility(View.GONE);   // Ẩn layout empty

            bookGridAdapter.setProducts(products);
        }
    }
}