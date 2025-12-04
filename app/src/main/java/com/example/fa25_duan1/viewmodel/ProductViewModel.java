package com.example.fa25_duan1.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

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

    // Các hằng số cho chế độ sắp xếp
    public static final int SORT_DATE_NEWEST = 0;
    public static final int SORT_DATE_OLDEST = 1;
    public static final int SORT_PRICE_ASC = 2;
    public static final int SORT_PRICE_DESC = 3;

    private final ProductRepository repository;

    // LiveData chứa danh sách gốc (tất cả sản phẩm lấy từ API về)
    private final MediatorLiveData<List<Product>> allProductsLiveData = new MediatorLiveData<>();

    // LiveData chứa danh sách đang hiển thị (đã qua lọc, sắp xếp)
    private final MediatorLiveData<List<Product>> displayedProductsLiveData = new MediatorLiveData<>();

    // Nguồn dữ liệu hiện tại (để switch giữa các API khác nhau)
    private LiveData<List<Product>> currentRepoSource;

    private int currentSortMode = SORT_DATE_NEWEST;

    public ProductViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application.getApplicationContext());
    }

    // --- Getters ---
    public LiveData<List<Product>> getDisplayedProducts() {
        return displayedProductsLiveData;
    }

    public LiveData<List<Product>> getMListLiveData() {
        return displayedProductsLiveData;
    }

    // --- Data Fetching & Refresh ---
    public void refreshData() {
        switchSource(repository.getAllProducts());
    }

    public void resetToAllProducts() {
        refreshData();
    }

    // --- API Wrappers ---
    public LiveData<Product> viewProductApi(String id) {
        return repository.viewProduct(id);
    }

    public LiveData<Integer> getTotalProduct() {
        return repository.getTotalProduct();
    }

    public LiveData<List<Product>> getOnSaleProductsApi(int limit) {
        return repository.getOnSaleProducts(limit);
    }

    public void filterProductsByCategoryApi(String categoryId) {
        switchSource(repository.getProductsByCategory(categoryId));
    }

    public void searchProductsByNameApi(String keyword) {
        switchSource(repository.searchProductsByName(keyword));
    }

    public LiveData<List<Product>> getRandomProductsApi(int limit) {
        return repository.getRandomProducts(limit);
    }

    // Các hàm CRUD sản phẩm
    public LiveData<Product> addProductWithImage(RequestBody name, RequestBody description, RequestBody pages, RequestBody publishDate, RequestBody status, RequestBody categoryID, RequestBody authorID, RequestBody price, RequestBody quantity, MultipartBody.Part image) {
        return repository.addProductWithImage(name, description, pages, publishDate, status, categoryID, authorID, price, quantity, image);
    }

    public LiveData<Product> updateProductWithImage(String id, RequestBody name, RequestBody description, RequestBody pages, RequestBody publishDate, RequestBody status, RequestBody categoryID, RequestBody authorID, RequestBody price, RequestBody quantity, MultipartBody.Part image) {
        return repository.updateProductWithImage(id, name, description, pages, publishDate, status, categoryID, authorID, price, quantity, image);
    }

    public LiveData<Boolean> deleteProduct(String id) {
        return repository.deleteProduct(id);
    }

    public LiveData<Product> getProductByID(String id) {
        return repository.getProductByID(id);
    }

    public LiveData<List<Product>> getProductsByAuthor(String authorId) {
        return repository.getProductsByAuthor(authorId);
    }

    public LiveData<List<Product>> getProductsByCategory(String categoryId) {
        return repository.getProductsByCategory(categoryId);
    }

    // --- Helper: Switch Source ---
    private void switchSource(LiveData<List<Product>> newSource) {
        if (currentRepoSource != null) {
            displayedProductsLiveData.removeSource(currentRepoSource);
        }
        currentRepoSource = newSource;
        displayedProductsLiveData.addSource(currentRepoSource, products -> {
            if (products == null) products = new ArrayList<>();
            allProductsLiveData.setValue(products);
            // Mỗi khi có dữ liệu mới, copy sang displayed list và sắp xếp
            List<Product> sorted = new ArrayList<>(products);
            sortListInternal(sorted, currentSortMode);
            displayedProductsLiveData.setValue(sorted);
        });
    }

    // ========================================================================
    // CLIENT-SIDE LOGIC (XỬ LÝ TRÊN APP)
    // ========================================================================

    // 1. Tìm kiếm (Client-side filtering)
    public void searchProducts(String query, String type) {
        List<Product> masterList = allProductsLiveData.getValue();
        if (masterList == null) masterList = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            List<Product> resetList = new ArrayList<>(masterList);
            sortListInternal(resetList, currentSortMode);
            displayedProductsLiveData.setValue(resetList);
            return;
        }

        String q = query.toLowerCase().trim();
        List<Product> result = new ArrayList<>();
        final String searchType = (type != null) ? type.toLowerCase() : "";

        for (Product p : masterList) {
            boolean match = false;
            switch (searchType) {
                case "name": if (p.getName() != null && p.getName().toLowerCase().contains(q)) match = true; break;
                case "author": if (p.getAuthor() != null && p.getAuthor().getName() != null && p.getAuthor().getName().toLowerCase().contains(q)) match = true; break;
                default: if (p.getName() != null && p.getName().toLowerCase().contains(q)) match = true; break;
            }
            if (match) result.add(p);
        }

        sortListInternal(result, currentSortMode);
        displayedProductsLiveData.setValue(result);
    }

    // 2. Lọc nhanh sản phẩm giảm giá (API)
    public void filterOnSaleProductsApi() {
        switchSource(repository.getOnSaleProducts(0));
    }

    // 3. Sắp xếp
    public void sortProducts(int sortMode) {
        this.currentSortMode = sortMode;
        List<Product> current = displayedProductsLiveData.getValue();
        if (current == null || current.isEmpty()) return;

        List<Product> listToSort = new ArrayList<>(current);
        sortListInternal(listToSort, sortMode);
        displayedProductsLiveData.setValue(listToSort);
    }

    /**
     * [CẬP NHẬT MỚI] Hàm lọc sản phẩm chi tiết
     * @param showSelling : Lọc trạng thái Đang bán
     * @param showStopped : Lọc trạng thái Ngừng bán
     * @param minPrice    : Giá thấp nhất (từ thanh trượt)
     * @param maxPrice    : Giá cao nhất (từ thanh trượt)
     * @param categoryIds : Danh sách ID danh mục cần lọc
     */
    public void filterProducts(boolean showSelling, boolean showStopped, boolean showLowStock,
                               float minPrice, float maxPrice, boolean filterByOriginalPrice,
                               List<String> categoryIds) {

        List<Product> masterList = allProductsLiveData.getValue();
        if (masterList == null) masterList = new ArrayList<>();
        List<Product> result = new ArrayList<>();

        for (Product p : masterList) {
            // A. Lọc theo trạng thái
            boolean statusMatch = false;
            if (p.isStatus() && showSelling) statusMatch = true;
            if (!p.isStatus() && showStopped) statusMatch = true;
            if (!statusMatch) continue;

            // B. Lọc theo tồn kho (MỚI)
            // Nếu user check "Sắp hết hàng", chỉ lấy sp có quantity < 10
            if (showLowStock && p.getQuantity() >= 10) {
                continue;
            }

            // C. Lọc theo khoảng giá
            double originalPrice = p.getPrice();
            double priceToCheck;

            if (filterByOriginalPrice) {
                priceToCheck = originalPrice;
            } else {
                double discountPercent = p.getDiscount();
                priceToCheck = originalPrice * (1 - (discountPercent / 100.0));
            }

            if (priceToCheck < minPrice || priceToCheck > maxPrice) {
                continue;
            }

            // D. Lọc theo Category ID
            boolean categoryMatch = false;
            if (categoryIds == null || categoryIds.isEmpty()) {
                categoryMatch = true;
            } else {
                String pCatId = (p.getCategory() != null) ? p.getCategory().get_id() : "0";
                if (categoryIds.contains(pCatId)) categoryMatch = true;
            }
            if (!categoryMatch) continue;

            result.add(p);
        }

        sortListInternal(result, currentSortMode);
        displayedProductsLiveData.setValue(result);
    }

    // Helper: Logic sắp xếp nội bộ
    private void sortListInternal(List<Product> list, int sortMode) {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        Collections.sort(list, (p1, p2) -> {

            // Tính giá thực tế để sắp xếp cho chuẩn
            double p1Price = p1.getPrice() * (1 - (p1.getDiscount() / 100.0));
            double p2Price = p2.getPrice() * (1 - (p2.getDiscount() / 100.0));

            if (sortMode == SORT_PRICE_ASC) return Double.compare(p1Price, p2Price);
            else if (sortMode == SORT_PRICE_DESC) return Double.compare(p2Price, p1Price);

            // Sắp xếp theo ngày
            try {
                String d1Str = p1.getCreatedAt();
                String d2Str = p2.getCreatedAt();
                // Xử lý null safety cho ngày
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
}