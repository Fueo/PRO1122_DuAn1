package com.example.fa25_duan1.view.home;

import java.util.concurrent.atomic.AtomicInteger;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
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
import com.example.fa25_duan1.model.ApiResponse; // Import ApiResponse
import com.example.fa25_duan1.model.Banner;
import com.example.fa25_duan1.model.Category;
import com.example.fa25_duan1.model.Discount;
import com.example.fa25_duan1.model.Product;
import com.example.fa25_duan1.view.detail.DetailActivity;
import com.example.fa25_duan1.view.detail.ProductDetailActivity;
import com.example.fa25_duan1.viewmodel.CartViewModel;
import com.example.fa25_duan1.viewmodel.CategoryViewModel;
import com.example.fa25_duan1.viewmodel.DiscountViewModel;
import com.example.fa25_duan1.viewmodel.FavoriteViewModel;
import com.example.fa25_duan1.viewmodel.ProductViewModel;
import com.shashank.sony.fancytoastlib.FancyToast;

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
    private View btnSeeMoreRanking;

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

    // --- Data Observers (Sửa kiểu dữ liệu thành ApiResponse) ---
    private LiveData<ApiResponse<List<Product>>> currentTopBooksLiveData;
    private Observer<ApiResponse<List<Product>>> currentTopBooksObserver;

    private LiveData<ApiResponse<List<Product>>> currentRankingBooksLiveData;
    private Observer<ApiResponse<List<Product>>> currentRankingBooksObserver;

    // --- State Variables ---
    private String currentSelectedCategoryId = null;
    private String currentRankingCategoryId = null;
    private List<Category> mCategoryList = new ArrayList<>();

    // --- Banner Auto-scroll Variables ---
    private List<Banner> mListBanners = new ArrayList<>();
    private Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable = new Runnable() {
        @Override
        public void run() {
            if (viewPagerBanner == null || mListBanners == null || mListBanners.isEmpty()) return;

            int currentItem = viewPagerBanner.getCurrentItem();
            int totalItem = mListBanners.size();

            int nextItem = currentItem + 1;
            if (nextItem >= totalItem) {
                nextItem = 0;
            }
            viewPagerBanner.setCurrentItem(nextItem);
            bannerHandler.postDelayed(this, 5000);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupBanner();
        setupLayoutManagers();

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
        btnSeeMoreRanking = view.findViewById(R.id.btn_see_more_ranking);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (favoriteViewModel != null) favoriteViewModel.refreshFavorites();
        if (cartViewModel != null) cartViewModel.refreshCart();
        bannerHandler.postDelayed(bannerRunnable, 5000);
    }

    @Override
    public void onPause() {
        super.onPause();
        bannerHandler.removeCallbacks(bannerRunnable);
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

        if (btnSeeMoreRanking != null) {
            btnSeeMoreRanking.setOnClickListener(v -> {
                ProductFragment productFragment = new ProductFragment();
                Bundle args = new Bundle();
                String idToSend = currentRankingCategoryId;
                if (idToSend == null && !mCategoryList.isEmpty()) {
                    idToSend = mCategoryList.get(0).get_id();
                }

                if (idToSend != null) {
                    args.putString("category_id", idToSend);
                    productFragment.setArguments(args);
                    if (getActivity() instanceof HomeActivity) {
                        ((HomeActivity) getActivity()).loadFragment(productFragment, true);
                    }
                } else {
                    Toast.makeText(getContext(), "Đang tải dữ liệu...", Toast.LENGTH_SHORT).show();
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
            @Override public void onBuyNowClick(Product product) { handleBuyNow(product); }
        });
        rvSaleBooks.setAdapter(saleBookAdapter);

        bookGridAdapter = new BookGridAdapter(getContext(), new ArrayList<>(), new BookGridAdapter.OnItemClickListener() {
            @Override public void onItemClick(Product product) { openDetail(product.getId()); }
            @Override public void onFavoriteClick(String productId) { favoriteViewModel.toggleFavorite(productId); }
            @Override public void onAddToCartClick(Product product) { addToCart(product); }
            @Override public void onBuyNowClick(Product product) { handleBuyNow(product); }
        });
        rvBooksGrid.setAdapter(bookGridAdapter);

        bookHorizontalAdapter = new BookHorizontalAdapter(getContext(), new ArrayList<>(), new BookHorizontalAdapter.OnItemClickListener() {
            @Override public void onItemClick(Product product) { openDetail(product.getId()); }
            @Override public void onBuyClick(Product product) { handleBuyNow(product); }
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

    private void handleFlashSaleVisibility(boolean isVisible) {
        if (layoutFlashSaleContainer != null) {
            layoutFlashSaleContainer.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    private void setupDataObservation() {
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

        // [SỬA] getRandomProductsApi trả về ApiResponse
        productViewModel.getRandomProductsApi(8).observe(getViewLifecycleOwner(), apiResponse -> {
            if (apiResponse != null && apiResponse.isStatus()) {
                bookGridAdapter.setProducts(apiResponse.getData());
            }
        });

        categoryViewModel.getDisplayedCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null && !categories.isEmpty()) {
                // Server đã lọc sẵn rồi, cứ thế mà dùng thôi!
                mCategoryList.clear();
                mCategoryList.addAll(categories);

                // Setup Tab lần đầu tiên
                if (currentSelectedCategoryId == null) {
                    currentSelectedCategoryId = categories.get(0).get_id();
                }
                if (currentRankingCategoryId == null) {
                    currentRankingCategoryId = categories.get(0).get_id();
                }

                setupCategoryTabs(categories);
            }
        });

        favoriteViewModel.getFavoriteIds().observe(getViewLifecycleOwner(), ids -> {
            if (bookGridAdapter != null) bookGridAdapter.setFavoriteIds(ids);
            if (saleBookAdapter != null) saleBookAdapter.setFavoriteIds(ids);
            if (bookHorizontalAdapter != null) bookHorizontalAdapter.setFavoriteIds(ids);
        });
    }

    private void fetchOnSaleProducts(int limit) {
        // [SỬA] getOnSaleProductsApi trả về ApiResponse
        productViewModel.getOnSaleProductsApi(limit).observe(getViewLifecycleOwner(), apiResponse -> {
            if (apiResponse != null && apiResponse.isStatus()) {
                List<Product> products = apiResponse.getData();
                if (products != null && !products.isEmpty()) {
                    saleBookAdapter.setProducts(products);
                } else {
                    handleFlashSaleVisibility(false);
                }
            } else {
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
        if (product == null) return;
        // [SỬA] Xử lý ApiResponse<CartItem>
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
        // [SỬA] Xử lý ApiResponse<CartItem>
        cartViewModel.increaseQuantity(product.getId()).observe(getViewLifecycleOwner(), apiResponse -> {
            if (apiResponse != null && apiResponse.isStatus()) {
                cartViewModel.refreshCart();
                FancyToast.makeText(getContext(), "Đã thêm vào giỏ hàng", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();
                Intent intent = new Intent(getContext(), DetailActivity.class);
                intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Giỏ hàng");
                intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "cart");
                startActivity(intent);
            } else {
                String msg = (apiResponse != null) ? apiResponse.getMessage() : "Lỗi kết nối";
                FancyToast.makeText(getContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
            }
        });
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
            currentRankingCategoryId = rankCats.get(0).get_id();
            loadRankingBooksByCategory(rankCats.get(0).get_id());
        }
        CategoryAdapter adapterRanking = new CategoryAdapter(rankCats, category -> {
            currentRankingCategoryId = category.get_id();
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
        if (currentTopBooksLiveData != null && currentTopBooksObserver != null)
            currentTopBooksLiveData.removeObserver(currentTopBooksObserver);

        currentTopBooksLiveData = productViewModel.getProductsByCategory(categoryId);

        // [SỬA] Observer ApiResponse<List<Product>>
        currentTopBooksObserver = apiResponse -> {
            List<Product> products = new ArrayList<>();
            if (apiResponse != null && apiResponse.isStatus() && apiResponse.getData() != null) {
                products = apiResponse.getData();
            }

            List<Product> activeProducts = new ArrayList<>();
            for (Product p : products) {
                if (p.isStatus()) {
                    activeProducts.add(p);
                }
            }
            bookHorizontalAdapter.setProducts(activeProducts);
        };
        currentTopBooksLiveData.observe(getViewLifecycleOwner(), currentTopBooksObserver);
    }

    private void loadRankingBooksByCategory(String categoryId) {
        if (currentRankingBooksLiveData != null && currentRankingBooksObserver != null)
            currentRankingBooksLiveData.removeObserver(currentRankingBooksObserver);

        currentRankingBooksLiveData = productViewModel.getProductsByCategory(categoryId);

        // [SỬA] Observer ApiResponse<List<Product>>
        currentRankingBooksObserver = apiResponse -> {
            List<Product> products = new ArrayList<>();
            if (apiResponse != null && apiResponse.isStatus() && apiResponse.getData() != null) {
                products = apiResponse.getData();
            }

            List<Product> activeProducts = new ArrayList<>();
            for (Product p : products) {
                if (p.isStatus()) {
                    activeProducts.add(p);
                }
            }
            Collections.sort(activeProducts, (p1, p2) -> Integer.compare(p2.getFavorite(), p1.getFavorite()));
            int limit = Math.min(activeProducts.size(), 5);
            rankingBookAdapter.setProducts(activeProducts.subList(0, limit));
        };
        currentRankingBooksLiveData.observe(getViewLifecycleOwner(), currentRankingBooksObserver);
    }

    private void setupBanner() {
        mListBanners = new ArrayList<>();
        mListBanners.add(new Banner("Giấc mơ Mỹ", "Huyền Chip", R.drawable.banner1));
        mListBanners.add(new Banner("Re:Zero − Bắt đầu lại ở thế giới khác", "Nagatsuki Tappei", R.drawable.banner2));
        mListBanners.add(new Banner("Dược sư tự sự", "Hyuga Natsu", R.drawable.banner3));
        mListBanners.add(new Banner("86 - Eighty six", "Asato Asato", R.drawable.banner4));

        BannerAdapter adapter = new BannerAdapter(mListBanners);
        viewPagerBanner.setAdapter(adapter);
        setupIndicators(mListBanners.size());
        setCurrentIndicator(0);

        viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentIndicator(position);
                bannerHandler.removeCallbacks(bannerRunnable);
                bannerHandler.postDelayed(bannerRunnable, 5000);
            }
        });

        bannerHandler.postDelayed(bannerRunnable, 5000);
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
        bannerHandler.removeCallbacks(bannerRunnable);
    }

    private void filterActiveCategories(List<Category> allCategories) {
        List<String> validCategoryIds = new ArrayList<>();
        AtomicInteger checkedCount = new AtomicInteger(0);
        int total = allCategories.size();

        for (Category cat : allCategories) {
            // [SỬA] Gọi API kiểm tra sản phẩm, trả về ApiResponse<List<Product>>
            productViewModel.getProductsByCategory(cat.get_id()).observe(getViewLifecycleOwner(), new Observer<ApiResponse<List<Product>>>() {
                @Override
                public void onChanged(ApiResponse<List<Product>> apiResponse) {
                    productViewModel.getProductsByCategory(cat.get_id()).removeObserver(this);

                    boolean hasActiveProduct = false;

                    if (apiResponse != null && apiResponse.isStatus()) {
                        List<Product> products = apiResponse.getData();
                        if (products != null && !products.isEmpty()) {
                            for (Product p : products) {
                                if (p.isStatus()) {
                                    hasActiveProduct = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (hasActiveProduct) {
                        validCategoryIds.add(cat.get_id());
                    }

                    if (checkedCount.incrementAndGet() == total) {
                        finalizeCategorySetup(allCategories, validCategoryIds);
                    }
                }
            });
        }
    }

    private void finalizeCategorySetup(List<Category> allCategories, List<String> validIds) {
        List<Category> finalCategories = new ArrayList<>();
        for (Category cat : allCategories) {
            if (validIds.contains(cat.get_id())) {
                finalCategories.add(cat);
            }
        }

        if (!finalCategories.isEmpty()) {
            mCategoryList.clear();
            mCategoryList.addAll(finalCategories);

            if (currentSelectedCategoryId == null || !validIds.contains(currentSelectedCategoryId)) {
                currentSelectedCategoryId = finalCategories.get(0).get_id();
            }
            if (currentRankingCategoryId == null || !validIds.contains(currentRankingCategoryId)) {
                currentRankingCategoryId = finalCategories.get(0).get_id();
            }

            setupCategoryTabs(finalCategories);
        }
    }
}