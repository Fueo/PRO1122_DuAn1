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
import com.example.fa25_duan1.model.Book;
import com.example.fa25_duan1.model.Category;
import com.example.fa25_duan1.model.Product;
import com.example.fa25_duan1.view.detail.ProductDetailActivity;
import com.example.fa25_duan1.view.detail.ProductFragment;
import com.example.fa25_duan1.viewmodel.CategoryViewModel;
import com.example.fa25_duan1.viewmodel.ProductViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPagerBanner;
    private LinearLayout layoutIndicators;
    private RecyclerView rvBooksGrid, rvCategories, rvBooksHorizontal, rvRankingCategories, rvRankingBooks;
    private List<Banner> mListBanners;
    private View btnSeeMore;

    // Adapter
    private BookHorizontalAdapter bookHorizontalAdapter;
    private RankingBookAdapter rankingBookAdapter;
    private BookGridAdapter bookGridAdapter;

    // ViewModel
    private ProductViewModel productViewModel;
    private CategoryViewModel categoryViewModel;

    // Biến lưu trữ dữ liệu thô từ API
    private List<Product> mAllProducts = new ArrayList<>();
    private List<Category> mAllCategories = new ArrayList<>(); // Mới thêm

    private Category mCurrentSelectedCategory = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupBanner();

        // Setup Layout Manager
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRankingCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvBooksHorizontal.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        setupBookGrid();
        setupHorizontalBooks();

        // Setup ViewModel và quan sát dữ liệu
        setupViewModel();

        setupRankingBooks();

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

    private void setupViewModel() {
        // --- PRODUCT VIEW MODEL ---
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        productViewModel.getDisplayedProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                // 1. Lưu trữ toàn bộ sản phẩm
                mAllProducts = new ArrayList<>(products);

                // 2. Xử lý GridView Random
                List<Product> randomList = new ArrayList<>(products);
                Collections.shuffle(randomList);
                int limit = Math.min(randomList.size(), 8);
                if (bookGridAdapter != null) {
                    bookGridAdapter.setProducts(randomList.subList(0, limit));
                }

                // 3. CẬP NHẬT LẠI DANH SÁCH DANH MỤC (để ẩn danh mục rỗng)
                // Vì sản phẩm đã load xong, ta cần check lại xem danh mục nào có sản phẩm
                filterAndShowCategories();
            }
        });

        // --- CATEGORY VIEW MODEL ---
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        categoryViewModel.getDisplayedCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                // 1. Lưu trữ toàn bộ danh mục
                mAllCategories = new ArrayList<>(categories);

                // 2. Lọc và hiển thị danh mục
                filterAndShowCategories();
            }
        });
    }

    /**
     * Hàm lọc danh mục: Chỉ hiển thị danh mục nào CÓ sản phẩm
     */
    private void filterAndShowCategories() {
        // Chỉ chạy khi cả 2 list đều đã có dữ liệu
        if (mAllCategories.isEmpty() || mAllProducts.isEmpty()) {
            return;
        }

        // 1. Tìm tất cả các CategoryID có trong danh sách Product
        Set<String> activeCategoryIds = new HashSet<>();
        for (Product p : mAllProducts) {
            if (p.getCategory() != null && p.getCategory().get_id() != null) {
                activeCategoryIds.add(p.getCategory().get_id());
            }
        }

        // 2. Lọc danh sách Category gốc
        List<Category> visibleCategories = new ArrayList<>();
        for (Category cat : mAllCategories) {
            // Nếu ID danh mục tồn tại trong tập hợp các ID có sản phẩm -> Thêm vào list hiển thị
            if (activeCategoryIds.contains(cat.get_id())) {
                visibleCategories.add(cat);
            }
        }

        // 3. Gọi hàm setup hiển thị với danh sách đã lọc
        // Kiểm tra để tránh reset adapter liên tục nếu dữ liệu không đổi (tùy chọn)
        setupCategories(visibleCategories);
    }

    private void setupCategories(List<Category> categories) {
        // Nếu sau khi lọc mà không còn danh mục nào thì return
        if (categories.isEmpty()) return;

        List<Category> topCategories = new ArrayList<>(categories);

        // Chọn mặc định item đầu tiên
        // (Logic: Nếu chưa chọn cái nào thì chọn cái đầu, nếu đang chọn cái cũ mà cái cũ vẫn còn trong list mới thì giữ nguyên)
        if (mCurrentSelectedCategory == null || !isCategoryInList(mCurrentSelectedCategory, topCategories)) {
            topCategories.get(0).setSelected(true);
            mCurrentSelectedCategory = topCategories.get(0);
        } else {
            // Tìm lại object trong list mới để set selected (vì list mới là object mới)
            for(Category c : topCategories) {
                if(c.get_id().equals(mCurrentSelectedCategory.get_id())) {
                    c.setSelected(true);
                    break;
                }
            }
        }

        // Gọi lọc sách cho danh mục đang chọn
        filterBooksByCategory(mCurrentSelectedCategory.get_id());

        // --- Adapter Top Categories ---
        CategoryAdapter adapterTop = new CategoryAdapter(topCategories, category -> {
            mCurrentSelectedCategory = category;
            filterBooksByCategory(category.get_id());
        });
        rvCategories.setAdapter(adapterTop);


        // --- Adapter Ranking (Giữ nguyên logic) ---
        List<Category> rankingCategories = new ArrayList<>();
        for (Category c : categories) rankingCategories.add(new Category(c.getName(), false));
        if (!rankingCategories.isEmpty()) rankingCategories.get(0).setSelected(true);

        CategoryAdapter adapterRanking = new CategoryAdapter(rankingCategories, category -> {
            // Logic ranking
        });
        rvRankingCategories.setAdapter(adapterRanking);
    }

    // Helper check tồn tại
    private boolean isCategoryInList(Category target, List<Category> list) {
        for (Category c : list) {
            if (c.get_id().equals(target.get_id())) return true;
        }
        return false;
    }

    private void filterBooksByCategory(String categoryId) {
        if (mAllProducts == null || mAllProducts.isEmpty()) return;

        List<Product> filteredList = new ArrayList<>();
        for (Product p : mAllProducts) {
            if (p.getCategory() != null && p.getCategory().get_id() != null) {
                if (p.getCategory().get_id().equals(categoryId)) {
                    filteredList.add(p);
                }
            }
        }

        if (bookHorizontalAdapter != null) {
            bookHorizontalAdapter.setProducts(filteredList);
        }
    }

    // ... (Giữ nguyên các hàm setup khác: setupBookGrid, setupHorizontalBooks, setupBanner...)
    private void setupHorizontalBooks() {
        bookHorizontalAdapter = new BookHorizontalAdapter(getContext(), new ArrayList<>(), product -> {
            Toast.makeText(requireActivity(), "Click ngang: " + product.getName(), Toast.LENGTH_SHORT).show();
        });
        rvBooksHorizontal.setAdapter(bookHorizontalAdapter);
    }

    private void setupBookGrid() {
        bookGridAdapter = new BookGridAdapter(getContext(), new ArrayList<>(), product -> {
            requireActivity().startActivity(new Intent(requireActivity(), ProductDetailActivity.class));
        });
        rvBooksGrid.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvBooksGrid.setNestedScrollingEnabled(false);
        rvBooksGrid.setAdapter(bookGridAdapter);
    }

    private void setupBanner() {
        mListBanners = new ArrayList<>();
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
            if (i == position) {
                imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_indicator_active));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_indicator_inactive));
            }
        }
    }

    private void setupRankingBooks() {
        List<Book> books = new ArrayList<>();
        books.add(new Book("Mắt Biếc", "Nguyễn Nhật Ánh", R.drawable.book_cover_placeholder, 80000, 100000, "-20%", 5000, 2300));
        rankingBookAdapter = new RankingBookAdapter(books);
        rvRankingBooks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRankingBooks.setAdapter(rankingBookAdapter);
    }
}