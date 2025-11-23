package com.example.fa25_duan1.view.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.BookGridAdapter;
import com.example.fa25_duan1.model.Product;
import com.example.fa25_duan1.view.detail.ProductDetailActivity;
import com.example.fa25_duan1.viewmodel.FavoriteViewModel;
import com.example.fa25_duan1.viewmodel.ProductViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FavoriteFragment extends Fragment {

    private RecyclerView rvProducts;
    private View layoutEmpty;
    private ProgressBar progressBar;
    private TextView tvTitle; // 1. Khai báo TextView hiển thị số lượng

    private BookGridAdapter bookGridAdapter;
    private FavoriteViewModel favoriteViewModel;
    private ProductViewModel productViewModel;

    private List<Product> tempProductList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupAdapter();
        setupViewModels();
    }

    private void initViews(View view) {
        rvProducts = view.findViewById(R.id.rv_products);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        progressBar = view.findViewById(R.id.progressBar);
        tvTitle = view.findViewById(R.id.tvTitle); // 2. Ánh xạ TextView
    }

    // 3. Hàm cập nhật số lượng sản phẩm lên giao diện
    private void updateTotalCount(int count) {
        if (tvTitle != null) {
            tvTitle.setText(count + " Sản phẩm");
        }
    }

    private void setupAdapter() {
        bookGridAdapter = new BookGridAdapter(getContext(), new ArrayList<>(), new BookGridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Product product) {
                Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(String productId) {
                // Xử lý khi user bỏ tim
                favoriteViewModel.toggleFavorite(productId);

                // Xóa item khỏi list hiển thị
                bookGridAdapter.removeProductById(productId);

                // 4. CẬP NHẬT LẠI SỐ LƯỢNG KHI XÓA
                int newCount = bookGridAdapter.getItemCount();
                updateTotalCount(newCount);

                // Check hiển thị layout trống
                if (newCount == 0) {
                    if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
                }
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvProducts.setLayoutManager(gridLayoutManager);
        rvProducts.setAdapter(bookGridAdapter);
    }

    private void setupViewModels() {
        favoriteViewModel = new ViewModelProvider(this).get(FavoriteViewModel.class);
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // Lấy danh sách ID
        favoriteViewModel.getFavoriteIds().observe(getViewLifecycleOwner(), ids -> {
            bookGridAdapter.setFavoriteIds(ids);

            if (ids == null || ids.isEmpty()) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
                rvProducts.setVisibility(View.GONE);

                bookGridAdapter.setProducts(new ArrayList<>());
                updateTotalCount(0); // 5. Nếu list rỗng -> 0 Sản phẩm
            } else {
                if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
                rvProducts.setVisibility(View.VISIBLE);

                // Gọi hàm lấy chi tiết
                fetchProductsFromIds(ids);
            }
        });
    }

    private void fetchProductsFromIds(List<String> ids) {
        tempProductList.clear();

        AtomicInteger completedRequests = new AtomicInteger(0);
        int totalRequests = ids.size();

        for (String id : ids) {
            productViewModel.getProductByID(id).observe(getViewLifecycleOwner(), product -> {

                if (product != null) {
                    tempProductList.add(product);
                }

                // Khi chạy xong request cuối cùng
                if (completedRequests.incrementAndGet() == totalRequests) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                    // Update Adapter
                    bookGridAdapter.setProducts(new ArrayList<>(tempProductList));

                    // 6. CẬP NHẬT TỔNG SỐ LƯỢNG SAU KHI LOAD XONG
                    updateTotalCount(tempProductList.size());
                }
            });
        }
    }
}