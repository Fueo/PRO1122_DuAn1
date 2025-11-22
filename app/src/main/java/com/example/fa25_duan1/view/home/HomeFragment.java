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
import com.example.fa25_duan1.viewmodel.CategoryViewModel;
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

    private ProductViewModel productViewModel;
    private CategoryViewModel categoryViewModel;

    // Biến lưu observer hiện tại để tránh memory leak khi click nhiều lần
    private LiveData<List<Product>> currentTopBooksLiveData;
    private Observer<List<Product>> currentTopBooksObserver;

    private LiveData<List<Product>> currentRankingBooksLiveData;
    private Observer<List<Product>> currentRankingBooksObserver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupBanner();

        // Setup RecyclerView layouts
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRankingCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvBooksHorizontal.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRankingBooks.setLayoutManager(new LinearLayoutManager(getContext()));

        rvBooksGrid.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvBooksGrid.setNestedScrollingEnabled(false);

        // Khởi tạo Adapter rỗng trước
        setupAdapters();

        setupViewModel();

        if (btnSeeMore != null) {
            btnSeeMore.setOnClickListener(v -> {
                if (getActivity() instanceof HomeActivity) {
                    ((HomeActivity) getActivity()).loadFragment(new ProductFragment(), true);
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

    private void setupAdapters() {
        // 1. Adapter Grid (Sách gợi ý/Random)
        bookGridAdapter = new BookGridAdapter(getContext(), new ArrayList<>(), product -> openDetail(product));
        rvBooksGrid.setAdapter(bookGridAdapter);

        // 2. Adapter Horizontal (Sách mới theo danh mục)
        bookHorizontalAdapter = new BookHorizontalAdapter(getContext(), new ArrayList<>(), new BookHorizontalAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Product product) { openDetail(product); }
            @Override
            public void onBuyClick(Product product) {
                Toast.makeText(requireContext(), "Đã thêm " + product.getName() + " vào giỏ!", Toast.LENGTH_SHORT).show();
            }
        });
        rvBooksHorizontal.setAdapter(bookHorizontalAdapter);

        // 3. Adapter Ranking (BXH theo danh mục)
        rankingBookAdapter = new RankingBookAdapter(getContext(), new ArrayList<>(), product -> openDetail(product));
        rvRankingBooks.setAdapter(rankingBookAdapter);
    }

    private void openDetail(Product product) {
        Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }

    private void setupViewModel() {
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        // --- PHẦN 1: LOAD SÁCH RANDOM (GRID VIEW) ---
        // Gọi API lấy 8 sách ngẫu nhiên từ server
        productViewModel.getRandomProductsApi(8).observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                bookGridAdapter.setProducts(products);
            }
        });

        // --- PHẦN 2: LOAD DANH MỤC & SETUP TABS ---
        categoryViewModel.getDisplayedCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null && !categories.isEmpty()) {
                setupCategoryTabs(categories);
            }
        });
    }

    // Thiết lập Tabs cho cả phần Sách Mới (Top) và BXH (Ranking)
    private void setupCategoryTabs(List<Category> categories) {
        // --- A. Tabs cho Sách Mới (Horizontal) ---
        List<Category> topCats = copyCategories(categories);
        // Mặc định chọn tab đầu tiên
        if (!topCats.isEmpty()) {
            topCats.get(0).setSelected(true);
            loadTopBooksByCategory(topCats.get(0).get_id()); // Load dữ liệu ngay
        }

        CategoryAdapter adapterTop = new CategoryAdapter(topCats, category -> {
            // Khi click tab
            loadTopBooksByCategory(category.get_id());
        });
        rvCategories.setAdapter(adapterTop);

        // --- B. Tabs cho BXH (Ranking) ---
        List<Category> rankCats = copyCategories(categories);
        // Mặc định chọn tab đầu tiên
        if (!rankCats.isEmpty()) {
            rankCats.get(0).setSelected(true);
            loadRankingBooksByCategory(rankCats.get(0).get_id()); // Load dữ liệu ngay
        }

        CategoryAdapter adapterRanking = new CategoryAdapter(rankCats, category -> {
            // Khi click tab
            loadRankingBooksByCategory(category.get_id());
        });
        rvRankingCategories.setAdapter(adapterRanking);
    }

    // Helper copy list để tránh thay đổi trạng thái selected lẫn lộn giữa 2 recyclerview
    private List<Category> copyCategories(List<Category> source) {
        List<Category> dest = new ArrayList<>();
        for (Category c : source) {
            dest.add(new Category(c.getName(), c.getCreateAt(), c.get_id(), false));
        }
        return dest;
    }

    // --- LOGIC GỌI API KHI CHỌN TAB ---

    // 1. Load Sách theo Category (Horizontal List)
    private void loadTopBooksByCategory(String categoryId) {
        // Xóa observer cũ nếu có (để tránh chồng chéo dữ liệu khi click nhanh)
        if (currentTopBooksLiveData != null && currentTopBooksObserver != null) {
            currentTopBooksLiveData.removeObserver(currentTopBooksObserver);
        }

        currentTopBooksLiveData = productViewModel.getProductsByCategory(categoryId);
        currentTopBooksObserver = products -> {
            if (products == null) products = new ArrayList<>();
            bookHorizontalAdapter.setProducts(products);
        };

        // Observe LiveData mới từ API
        currentTopBooksLiveData.observe(getViewLifecycleOwner(), currentTopBooksObserver);
    }

    // 2. Load Sách theo Category cho BXH (Ranking List)
    private void loadRankingBooksByCategory(String categoryId) {
        // Xóa observer cũ
        if (currentRankingBooksLiveData != null && currentRankingBooksObserver != null) {
            currentRankingBooksLiveData.removeObserver(currentRankingBooksObserver);
        }

        currentRankingBooksLiveData = productViewModel.getProductsByCategory(categoryId);
        currentRankingBooksObserver = products -> {
            if (products == null) products = new ArrayList<>();

            // Logic sắp xếp Client-side: Lấy về list category đó -> Sort theo favorite -> Cắt top 5
            Collections.sort(products, (p1, p2) -> Integer.compare(p2.getFavorite(), p1.getFavorite()));

            int limit = Math.min(products.size(), 5);
            rankingBookAdapter.setProducts(products.subList(0, limit));
        };

        currentRankingBooksLiveData.observe(getViewLifecycleOwner(), currentRankingBooksObserver);
    }

    // --- Banner Setup (Không đổi) ---
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
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentIndicator(position);
            }
        });
    }

    private void setupIndicators(int count) {
        layoutIndicators.removeAllViews(); // Reset indicators
        ImageView[] indicators = new ImageView[count];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
            imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(),
                    i == position ? R.drawable.bg_indicator_active : R.drawable.bg_indicator_inactive));
        }
    }
}