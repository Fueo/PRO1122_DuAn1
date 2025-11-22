package com.example.fa25_duan1.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.fa25_duan1.model.Author;
import com.example.fa25_duan1.model.Product;
import com.example.fa25_duan1.repository.ProductRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ProductViewModel extends AndroidViewModel {

    private final ProductRepository repository;
    private final MediatorLiveData<List<Product>> allProductsLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<List<Product>> displayedProductsLiveData = new MediatorLiveData<>();
    private LiveData<List<Product>> currentRepoSource;

    public ProductViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application.getApplicationContext());
        refreshData();
    }

    public LiveData<List<Product>> getDisplayedProducts() {
        return displayedProductsLiveData;
    }

    // ... (Hàm refreshData giữ nguyên) ...
    public void refreshData() {
        if (currentRepoSource != null) {
            displayedProductsLiveData.removeSource(currentRepoSource);
        }
        currentRepoSource = repository.getAllProducts();
        displayedProductsLiveData.addSource(currentRepoSource, products -> {
            if (products == null) products = new ArrayList<>();
            allProductsLiveData.setValue(products);
            // Sắp xếp mặc định (mới nhất)
            List<Product> sorted = new ArrayList<>(products);
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            sorted.sort((p1, p2) -> {
                try {
                    if (p1.getCreatedAt() == null || p2.getCreatedAt() == null) return 0;
                    Date date1 = isoFormat.parse(p1.getCreatedAt());
                    Date date2 = isoFormat.parse(p2.getCreatedAt());
                    return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                } catch (ParseException e) {
                    return 0;
                }
            });
            displayedProductsLiveData.setValue(sorted);
        });
    }

    // 🆕 Search Products By Name (Server-side)
    // Hàm này dùng khi bạn muốn tìm kiếm trực tiếp từ server thay vì filter list có sẵn
    public LiveData<List<Product>> searchProductsByNameApi(String name) {
        return repository.searchProductsByName(name);
    }

    // 🆕 Get Random Products (Server-side)
    // Hàm này dùng cho tính năng "Đọc gì hôm nay" (Random) nếu muốn lấy ngẫu nhiên thật sự từ server
    public LiveData<List<Product>> getRandomProductsApi(int limit) {
        return repository.getRandomProducts(limit);
    }

    // ... (Các hàm CRUD giữ nguyên) ...
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

    // ... (Các hàm searchProducts, sortByCreateAt, filterProducts giữ nguyên - Client side logic) ...
    // Bạn vẫn có thể giữ lại các hàm search/filter client-side này nếu muốn lọc trên danh sách đã tải về
    // thay vì gọi API search mới mỗi lần gõ phím.

    public void searchProducts(String query, String type) {
        List<Product> masterList = allProductsLiveData.getValue();
        if (masterList == null) masterList = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            refreshData();
            return;
        }

        String q = query.toLowerCase().trim();
        List<Product> result = new ArrayList<>();
        final String searchType = (type != null) ? type.toLowerCase() : "";

        for (Product p : masterList) {
            boolean match = false;
            switch (searchType) {
                case "name":
                    if (p.getName() != null && p.getName().toLowerCase().contains(q)) match = true;
                    break;
                case "author":
                    Author author = p.getAuthor();
                    if (author != null && author.getName() != null && author.getName().toLowerCase().contains(q)) match = true;
                    break;
                default:
                    if (p.getName() != null && p.getName().toLowerCase().contains(q)) match = true;
                    break;
            }
            if (match) result.add(p);
        }
        displayedProductsLiveData.setValue(result);
    }

    public void sortByCreateAt(boolean newestFirst) {
        List<Product> current = displayedProductsLiveData.getValue();
        if (current == null || current.isEmpty()) return;
        List<Product> listToSort = new ArrayList<>(current);
        listToSort.sort((p1, p2) -> {
            if (p1.getCreatedAt() == null || p2.getCreatedAt() == null) return 0;
            return newestFirst ? p2.getCreatedAt().compareTo(p1.getCreatedAt()) : p1.getCreatedAt().compareTo(p2.getCreatedAt());
        });
        displayedProductsLiveData.setValue(listToSort);
    }

    public void filterProducts(boolean showSelling, boolean showStopped, List<Integer> priceRanges, List<String> categoryIds) {
        List<Product> masterList = allProductsLiveData.getValue();
        if (masterList == null) masterList = new ArrayList<>();
        List<Product> result = new ArrayList<>();

        for (Product p : masterList) {
            boolean statusMatch = false;
            if (p.isStatus() && showSelling) statusMatch = true;
            if (!p.isStatus() && showStopped) statusMatch = true;
            if (!statusMatch) continue;

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
        displayedProductsLiveData.setValue(result);
    }
}