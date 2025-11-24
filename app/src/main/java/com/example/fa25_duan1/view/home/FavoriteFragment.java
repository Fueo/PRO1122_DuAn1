package com.example.fa25_duan1.view.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.example.fa25_duan1.view.detail.ProductDetailActivity;
import com.example.fa25_duan1.viewmodel.CartViewModel;
import com.example.fa25_duan1.viewmodel.FavoriteViewModel;
import com.example.fa25_duan1.viewmodel.ProductViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FavoriteFragment extends Fragment {

    private RecyclerView rvProducts;
    private View layoutEmpty;
    private ProgressBar progressBar;
    private TextView tvTitle;

    private BookGridAdapter bookGridAdapter;
    private FavoriteViewModel favoriteViewModel;
    private ProductViewModel productViewModel;
    private CartViewModel cartViewModel;

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
        setupViewModels();
        setupAdapter();
    }

    private void initViews(View view) {
        rvProducts = view.findViewById(R.id.rv_products);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        progressBar = view.findViewById(R.id.progressBar);
        tvTitle = view.findViewById(R.id.tvTitle);
    }

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
                favoriteViewModel.toggleFavorite(productId);
                bookGridAdapter.removeProductById(productId);
                int newCount = bookGridAdapter.getItemCount();
                updateTotalCount(newCount);
                if (newCount == 0) {
                    if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAddToCartClick(Product product) {
                if (product != null) {
                    cartViewModel.increaseQuantity(product.getId());
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

        // QUAN TRỌNG: Dùng requireActivity() để chia sẻ ViewModel với HomeActivity
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        favoriteViewModel.getFavoriteIds().observe(getViewLifecycleOwner(), ids -> {
            bookGridAdapter.setFavoriteIds(ids);

            if (ids == null || ids.isEmpty()) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
                rvProducts.setVisibility(View.GONE);
                bookGridAdapter.setProducts(new ArrayList<>());
                updateTotalCount(0);
            } else {
                if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
                rvProducts.setVisibility(View.VISIBLE);
                fetchProductsFromIds(ids);
            }
        });

        // --- SỬA TẠI ĐÂY ---
        cartViewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                String msgLower = msg.toLowerCase();
                if (msgLower.contains("giỏ") || msgLower.contains("kho") ||
                        msgLower.contains("thành công") || msgLower.contains("hết")) {
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                }
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
                if (completedRequests.incrementAndGet() == totalRequests) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    bookGridAdapter.setProducts(new ArrayList<>(tempProductList));
                    updateTotalCount(tempProductList.size());
                }
            });
        }
    }
}