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
import com.example.fa25_duan1.view.detail.DetailActivity;
import com.example.fa25_duan1.view.detail.ProductDetailActivity;
import com.example.fa25_duan1.viewmodel.CartViewModel;
import com.example.fa25_duan1.viewmodel.FavoriteViewModel;
import com.example.fa25_duan1.viewmodel.ProductViewModel;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends Fragment {

    private RecyclerView rvProducts;
    private View layoutEmpty;
    private ProgressBar progressBar;
    private TextView tvTitle;

    private BookGridAdapter bookGridAdapter;
    private FavoriteViewModel favoriteViewModel;
    private ProductViewModel productViewModel;
    private CartViewModel cartViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModels();
        setupAdapter();

        // Setup lắng nghe dữ liệu
        setupObservers();

        // Tải dữ liệu lần đầu
        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh lại ID để icon tim đúng trạng thái
        if (favoriteViewModel != null) favoriteViewModel.refreshFavorites();
        if (cartViewModel != null) cartViewModel.refreshCart();

        // Refresh lại danh sách sản phẩm (đề phòng user bỏ tim ở màn hình khác)
        loadData();
    }

    private void initViews(View view) {
        rvProducts = view.findViewById(R.id.rv_products);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        progressBar = view.findViewById(R.id.progressBar);
        tvTitle = view.findViewById(R.id.tvTitle);
    }

    private void initViewModels() {
        favoriteViewModel = new ViewModelProvider(this).get(FavoriteViewModel.class);
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
    }

    private void setupAdapter() {
        // Khởi tạo Adapter rỗng
        bookGridAdapter = new BookGridAdapter(getContext(), new ArrayList<>(), new BookGridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Product product) {
                Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(String productId) {
                // Khi bấm tim ở màn hình này -> Tức là Bỏ thích
                favoriteViewModel.toggleFavorite(productId);

                // Xóa ngay khỏi danh sách hiển thị (Hiệu ứng UI tức thì)
                bookGridAdapter.removeProductById(productId);

                // Cập nhật lại số lượng và giao diện trống nếu cần
                checkEmptyState();
            }

            @Override
            public void onAddToCartClick(Product product) {
                addToCartLogic(product);
            }

            @Override
            public void onBuyNowClick(Product product) {
                handleBuyNow(product);
            }
        });

        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvProducts.setAdapter(bookGridAdapter);
    }

    /**
     * Lắng nghe các thay đổi từ ViewModel (chủ yếu là list ID để tô màu tim)
     */
    private void setupObservers() {
        // Lắng nghe danh sách ID yêu thích để cập nhật icon tim (Đỏ/Xám) trong Adapter
        favoriteViewModel.getFavoriteIds().observe(getViewLifecycleOwner(), ids -> {
            if (bookGridAdapter != null) {
                bookGridAdapter.setFavoriteIds(ids);
            }
        });
    }

    /**
     * [QUAN TRỌNG] Gọi API lấy danh sách sản phẩm yêu thích (1 Request duy nhất)
     */
    private void loadData() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // Gọi API mới: getFavoriteProductsApi
        productViewModel.getFavoriteProductsApi().observe(getViewLifecycleOwner(), apiResponse -> {
            if (progressBar != null) progressBar.setVisibility(View.GONE);

            if (apiResponse != null && apiResponse.isStatus()) {
                List<Product> products = apiResponse.getData();

                // Cập nhật Adapter
                bookGridAdapter.setProducts(products != null ? products : new ArrayList<>());

                // Cập nhật UI
                checkEmptyState();
            } else {
                // Xử lý lỗi (ví dụ: mất mạng, hết phiên)
                String msg = (apiResponse != null) ? apiResponse.getMessage() : "Lỗi tải dữ liệu";
                if (getContext() != null) {
                    // Có thể show toast hoặc log tùy ý
                }
                // Nếu lỗi thì coi như danh sách rỗng
                bookGridAdapter.setProducts(new ArrayList<>());
                checkEmptyState();
            }
        });
    }

    private void checkEmptyState() {
        int count = bookGridAdapter.getItemCount();
        updateTotalCount(count);

        if (count == 0) {
            if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
            rvProducts.setVisibility(View.GONE);
        } else {
            if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
            rvProducts.setVisibility(View.VISIBLE);
        }
    }

    private void updateTotalCount(int count) {
        if (tvTitle != null) {
            tvTitle.setText(count + " Sản phẩm");
        }
    }

    // --- Logic Giỏ hàng (Giữ nguyên) ---

    private void addToCartLogic(Product product) {
        if (product == null) return;
        cartViewModel.increaseQuantity(product.getId()).observe(getViewLifecycleOwner(), apiResponse -> {
            if (apiResponse != null && apiResponse.isStatus()) {
                cartViewModel.refreshCart();
                FancyToast.makeText(getContext(), "Đã thêm vào giỏ hàng", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();
            } else {
                String msg = (apiResponse != null) ? apiResponse.getMessage() : "Lỗi kết nối";
                FancyToast.makeText(getContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
            }
        });
    }

    private void handleBuyNow(Product product) {
        if (product == null) return;
        cartViewModel.increaseQuantity(product.getId()).observe(getViewLifecycleOwner(), apiResponse -> {
            if (apiResponse != null && apiResponse.isStatus()) {
                cartViewModel.refreshCart();
                Intent intent = new Intent(getContext(), DetailActivity.class);
                intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Giỏ hàng");
                intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "cart");
                startActivity(intent);
            } else {
                String msg = (apiResponse != null) ? apiResponse.getMessage() : "Lỗi kết nối server";
                FancyToast.makeText(getContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
            }
        });
    }
}