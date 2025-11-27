package com.example.fa25_duan1.view.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.BannerAdapter;
import com.example.fa25_duan1.adapter.BookGridAdapter;
import com.example.fa25_duan1.adapter.BookHorizontalAdapter;
import com.example.fa25_duan1.adapter.CategoryAdapter;
import com.example.fa25_duan1.adapter.RankingBookAdapter;
import com.example.fa25_duan1.model.Banner;
import com.example.fa25_duan1.model.Category;
import com.example.fa25_duan1.model.Product;
import com.example.fa25_duan1.view.detail.ProductDetailActivity;
import com.example.fa25_duan1.viewmodel.CartViewModel;
import com.example.fa25_duan1.viewmodel.CategoryViewModel;
import com.example.fa25_duan1.viewmodel.FavoriteViewModel;
import com.example.fa25_duan1.viewmodel.ProductViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPagerBanner;
    private LinearLayout layoutIndicators;
    private RecyclerView rvBooksGrid, rvCategories, rvBooksHorizontal, rvRankingCategories, rvRankingBooks;
    private View btnSeeMore;

    private BookHorizontalAdapter bookHorizontalAdapter;
    private RankingBookAdapter rankingBookAdapter;
    private BookGridAdapter bookGridAdapter;
    private CartViewModel cartViewModel;
    private ProductViewModel productViewModel;
    private CategoryViewModel categoryViewModel;
    private FavoriteViewModel favoriteViewModel;

    private LiveData<List<Product>> currentTopBooksLiveData;
    private Observer<List<Product>> currentTopBooksObserver;
    private LiveData<List<Product>> currentRankingBooksLiveData;
    private Observer<List<Product>> currentRankingBooksObserver;

    // --- BIẾN TOÀN CỤC QUAN TRỌNG ---
    // 1. Biến lưu ID đang chọn
    private String currentSelectedCategoryId = null;
    // 2. Biến lưu danh sách Category dự phòng
    private List<Category> mCategoryList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupBanner();
        setupLayoutManagers();

        // Khởi tạo ViewModel
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);
        favoriteViewModel = new ViewModelProvider(this).get(FavoriteViewModel.class);
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        setupAdapters();
        setupDataObservation();

        // --- SỬA LỖI NÚT XEM THÊM (LOGIC 3 TẦNG) ---
        if (btnSeeMore != null) {
            btnSeeMore.setOnClickListener(v -> {
                ProductFragment productFragment = new ProductFragment();
                Bundle args = new Bundle();

                // Tầng 1: Lấy từ biến đang chọn (nếu user đã click hoặc logic setup đã chạy)
                String idToSend = currentSelectedCategoryId;

                // Tầng 2: Nếu Tầng 1 null, lấy phần tử đầu tiên từ list dự phòng
                if (idToSend == null && !mCategoryList.isEmpty()) {
                    idToSend = mCategoryList.get(0).get_id();
                }

                // Tầng 3: Nếu list dự phòng rỗng (rất hiếm), chọc thẳng vào ViewModel lấy mới nhất
                if (idToSend == null) {
                    List<Category> liveDataValue = categoryViewModel.getDisplayedCategories().getValue();
                    if (liveDataValue != null && !liveDataValue.isEmpty()) {
                        idToSend = liveDataValue.get(0).get_id();
                    }
                }

                // Đóng gói và gửi đi nếu tìm thấy ID
                if (idToSend != null) {
                    args.putString("category_id", idToSend);
                    productFragment.setArguments(args);

                    if (getActivity() instanceof HomeActivity) {
                        ((HomeActivity) getActivity()).loadFragment(productFragment, true);
                    }
                } else {
                    // Trường hợp không có mạng hoặc không có danh mục nào
                    Toast.makeText(getContext(), "Đang tải dữ liệu danh mục...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        return view;
    }

    private void initViews(View view) {
        viewPagerBanner = view.findViewById(R.id.viewPagerBanner);
        layoutIndicators = view.findViewById(R.id.layoutIndicators);
        rvBooksGrid = view.findViewById(R.id.rv_books_grid);
        rvCategories = view.findViewById(R.id.rv_categories);
        rvBooksHorizontal = view.findViewById(R.id.rv_books_horizontal);
        rvRankingCategories = view.findViewById(R.id.rv_ranking_categories);
        rvRankingBooks = view.findViewById(R.id.rv_ranking_books);
        btnSeeMore = view.findViewById(R.id.btn_see_more);
    }

    private void setupLayoutManagers() {
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRankingCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvBooksHorizontal.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRankingBooks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBooksGrid.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvBooksGrid.setNestedScrollingEnabled(false);
    }

    private void setupAdapters() {
        // --- Grid Adapter ---
        bookGridAdapter = new BookGridAdapter(getContext(), new ArrayList<>(), new BookGridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Product product) {
                // SỬA Ở ĐÂY: Lấy ID từ product và truyền vào
                openDetail(product.getId());
            }

            @Override
            public void onFavoriteClick(String productId) {
                favoriteViewModel.toggleFavorite(productId);
            }

            @Override
            public void onAddToCartClick(Product product) {
                addToCart(product);
            }
        });
        rvBooksGrid.setAdapter(bookGridAdapter);

        // --- Horizontal Adapter ---
        bookHorizontalAdapter = new BookHorizontalAdapter(getContext(), new ArrayList<>(), new BookHorizontalAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Product product) {
                // SỬA Ở ĐÂY
                openDetail(product.getId());
            }

            @Override
            public void onBuyClick(Product product) {
                // Xử lý mua ngay
            }

            @Override
            public void onFavoriteClick(String productId) {
                favoriteViewModel.toggleFavorite(productId);
            }

            @Override
            public void onAddToCartClick(Product product) {
                addToCart(product);
            }
        });
        rvBooksHorizontal.setAdapter(bookHorizontalAdapter);

        // --- Ranking Adapter ---
        // SỬA Ở ĐÂY: Dùng lambda expression để lấy ID truyền vào
        rankingBookAdapter = new RankingBookAdapter(getContext(), new ArrayList<>(), product -> {
            openDetail(product.getId());
        });
        rvRankingBooks.setAdapter(rankingBookAdapter);
    }

    private void addToCart(Product product) {
        if (product != null) {
            cartViewModel.increaseQuantity(product.getId());
        }
    }

    private void openDetail(String productId) {
        Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
        // Truyền thẳng ID nhận được vào Intent
        intent.putExtra("product_id", productId);
        startActivity(intent);
    }

    private void setupDataObservation() {
        // 1. Random Products
        productViewModel.getRandomProductsApi(8).observe(getViewLifecycleOwner(), products -> {
            if (products != null) bookGridAdapter.setProducts(products);
        });

        // 2. Categories
        categoryViewModel.getDisplayedCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null && !categories.isEmpty()) {
                // A. Lưu vào list dự phòng ngay lập tức
                mCategoryList.clear();
                mCategoryList.addAll(categories);

                // B. Nếu chưa có ID nào được chọn, gán mặc định là cái đầu tiên
                if (currentSelectedCategoryId == null) {
                    currentSelectedCategoryId = categories.get(0).get_id();
                }

                // C. Setup giao diện Tabs
                setupCategoryTabs(categories);
            }
        });

        // 3. Favorites
        favoriteViewModel.getFavoriteIds().observe(getViewLifecycleOwner(), ids -> {
            if (bookGridAdapter != null) bookGridAdapter.setFavoriteIds(ids);
            if (bookHorizontalAdapter != null) bookHorizontalAdapter.setFavoriteIds(ids);
        });

        cartViewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                // (Tùy chọn) Reset message trong ViewModel nếu cần, để tránh hiện lại khi xoay màn hình
                // cartViewModel.clearMessage(); // Cần viết thêm hàm này trong ViewModel nếu muốn kỹ
            }
        });
    }

    private void setupCategoryTabs(List<Category> categories) {
        List<Category> topCats = copyCategories(categories);

        if (!topCats.isEmpty()) {
            boolean found = false;
            // Duyệt để tìm xem ID nào đang được chọn để highlight
            for (Category c : topCats) {
                if (c.get_id().equals(currentSelectedCategoryId)) {
                    c.setSelected(true);
                    found = true;
                    break;
                }
            }

            // Nếu không tìm thấy (ví dụ lần đầu chạy), chọn cái đầu tiên
            if (!found) {
                topCats.get(0).setSelected(true);
                currentSelectedCategoryId = topCats.get(0).get_id();
            }

            // Load sách theo ID đang chọn
            loadTopBooksByCategory(currentSelectedCategoryId);
        }

        CategoryAdapter adapterTop = new CategoryAdapter(topCats, category -> {
            // Cập nhật ID khi người dùng bấm chọn
            currentSelectedCategoryId = category.get_id();
            loadTopBooksByCategory(category.get_id());
        });
        rvCategories.setAdapter(adapterTop);

        // --- Phần Ranking (Giữ nguyên) ---
        List<Category> rankCats = copyCategories(categories);
        if (!rankCats.isEmpty()) {
            rankCats.get(0).setSelected(true);
            loadRankingBooksByCategory(rankCats.get(0).get_id());
        }
        CategoryAdapter adapterRanking = new CategoryAdapter(rankCats, category -> {
            loadRankingBooksByCategory(category.get_id());
        });
        rvRankingCategories.setAdapter(adapterRanking);
    }

    private List<Category> copyCategories(List<Category> source) {
        List<Category> dest = new ArrayList<>();
        for (Category c : source) dest.add(new Category(c.getName(), c.getCreateAt(), c.get_id(), false));
        return dest;
    }

    private void loadTopBooksByCategory(String categoryId) {
        if (currentTopBooksLiveData != null && currentTopBooksObserver != null) currentTopBooksLiveData.removeObserver(currentTopBooksObserver);
        currentTopBooksLiveData = productViewModel.getProductsByCategory(categoryId);
        currentTopBooksObserver = products -> {
            if (products == null) products = new ArrayList<>();
            bookHorizontalAdapter.setProducts(products);
        };
        currentTopBooksLiveData.observe(getViewLifecycleOwner(), currentTopBooksObserver);
    }

    private void loadRankingBooksByCategory(String categoryId) {
        if (currentRankingBooksLiveData != null && currentRankingBooksObserver != null) currentRankingBooksLiveData.removeObserver(currentRankingBooksObserver);
        currentRankingBooksLiveData = productViewModel.getProductsByCategory(categoryId);
        currentRankingBooksObserver = products -> {
            if (products == null) products = new ArrayList<>();
            Collections.sort(products, (p1, p2) -> Integer.compare(p2.getFavorite(), p1.getFavorite()));
            int limit = Math.min(products.size(), 5);
            rankingBookAdapter.setProducts(products.subList(0, limit));
        };
        currentRankingBooksLiveData.observe(getViewLifecycleOwner(), currentRankingBooksObserver);
    }

    private void setupBanner() {
        List<Banner> mListBanners = new ArrayList<>();
        mListBanners.add(new Banner("Giấc mơ Mỹ", "Huyền Chip", R.drawable.banner));
        mListBanners.add(new Banner("Đắc Nhân Tâm", "Dale Carnegie", R.drawable.banner));
        mListBanners.add(new Banner("Nhà Giả Kim", "Paulo Coelho", R.drawable.banner));
        mListBanners.add(new Banner("Tắt Đèn", "Ngô Tất Tố", R.drawable.banner));
        BannerAdapter adapter = new BannerAdapter(mListBanners);
        viewPagerBanner.setAdapter(adapter);
        setupIndicators(mListBanners.size());
        setCurrentIndicator(0);
        viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) { super.onPageSelected(position); setCurrentIndicator(position); }
        });
    }

    private void setupIndicators(int count) {
        layoutIndicators.removeAllViews();
        ImageView[] indicators = new ImageView[count];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(8, 0, 8, 0);
        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(getContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_indicator_inactive));
            indicators[i].setLayoutParams(layoutParams);
            layoutIndicators.addView(indicators[i]);
        }
    }

    private void setCurrentIndicator(int position) {
        int childCount = layoutIndicators.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) layoutIndicators.getChildAt(i);
            imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), i == position ? R.drawable.bg_indicator_active : R.drawable.bg_indicator_inactive));
        }
    }
}