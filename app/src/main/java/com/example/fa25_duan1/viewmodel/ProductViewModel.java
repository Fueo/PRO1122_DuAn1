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

    // Định nghĩa các hằng số cho chế độ sắp xếp
    public static final int SORT_DATE_NEWEST = 0;
    public static final int SORT_DATE_OLDEST = 1;
    public static final int SORT_PRICE_ASC = 2; // Giá tăng dần
    public static final int SORT_PRICE_DESC = 3; // Giá giảm dần

    private final ProductRepository repository;
    private final MediatorLiveData<List<Product>> allProductsLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<List<Product>> displayedProductsLiveData = new MediatorLiveData<>();
    private LiveData<List<Product>> currentRepoSource;

    // Thay thế boolean cũ bằng biến int để lưu 4 trạng thái
    private int currentSortMode = SORT_DATE_NEWEST;

    public ProductViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application.getApplicationContext());
        refreshData();
    }

    public LiveData<List<Product>> getDisplayedProducts() {
        return displayedProductsLiveData;
    }

    public void refreshData() {
        if (currentRepoSource != null) {
            displayedProductsLiveData.removeSource(currentRepoSource);
        }
        currentRepoSource = repository.getAllProducts();
        displayedProductsLiveData.addSource(currentRepoSource, products -> {
            if (products == null) products = new ArrayList<>();
            allProductsLiveData.setValue(products);

            // Sắp xếp lại theo chế độ hiện tại khi load mới
            List<Product> sorted = new ArrayList<>(products);
            sortListInternal(sorted, currentSortMode);
            displayedProductsLiveData.setValue(sorted);
        });
    }

    // ... (Giữ nguyên các hàm API CRUD/Search API ở đây) ...
    public LiveData<List<Product>> searchProductsByNameApi(String name) { return repository.searchProductsByName(name); }
    public LiveData<List<Product>> getRandomProductsApi(int limit) { return repository.getRandomProducts(limit); }
    public LiveData<Product> addProductWithImage(RequestBody name, RequestBody description, RequestBody pages, RequestBody publishDate, RequestBody status, RequestBody categoryID, RequestBody authorID, RequestBody price, RequestBody quantity, MultipartBody.Part image) { return repository.addProductWithImage(name, description, pages, publishDate, status, categoryID, authorID, price, quantity, image); }
    public LiveData<Product> updateProductWithImage(String id, RequestBody name, RequestBody description, RequestBody pages, RequestBody publishDate, RequestBody status, RequestBody categoryID, RequestBody authorID, RequestBody price, RequestBody quantity, MultipartBody.Part image) { return repository.updateProductWithImage(id, name, description, pages, publishDate, status, categoryID, authorID, price, quantity, image); }
    public LiveData<Boolean> deleteProduct(String id) { return repository.deleteProduct(id); }
    public LiveData<Product> getProductByID(String id) { return repository.getProductByID(id); }
    public LiveData<List<Product>> getProductsByAuthor(String authorId) { return repository.getProductsByAuthor(authorId); }
    public LiveData<List<Product>> getProductsByCategory(String categoryId) { return repository.getProductsByCategory(categoryId); }

    // --- LOGIC SEARCH / SORT / FILTER CLIENT-SIDE ---

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

    // --- SỬA ĐỔI: Hàm sắp xếp công khai mới ---
    public void sortProducts(int sortMode) {
        this.currentSortMode = sortMode; // Lưu trạng thái

        List<Product> current = displayedProductsLiveData.getValue();
        if (current == null || current.isEmpty()) return;

        List<Product> listToSort = new ArrayList<>(current);
        sortListInternal(listToSort, sortMode);

        displayedProductsLiveData.setValue(listToSort);
    }

    public void filterProducts(boolean showSelling, boolean showStopped, List<Integer> priceRanges, List<String> categoryIds) {
        List<Product> masterList = allProductsLiveData.getValue();
        if (masterList == null) masterList = new ArrayList<>();
        List<Product> result = new ArrayList<>();

        for (Product p : masterList) {
            // 1. Lọc trạng thái
            boolean statusMatch = false;
            if (p.isStatus() && showSelling) statusMatch = true;
            if (!p.isStatus() && showStopped) statusMatch = true;
            if (!statusMatch) continue;

            // 2. Lọc giá (Range)
            boolean priceMatch = false;
            if (priceRanges == null || priceRanges.isEmpty()) {
                priceMatch = true;
            } else {
                double price = p.getPrice();
                for (int range : priceRanges) {
                    if (range == 0 && price >= 0 && price <= 150000) priceMatch = true;
                    if (range == 1 && price > 150000 && price <= 300000) priceMatch = true;
                    if (range == 2 && price > 300000 && price <= 500000) priceMatch = true;
                    if (range == 3 && price > 500000) priceMatch = true;
                }
            }
            if (!priceMatch) continue;

            // 3. Lọc danh mục
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

        // Sắp xếp lại danh sách kết quả lọc theo chế độ hiện tại
        sortListInternal(result, currentSortMode);

        displayedProductsLiveData.setValue(result);
    }

    // --- SỬA ĐỔI: Hàm helper xử lý logic sắp xếp chi tiết ---
    private void sortListInternal(List<Product> list, int sortMode) {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

        Collections.sort(list, (p1, p2) -> {
            // --- SẮP XẾP THEO GIÁ ---
            if (sortMode == SORT_PRICE_ASC) {
                return Double.compare(p1.getPrice(), p2.getPrice());
            }
            else if (sortMode == SORT_PRICE_DESC) {
                return Double.compare(p2.getPrice(), p1.getPrice());
            }

            // --- SẮP XẾP THEO NGÀY (Mặc định) ---
            try {
                // Kiểm tra null date
                String d1Str = p1.getCreatedAt();
                String d2Str = p2.getCreatedAt();
                if (d1Str == null && d2Str == null) return 0;
                if (d1Str == null) return 1;
                if (d2Str == null) return -1;

                Date date1 = isoFormat.parse(d1Str);
                Date date2 = isoFormat.parse(d2Str);

                if (date1 == null || date2 == null) return 0;

                if (sortMode == SORT_DATE_NEWEST) {
                    return date2.compareTo(date1); // Giảm dần (Mới nhất trước)
                } else {
                    return date1.compareTo(date2); // Tăng dần (Cũ nhất trước)
                }
            } catch (ParseException e) {
                return 0;
            }
        });
    }
}