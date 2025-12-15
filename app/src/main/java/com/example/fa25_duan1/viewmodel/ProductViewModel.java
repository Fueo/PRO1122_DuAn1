package com.example.fa25_duan1.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fa25_duan1.model.ApiResponse; // Import ApiResponse
import com.example.fa25_duan1.model.Product;
import com.example.fa25_duan1.repository.ProductRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ProductViewModel extends AndroidViewModel {

    public static final int SORT_DATE_NEWEST = 0;
    public static final int SORT_DATE_OLDEST = 1;
    public static final int SORT_PRICE_ASC = 2;
    public static final int SORT_PRICE_DESC = 3;

    private final ProductRepository repository;

    private final MediatorLiveData<List<Product>> allProductsLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<List<Product>> displayedProductsLiveData = new MediatorLiveData<>();
    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>();

    // Biến theo dõi nguồn repo (Kiểu dữ liệu thay đổi thành ApiResponse)
    private LiveData<ApiResponse<List<Product>>> currentRepoSource;

    private int currentSortMode = SORT_DATE_NEWEST;

    // ================= FILTER STATE =================
    private boolean fsShowSelling = true;
    private boolean fsShowStopped = true;
    private boolean fsShowLowStock = false;
    private float fsMinPrice = 0f;
    private float fsMaxPrice = Float.MAX_VALUE;
    private boolean fsFilterByOriginalPrice = true;
    private List<String> fsCategoryIds = null;

    private String fsSearchQuery = null;
    private String fsSearchType = null;
// ===============================================

    public ProductViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application.getApplicationContext());
    }

    public LiveData<List<Product>> getDisplayedProducts() { return displayedProductsLiveData; }
    public LiveData<String> getMessage() { return messageLiveData; }

    // --- FETCH DATA ---

    public void refreshData() {
        switchSource(repository.getAllProducts());
    }

    public void resetToAllProducts() {
        refreshData();
    }

    // --- API WRAPPERS ---

    public LiveData<ApiResponse<Product>> viewProductApi(String id) {
        return repository.viewProduct(id);
    }

    public LiveData<ApiResponse<Integer>> getTotalProduct() {
        return repository.getTotalProduct();
    }
    public LiveData<ApiResponse<List<Product>>> getOnSaleProductsApi(int limit) {
        return repository.getOnSaleProducts(limit);
    }
    // Lọc sản phẩm giảm giá (API)
    public void filterOnSaleProductsApi() {
        switchSource(repository.getOnSaleProducts(0));
    }

    public void filterProductsByCategoryApi(String categoryId) {
        switchSource(repository.getProductsByCategory(categoryId));
    }

    public void searchProductsByNameApi(String keyword) {
        switchSource(repository.searchProductsByName(keyword));
    }

    public LiveData<ApiResponse<List<Product>>> getRandomProductsApi(int limit) {
        return repository.getRandomProducts(limit);
    }

    // --- CRUD ---

    public LiveData<ApiResponse<Product>> addProductWithImage(RequestBody name, RequestBody description, RequestBody pages, RequestBody publishDate, RequestBody status, RequestBody categoryID, RequestBody authorID, RequestBody price, RequestBody quantity, MultipartBody.Part image) {
        return repository.addProductWithImage(name, description, pages, publishDate, status, categoryID, authorID, price, quantity, image);
    }

    public LiveData<ApiResponse<Product>> updateProductWithImage(String id, RequestBody name, RequestBody description, RequestBody pages, RequestBody publishDate, RequestBody status, RequestBody categoryID, RequestBody authorID, RequestBody price, RequestBody quantity, MultipartBody.Part image) {
        return repository.updateProductWithImage(id, name, description, pages, publishDate, status, categoryID, authorID, price, quantity, image);
    }

    public LiveData<ApiResponse<Void>> deleteProduct(String id) {
        return repository.deleteProduct(id);
    }

    public LiveData<ApiResponse<Product>> getProductByID(String id) {
        return repository.getProductByID(id);
    }

    public LiveData<ApiResponse<List<Product>>> getProductsByAuthor(String authorId) {
        return repository.getProductsByAuthor(authorId);
    }

    public LiveData<ApiResponse<List<Product>>> getProductsByCategory(String categoryId) {
        return repository.getProductsByCategory(categoryId);
    }

    public LiveData<ApiResponse<List<Product>>> getFavoriteProductsApi() {
        return repository.getFavoriteProducts();
    }
    // --- HELPER: SWITCH SOURCE (XỬ LÝ API RESPONSE) ---
    private void switchSource(LiveData<ApiResponse<List<Product>>> newSource) {
        if (currentRepoSource != null) {
            displayedProductsLiveData.removeSource(currentRepoSource);
        }
        currentRepoSource = newSource;

        displayedProductsLiveData.addSource(currentRepoSource, apiResponse -> {
            List<Product> products = new ArrayList<>();

            if (apiResponse != null) {
                if (apiResponse.isStatus()) {
                    // Thành công
                    if (apiResponse.getData() != null) {
                        products = apiResponse.getData();
                    }
                } else {
                    // Thất bại: Báo lỗi
                    messageLiveData.setValue(apiResponse.getMessage());
                }
            } else {
                messageLiveData.setValue("Lỗi kết nối");
            }

            // Cập nhật list gốc và hiển thị
            allProductsLiveData.setValue(products);
            applyClientSideState();
        });
    }

    // ========================================================================
    // CLIENT-SIDE LOGIC (XỬ LÝ TRÊN APP - GIỮ NGUYÊN)
    // ========================================================================

    public void searchProducts(String query, String type) {
        fsSearchQuery = query;
        fsSearchType = type;
        applyClientSideState();
    }

    public void sortProducts(int sortMode) {
        currentSortMode = sortMode;
        applyClientSideState();
    }

    public void filterFavoriteProductsApi() {
        switchSource(repository.getFavoriteProducts());
    }

    public void filterProducts(boolean showSelling, boolean showStopped, boolean showLowStock,
                               float minPrice, float maxPrice, boolean filterByOriginalPrice,
                               List<String> categoryIds) {

        fsShowSelling = showSelling;
        fsShowStopped = showStopped;
        fsShowLowStock = showLowStock;
        fsMinPrice = minPrice;
        fsMaxPrice = maxPrice;
        fsFilterByOriginalPrice = filterByOriginalPrice;
        fsCategoryIds = categoryIds;

        applyClientSideState();
    }

    private void sortListInternal(List<Product> list, int sortMode) {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        Collections.sort(list, (p1, p2) -> {
            double p1Price = p1.getPrice() * (1 - (p1.getDiscount() / 100.0));
            double p2Price = p2.getPrice() * (1 - (p2.getDiscount() / 100.0));

            if (sortMode == SORT_PRICE_ASC) return Double.compare(p1Price, p2Price);
            else if (sortMode == SORT_PRICE_DESC) return Double.compare(p2Price, p1Price);

            try {
                String d1Str = p1.getCreatedAt();
                String d2Str = p2.getCreatedAt();
                if (d1Str == null && d2Str == null) return 0;
                if (d1Str == null) return 1;
                if (d2Str == null) return -1;

                Date date1 = isoFormat.parse(d1Str);
                Date date2 = isoFormat.parse(d2Str);

                if (date1 == null || date2 == null) return 0;

                if (sortMode == SORT_DATE_NEWEST) return date2.compareTo(date1);
                else return date1.compareTo(date2);
            } catch (ParseException e) {
                return 0;
            }
        });
    }

    private void applyClientSideState() {
        List<Product> masterList = allProductsLiveData.getValue();
        if (masterList == null) masterList = new ArrayList<>();

        List<Product> result = new ArrayList<>();

        for (Product p : masterList) {
            boolean statusMatch = false;
            if (p.isStatus() && fsShowSelling) statusMatch = true;
            if (!p.isStatus() && fsShowStopped) statusMatch = true;
            if (!statusMatch) continue;

            if (fsShowLowStock && p.getQuantity() >= 10) continue;

            double originalPrice = p.getPrice();
            double priceToCheck = fsFilterByOriginalPrice
                    ? originalPrice
                    : originalPrice * (1 - (p.getDiscount() / 100.0));

            if (priceToCheck < fsMinPrice || priceToCheck > fsMaxPrice) continue;

            if (fsCategoryIds != null && !fsCategoryIds.isEmpty()) {
                String catId = p.getCategory() != null ? p.getCategory().get_id() : "";
                if (!fsCategoryIds.contains(catId)) continue;
            }

            // --- SEARCH ---
            if (fsSearchQuery != null && !fsSearchQuery.trim().isEmpty()) {
                String q = fsSearchQuery.toLowerCase();
                boolean match = false;

                if ("author".equalsIgnoreCase(fsSearchType)) {
                    match = p.getAuthor() != null
                            && p.getAuthor().getName() != null
                            && p.getAuthor().getName().toLowerCase().contains(q);
                } else {
                    match = p.getName() != null && p.getName().toLowerCase().contains(q);
                }

                if (!match) continue;
            }

            result.add(p);
        }

        sortListInternal(result, currentSortMode);
        displayedProductsLiveData.setValue(result);
    }

    // Sort
    public int getCurrentSortMode() {
        return currentSortMode;
    }

    // Status filter
    public boolean isShowSelling() {
        return fsShowSelling;
    }

    public boolean isShowStopped() {
        return fsShowStopped;
    }

    // Low stock
    public boolean isShowLowStock() {
        return fsShowLowStock;
    }

    // Price range
    public float getMinPrice() {
        return fsMinPrice;
    }

    public float getMaxPrice() {
        return fsMaxPrice;
    }

    // Category filter
    public List<String> getCategoryIds() {
        return fsCategoryIds;
    }

    // Search
    public String getSearchQuery() {
        return fsSearchQuery;
    }

    public String getSearchType() {
        return fsSearchType;
    }
}