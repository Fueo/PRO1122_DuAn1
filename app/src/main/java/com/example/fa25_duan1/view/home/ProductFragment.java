package com.example.fa25_duan1.view.home;

import android.content.Intent;
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
import com.example.fa25_duan1.view.detail.ProductDetailActivity;
import com.example.fa25_duan1.viewmodel.CartViewModel;
import com.example.fa25_duan1.viewmodel.FavoriteViewModel;
import com.example.fa25_duan1.viewmodel.ProductViewModel;
import com.shashank.sony.fancytoastlib.FancyToast; // Import thêm FancyToast

import java.util.ArrayList;
import java.util.List;

public class ProductFragment extends Fragment {

    private RecyclerView rvProducts;
    private View layoutEmpty;
    private View fragmentFilterContainer;
    private BookGridAdapter bookGridAdapter;

    private ProductViewModel productViewModel;
    private FavoriteViewModel favoriteViewModel;
    private CartViewModel cartViewModel;
    public static final String ID_CATEGORY_SALE = "CATEGORY_SALE_ID";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        // Dùng requireActivity() để share ViewModel
        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        favoriteViewModel = new ViewModelProvider(this).get(FavoriteViewModel.class);
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        setupProductGrid();
        handleArguments(savedInstanceState); // Xử lý logic Search ở đây
        setupDataObservation();
    }

    private void initViews(View view) {
        rvProducts = view.findViewById(R.id.rv_products);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        fragmentFilterContainer = view.findViewById(R.id.fragment_filter);
    }

    private void handleArguments(Bundle savedInstanceState) {
        Bundle args = getArguments();

        // --- TRƯỜNG HỢP 1: TÌM KIẾM (Search) ---
        if (args != null && args.containsKey("search_query")) {
            String searchQuery = args.getString("search_query", "").trim();

            if (!searchQuery.isEmpty()) {
                // Ẩn bộ lọc khi đang search
                if (fragmentFilterContainer != null) {
                    fragmentFilterContainer.setVisibility(View.GONE);
                }
                // GỌI API SEARCH
                productViewModel.searchProductsByNameApi(searchQuery);
            }
        }

        // --- TRƯỜNG HỢP 2: CÓ ID DANH MỤC (Filter Category) ---
        else if (args != null && args.containsKey("category_id")) {
            String categoryId = args.getString("category_id");

            if (fragmentFilterContainer != null) fragmentFilterContainer.setVisibility(View.VISIBLE);

            if (savedInstanceState == null) {
                if (getChildFragmentManager().findFragmentById(R.id.fragment_filter) == null) {
                    ProductListFilterFragment filterFragment = new ProductListFilterFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("category_id", categoryId);
                    filterFragment.setArguments(bundle);

                    getChildFragmentManager().beginTransaction()
                            .replace(R.id.fragment_filter, filterFragment)
                            .commit();
                }
            }
            productViewModel.filterProductsByCategoryApi(categoryId);
        }

        // --- TRƯỜNG HỢP 3: MẶC ĐỊNH (Xem tất cả) ---
        else {
            if (fragmentFilterContainer != null) fragmentFilterContainer.setVisibility(View.VISIBLE);

            productViewModel.refreshData();

            if (savedInstanceState == null) {
                if (getChildFragmentManager().findFragmentById(R.id.fragment_filter) == null) {
                    getChildFragmentManager().beginTransaction()
                            .replace(R.id.fragment_filter, new ProductListFilterFragment())
                            .commit();
                }
            }
        }
    }

    private void setupProductGrid() {
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
            }

            @Override
            public void onAddToCartClick(Product product) {
                // SỬA: Gọi hàm logic mới
                addToCartLogic(product);
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvProducts.setLayoutManager(gridLayoutManager);
        rvProducts.setAdapter(bookGridAdapter);
    }

    private void setupDataObservation() {
        // --- SỬA ĐOẠN NÀY ---
        productViewModel.getDisplayedProducts().observe(getViewLifecycleOwner(), products -> {
            // Tạo list mới để chứa sản phẩm đang kinh doanh
            List<Product> activeProducts = new ArrayList<>();

            if (products != null) {
                for (Product p : products) {
                    // Chỉ thêm vào nếu status = true
                    if (p.isStatus()) {
                        activeProducts.add(p);
                    }
                }
            }

            // Hiển thị list đã lọc (activeProducts) thay vì list gốc (products)
            checkDataAndShow(activeProducts);
        });
        // --------------------

        favoriteViewModel.getFavoriteIds().observe(getViewLifecycleOwner(), ids -> {
            if (bookGridAdapter != null) {
                bookGridAdapter.setFavoriteIds(ids);
            }
        });
    }

    // --- LOGIC THÊM GIỎ HÀNG MỚI ---
    private void addToCartLogic(Product product) {
        if (product == null) return;

        // Gọi ViewModel và lắng nghe kết quả trực tiếp
        cartViewModel.increaseQuantity(product.getId()).observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isStatus()) {
                // 1. Thành công: Refresh lại giỏ hàng (để Badge cập nhật)
                cartViewModel.refreshCart();

                // 2. Hiện thông báo đẹp
                FancyToast.makeText(getContext(), "Đã thêm vào giỏ hàng", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();
            } else {
                // 3. Thất bại
                String msg = (response != null) ? response.getMessage() : "Lỗi kết nối";
                FancyToast.makeText(getContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
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