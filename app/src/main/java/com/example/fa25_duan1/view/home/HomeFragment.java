package com.example.fa25_duan1.view.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.example.fa25_duan1.model.Discount;
import com.example.fa25_duan1.model.Product;
import com.example.fa25_duan1.view.detail.ProductDetailActivity;
import com.example.fa25_duan1.viewmodel.CartViewModel;
import com.example.fa25_duan1.viewmodel.CategoryViewModel;
import com.example.fa25_duan1.viewmodel.DiscountViewModel;
import com.example.fa25_duan1.viewmodel.FavoriteViewModel;
import com.example.fa25_duan1.viewmodel.ProductViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class HomeFragment extends Fragment {

    // --- Views ---
    private ViewPager2 viewPagerBanner;
    private LinearLayout layoutIndicators;

    // 1. SỬA: Layout chứa toàn bộ phần Flash Sale
    private LinearLayout layoutFlashSaleContainer;

    // RecyclerViews
    private RecyclerView rvSaleBooks;
    private RecyclerView rvBooksGrid, rvCategories, rvBooksHorizontal, rvRankingCategories, rvRankingBooks;

    // Timer Views
    private TextView tvTimerDay, tvTimerHour, tvTimerMinute, tvTimerSecond;
    private CountDownTimer countDownTimer;

    // --- CÁC NÚT XEM THÊM ---
    private View btnSeeMore;
    private View btnSeeMoreSale;

    // --- Adapters ---
    private BookGridAdapter saleBookAdapter;
    private BookHorizontalAdapter bookHorizontalAdapter;
    private RankingBookAdapter rankingBookAdapter;
    private BookGridAdapter bookGridAdapter;

    // --- ViewModels ---
    private CartViewModel cartViewModel;
    private ProductViewModel productViewModel;
    private CategoryViewModel categoryViewModel;
    private FavoriteViewModel favoriteViewModel;
    private DiscountViewModel discountViewModel;

    // --- Data Observers ---
    private LiveData<List<Product>> currentTopBooksLiveData;
    private Observer<List<Product>> currentTopBooksObserver;
    private LiveData<List<Product>> currentRankingBooksLiveData;
    private Observer<List<Product>> currentRankingBooksObserver;

    // --- State Variables ---
    private String currentSelectedCategoryId = null;
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
        discountViewModel = new ViewModelProvider(this).get(DiscountViewModel.class);

        setupAdapters();
        setupDataObservation();
        setupClickEvents();

        return view;
    }

    private void initViews(View view) {
        viewPagerBanner = view.findViewById(R.id.viewPagerBanner);
        layoutIndicators = view.findViewById(R.id.layoutIndicators);

        // 2. SỬA: Ánh xạ container tổng của Flash Sale (Nhớ thêm ID này vào XML như hướng dẫn)
        layoutFlashSaleContainer = view.findViewById(R.id.layout_flash_sale_container);

        tvTimerDay = view.findViewById(R.id.tv_timer_day);
        tvTimerHour = view.findViewById(R.id.tv_timer_hour);
        tvTimerMinute = view.findViewById(R.id.tv_timer_minute);
        tvTimerSecond = view.findViewById(R.id.tv_timer_second);

        rvSaleBooks = view.findViewById(R.id.rv_sale_books);
        rvBooksGrid = view.findViewById(R.id.rv_books_grid);
        rvCategories = view.findViewById(R.id.rv_categories);
        rvBooksHorizontal = view.findViewById(R.id.rv_books_horizontal);
        rvRankingCategories = view.findViewById(R.id.rv_ranking_categories);
        rvRankingBooks = view.findViewById(R.id.rv_ranking_books);

        btnSeeMore = view.findViewById(R.id.btn_see_more);
        btnSeeMoreSale = view.findViewById(R.id.btn_see_more_sale);
    }

    private void setupClickEvents() {
        if (btnSeeMore != null) {
            btnSeeMore.setOnClickListener(v -> handleSeeMoreClick());
        }

        if (btnSeeMoreSale != null) {
            btnSeeMoreSale.setOnClickListener(v -> {
                ProductFragment productFragment = new ProductFragment();
                Bundle args = new Bundle();
                args.putString("category_id", "SALE");
                productFragment.setArguments(args);
                if (getActivity() instanceof HomeActivity) {
                    ((HomeActivity) getActivity()).loadFragment(productFragment, true);
                }
            });
        }
    }

    private void setupLayoutManagers() {
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRankingCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvBooksHorizontal.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRankingBooks.setLayoutManager(new LinearLayoutManager(getContext()));

        rvBooksGrid.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvBooksGrid.setNestedScrollingEnabled(false);

        rvSaleBooks.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvSaleBooks.setNestedScrollingEnabled(false);
    }

    private void setupAdapters() {
        saleBookAdapter = new BookGridAdapter(getContext(), new ArrayList<>(), new BookGridAdapter.OnItemClickListener() {
            @Override public void onItemClick(Product product) { openDetail(product.getId()); }
            @Override public void onFavoriteClick(String productId) { favoriteViewModel.toggleFavorite(productId); }
            @Override public void onAddToCartClick(Product product) { addToCart(product); }
        });
        rvSaleBooks.setAdapter(saleBookAdapter);

        bookGridAdapter = new BookGridAdapter(getContext(), new ArrayList<>(), new BookGridAdapter.OnItemClickListener() {
            @Override public void onItemClick(Product product) { openDetail(product.getId()); }
            @Override public void onFavoriteClick(String productId) { favoriteViewModel.toggleFavorite(productId); }
            @Override public void onAddToCartClick(Product product) { addToCart(product); }
        });
        rvBooksGrid.setAdapter(bookGridAdapter);

        bookHorizontalAdapter = new BookHorizontalAdapter(getContext(), new ArrayList<>(), new BookHorizontalAdapter.OnItemClickListener() {
            @Override public void onItemClick(Product product) { openDetail(product.getId()); }
            @Override public void onBuyClick(Product product) { /* Logic mua ngay */ }
            @Override public void onFavoriteClick(String productId) { favoriteViewModel.toggleFavorite(productId); }
            @Override public void onAddToCartClick(Product product) { addToCart(product); }
        });
        rvBooksHorizontal.setAdapter(bookHorizontalAdapter);

        rankingBookAdapter = new RankingBookAdapter(getContext(), new ArrayList<>(), product -> openDetail(product.getId()));
        rvRankingBooks.setAdapter(rankingBookAdapter);
    }

    private void startCountdownTimer(long endTimeInMillis) {
        if (countDownTimer != null) countDownTimer.cancel();

        long currentTime = System.currentTimeMillis();
        long duration = endTimeInMillis - currentTime;

        if (duration <= 0) {
            handleFlashSaleVisibility(false);
            return;
        }

        countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long days = millisUntilFinished / (24 * 60 * 60 * 1000);
                long hours = (millisUntilFinished / (60 * 60 * 1000)) % 24;
                long minutes = ((millisUntilFinished / 1000) % 3600) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;

                if (tvTimerDay != null) tvTimerDay.setText(String.format("%02d", days));
                if (tvTimerHour != null) tvTimerHour.setText(String.format("%02d", hours));
                if (tvTimerMinute != null) tvTimerMinute.setText(String.format("%02d", minutes));
                if (tvTimerSecond != null) tvTimerSecond.setText(String.format("%02d", seconds));
            }

            @Override
            public void onFinish() {
                handleFlashSaleVisibility(false);
            }
        }.start();
    }

    // 3. SỬA: Hàm này giờ đây sẽ ẩn/hiện toàn bộ container
    private void handleFlashSaleVisibility(boolean isVisible) {
        if (layoutFlashSaleContainer != null) {
            layoutFlashSaleContainer.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    private void setupDataObservation() {
        // --- LOGIC FLASH SALE ---
        discountViewModel.getDisplayedDiscounts().observe(getViewLifecycleOwner(), discounts -> {
            if (discounts == null || discounts.isEmpty()) {
                handleFlashSaleVisibility(false);
                return;
            }

            long now = System.currentTimeMillis();
            Discount bestDiscount = null;

            for (Discount d : discounts) {
                long start = parseDateToMillis(d.getStartDate());
                long end = parseDateToMillis(d.getEndDate());
                if (now >= start && now <= end) {
                    if (bestDiscount == null || d.getDiscountRate() > bestDiscount.getDiscountRate()) {
                        bestDiscount = d;
                    }
                }
            }

            if (bestDiscount != null) {
                handleFlashSaleVisibility(true);
                long endTime = parseDateToMillis(bestDiscount.getEndDate());
                startCountdownTimer(endTime);
                fetchOnSaleProducts(6);
            } else {
                handleFlashSaleVisibility(false);
                if (countDownTimer != null) countDownTimer.cancel();
            }
        });

        productViewModel.getRandomProductsApi(8).observe(getViewLifecycleOwner(), products -> {
            if (products != null) bookGridAdapter.setProducts(products);
        });

        categoryViewModel.getDisplayedCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null && !categories.isEmpty()) {
                mCategoryList.clear();
                mCategoryList.addAll(categories);
                if (currentSelectedCategoryId == null) currentSelectedCategoryId = categories.get(0).get_id();
                setupCategoryTabs(categories);
            }
        });

        favoriteViewModel.getFavoriteIds().observe(getViewLifecycleOwner(), ids -> {
            if (bookGridAdapter != null) bookGridAdapter.setFavoriteIds(ids);
            if (saleBookAdapter != null) saleBookAdapter.setFavoriteIds(ids);
            if (bookHorizontalAdapter != null) bookHorizontalAdapter.setFavoriteIds(ids);
        });

        cartViewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchOnSaleProducts(int limit) {
        productViewModel.getOnSaleProductsApi(limit).observe(getViewLifecycleOwner(), products -> {
            if (products != null && !products.isEmpty()) {
                saleBookAdapter.setProducts(products);
            } else {
                // Nếu có chương trình giảm giá nhưng lại không lấy được sách nào
                // thì cũng ẩn luôn cho đỡ trống
                handleFlashSaleVisibility(false);
            }
        });
    }

    private long parseDateToMillis(Object dateObj) {
        if (dateObj instanceof Date) return ((Date) dateObj).getTime();
        if (dateObj instanceof String) {
            String dateStr = (String) dateObj;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                Date date = sdf.parse(dateStr);
                return date != null ? date.getTime() : 0;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private void handleSeeMoreClick() {
        ProductFragment productFragment = new ProductFragment();
        Bundle args = new Bundle();
        String idToSend = currentSelectedCategoryId;
        if (idToSend == null && !mCategoryList.isEmpty()) idToSend = mCategoryList.get(0).get_id();

        if (idToSend != null) {
            args.putString("category_id", idToSend);
            productFragment.setArguments(args);
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).loadFragment(productFragment, true);
            }
        } else {
            Toast.makeText(getContext(), "Đang tải dữ liệu danh mục...", Toast.LENGTH_SHORT).show();
        }
    }

    private void addToCart(Product product) {
        if (product != null) cartViewModel.increaseQuantity(product.getId());
    }

    private void openDetail(String productId) {
        Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
        intent.putExtra("product_id", productId);
        startActivity(intent);
    }

    private void setupCategoryTabs(List<Category> categories) {
        List<Category> topCats = copyCategories(categories);
        if (!topCats.isEmpty()) {
            boolean found = false;
            for (Category c : topCats) {
                if (c.get_id().equals(currentSelectedCategoryId)) {
                    c.setSelected(true);
                    found = true;
                    break;
                }
            }
            if (!found) {
                topCats.get(0).setSelected(true);
                currentSelectedCategoryId = topCats.get(0).get_id();
            }
            loadTopBooksByCategory(currentSelectedCategoryId);
        }
        CategoryAdapter adapterTop = new CategoryAdapter(topCats, category -> {
            currentSelectedCategoryId = category.get_id();
            loadTopBooksByCategory(category.get_id());
        });
        rvCategories.setAdapter(adapterTop);

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
        BannerAdapter adapter = new BannerAdapter(mListBanners);
        viewPagerBanner.setAdapter(adapter);
        setupIndicators(mListBanners.size());
        setCurrentIndicator(0);
        viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) { super.onPageSelected(position); setCurrentIndicator(position); }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}