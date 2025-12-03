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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
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
import com.shashank.sony.fancytoastlib.FancyToast;

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

    // List tạm để gom dữ liệu từ nhiều request lẻ
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

        // 1. Chỉ đăng ký lắng nghe (Observer) tại đây
        // Việc gọi dữ liệu sẽ để cho onResume lo
        setupViewModels();
    }

    @Override
    public void onResume() {
        super.onResume();

        // 2. Kích hoạt lấy dữ liệu mới nhất mỗi khi màn hình hiện lên
        // (Bao gồm lần đầu tiên và khi quay lại từ màn hình Detail)
        if (favoriteViewModel != null) {
            favoriteViewModel.refreshFavorites();
        }

        // Cập nhật giỏ hàng để đồng bộ Badge (nếu có)
        if (cartViewModel != null) {
            cartViewModel.refreshCart();
        }
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
                // Xử lý bỏ thích ngay tại màn hình này
                favoriteViewModel.toggleFavorite(productId);

                // Xóa khỏi adapter để UI cập nhật ngay lập tức
                bookGridAdapter.removeProductById(productId);

                // Cập nhật lại số lượng title
                int newCount = bookGridAdapter.getItemCount();
                updateTotalCount(newCount);

                // Nếu xóa hết thì hiện layout trống
                if (newCount == 0) {
                    if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAddToCartClick(Product product) {
                addToCartLogic(product);
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvProducts.setLayoutManager(gridLayoutManager);
        rvProducts.setAdapter(bookGridAdapter);
    }

    private void setupViewModels() {
        favoriteViewModel = new ViewModelProvider(this).get(FavoriteViewModel.class);
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        // Dùng requireActivity() để share ViewModel với Activity chính (đồng bộ giỏ hàng)
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // Lắng nghe sự thay đổi của danh sách ID yêu thích
        favoriteViewModel.getFavoriteIds().observe(getViewLifecycleOwner(), ids -> {
            // Cập nhật list ID cho adapter (để icon tim hiển thị đúng màu)
            bookGridAdapter.setFavoriteIds(ids);

            if (ids == null || ids.isEmpty()) {
                // Nếu không có ID nào -> Ẩn loading, hiện layout trống
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
                rvProducts.setVisibility(View.GONE);
                bookGridAdapter.setProducts(new ArrayList<>());
                updateTotalCount(0);
            } else {
                // Nếu có ID -> Hiện recyclerview, bắt đầu tải chi tiết sản phẩm
                if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
                rvProducts.setVisibility(View.VISIBLE);
                fetchProductsFromIds(ids);
            }
        });
    }

    private void addToCartLogic(Product product) {
        if (product == null) return;

        cartViewModel.increaseQuantity(product.getId()).observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isStatus()) {
                cartViewModel.refreshCart();
                FancyToast.makeText(getContext(), "Đã thêm vào giỏ hàng", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();
            } else {
                String msg = (response != null) ? response.getMessage() : "Lỗi kết nối";
                FancyToast.makeText(getContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
            }
        });
    }

    /**
     * Hàm tải chi tiết sản phẩm từ danh sách ID.
     * Đã được sửa lỗi duplicate item.
     */
    private void fetchProductsFromIds(List<String> ids) {
        // Clear danh sách cũ để tránh bị cộng dồn
        tempProductList.clear();

        if (ids == null || ids.isEmpty()) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            return;
        }

        // Dùng AtomicInteger để đếm số lượng request hoàn thành
        AtomicInteger completedRequests = new AtomicInteger(0);
        int totalRequests = ids.size();

        for (String id : ids) {
            // Lấy LiveData
            LiveData<Product> liveData = productViewModel.getProductByID(id);

            // Observer ẩn danh
            liveData.observe(getViewLifecycleOwner(), new Observer<Product>() {
                @Override
                public void onChanged(Product product) {
                    // [QUAN TRỌNG 1] Hủy đăng ký ngay lập tức để tránh gọi lại nhiều lần
                    liveData.removeObserver(this);

                    if (product != null) {
                        // [QUAN TRỌNG 2] Kiểm tra trùng lặp trước khi add (Double check)
                        boolean exists = false;
                        for (Product p : tempProductList) {
                            if (p.getId().equals(product.getId())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            tempProductList.add(product);
                        }
                    }

                    // Kiểm tra xem đã chạy xong hết tất cả ID chưa
                    if (completedRequests.incrementAndGet() == totalRequests) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);

                        // Update Adapter
                        bookGridAdapter.setProducts(new ArrayList<>(tempProductList));
                        updateTotalCount(tempProductList.size());

                        // Check lại lần cuối để hiện layout empty nếu cần
                        if (tempProductList.isEmpty() && layoutEmpty != null) {
                            layoutEmpty.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }
    }
}