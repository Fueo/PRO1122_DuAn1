package com.example.fa25_duan1.view.home;

import android.content.Intent; // 1. Thêm import Intent
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.BookGridAdapter;
import com.example.fa25_duan1.model.Product;
import com.example.fa25_duan1.view.detail.ProductDetailActivity; // 2. Thêm import Detail Activity
import com.example.fa25_duan1.view.detail.ProductListFilterFragment;
import com.example.fa25_duan1.viewmodel.FavoriteViewModel; // 3. Thêm import FavoriteViewModel
import com.example.fa25_duan1.viewmodel.ProductViewModel;

import java.util.ArrayList;
import java.util.List;

public class ProductFragment extends Fragment {

    private RecyclerView rvProducts;
    private View layoutEmpty;
    private View fragmentFilterContainer;
    private BookGridAdapter bookGridAdapter;

    private ProductViewModel productViewModel;
    private FavoriteViewModel favoriteViewModel; // 4. Khai báo FavoriteViewModel

    private String searchQuery = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product, container, false);

        initViews(view);

        if (getArguments() != null) {
            searchQuery = getArguments().getString("search_query");
        }

        // Khởi tạo ViewModel sớm để dùng trong setupProductGrid
        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        favoriteViewModel = new ViewModelProvider(this).get(FavoriteViewModel.class); // 5. Init FavoriteViewModel

        setupLogicBasedOnSearch(savedInstanceState);
        setupProductGrid();
        setupDataObservation(); // Đổi tên hàm setupViewModel cũ cho rõ nghĩa

        return view;
    }

    private void initViews(View view) {
        rvProducts = view.findViewById(R.id.rv_products);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        fragmentFilterContainer = view.findViewById(R.id.fragment_filter);
    }

    private void setupLogicBasedOnSearch(Bundle savedInstanceState) {
        if (searchQuery != null && !searchQuery.isEmpty()) {
            if (fragmentFilterContainer != null) {
                fragmentFilterContainer.setVisibility(View.GONE);
            }
        } else {
            if (fragmentFilterContainer != null) {
                fragmentFilterContainer.setVisibility(View.VISIBLE);
            }
            if (savedInstanceState == null) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_filter, new ProductListFilterFragment())
                        .commit();
            }
        }
    }

    private void setupProductGrid() {
        // 6. CẬP NHẬT ADAPTER VỚI INTERFACE MỚI
        bookGridAdapter = new BookGridAdapter(getContext(), new ArrayList<>(), new BookGridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Product product) {
                // Chuyển sang màn hình chi tiết
                Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(String productId) {
                // Gọi ViewModel để xử lý thêm/xóa tim
                favoriteViewModel.toggleFavorite(productId);
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvProducts.setLayoutManager(gridLayoutManager);
        rvProducts.setAdapter(bookGridAdapter);
    }

    private void setupDataObservation() {
        // --- PHẦN 1: OBSERVE SẢN PHẨM ---
        if (searchQuery != null && !searchQuery.isEmpty()) {
            productViewModel.searchProductsByNameApi(searchQuery).observe(getViewLifecycleOwner(), products -> {
                checkDataAndShow(products);
            });
        } else {
            productViewModel.getDisplayedProducts().observe(getViewLifecycleOwner(), products -> {
                checkDataAndShow(products);
            });
        }

        // --- PHẦN 2: OBSERVE FAVORITE (MỚI) ---
        // Lắng nghe danh sách các ID đã like để cập nhật tim đỏ/trắng
        favoriteViewModel.getFavoriteIds().observe(getViewLifecycleOwner(), ids -> {
            if (bookGridAdapter != null) {
                bookGridAdapter.setFavoriteIds(ids);
            }
        });
    }

    private void checkDataAndShow(List<Product> products) {
        if (products == null || products.isEmpty()) {
            rvProducts.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            bookGridAdapter.setProducts(new ArrayList<>());
        } else {
            rvProducts.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            bookGridAdapter.setProducts(products);
        }
    }
}