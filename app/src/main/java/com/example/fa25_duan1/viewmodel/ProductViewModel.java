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

    public void refreshData() {
        // ... (Giữ nguyên)
        if (currentRepoSource != null) {
            displayedProductsLiveData.removeSource(currentRepoSource);
        }

        currentRepoSource = repository.getAllProducts();

        displayedProductsLiveData.addSource(currentRepoSource, products -> {
            if (products == null) {
                products = new ArrayList<>();
            }

            allProductsLiveData.setValue(products);

            List<Product> sorted = new ArrayList<>(products);
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

            sorted.sort((p1, p2) -> {
                try {
                    if (p1.getCreatedAt() == null || p2.getCreatedAt() == null) return 0;

                    Date date1 = isoFormat.parse(p1.getCreatedAt());
                    Date date2 = isoFormat.parse(p2.getCreatedAt());

                    // So sánh theo Date
                    return p2.getCreatedAt().compareTo(p1.getCreatedAt());

                } catch (ParseException e) {
                    // Xử lý khi parse lỗi (ví dụ: dữ liệu createdAt bị sai định dạng)
                    Log.e("ViewModel", "Date parsing error for product sort: " + e.getMessage());
                    return 0; // Không sắp xếp nếu lỗi
                }
            });
            displayedProductsLiveData.setValue(sorted);
            Log.d("ProductViewModel", "Data refreshed. Loaded " + products.size() + " products.");
        });
    }

    // --- CRUD ---

    public LiveData<Product> addProductWithImage(RequestBody name,
                                                 RequestBody description,
                                                 RequestBody pages, // Sửa thứ tự
                                                 RequestBody publishDate, // Sửa thứ tự
                                                 RequestBody status, // Sửa thứ tự
                                                 RequestBody categoryID, // Sửa thứ tự
                                                 RequestBody authorID, // Sửa thứ tự
                                                 RequestBody price, // Sửa thứ tự
                                                 RequestBody quantity, // Sửa thứ tự
                                                 MultipartBody.Part image) {
        return repository.addProductWithImage(name, description, pages, publishDate, status, categoryID, authorID, price, quantity, image);
    }

    public LiveData<Product> updateProductWithImage(String id,
                                                    RequestBody name,
                                                    RequestBody description,
                                                    RequestBody pages, // Sửa thứ tự
                                                    RequestBody publishDate, // Sửa thứ tự
                                                    RequestBody status, // Sửa thứ tự
                                                    RequestBody categoryID, // Sửa thứ tự
                                                    RequestBody authorID, // Sửa thứ tự
                                                    RequestBody price, // Sửa thứ tự
                                                    RequestBody quantity, // Sửa thứ tự
                                                    MultipartBody.Part image) {
        return repository.updateProductWithImage(id, name, description, pages, publishDate, status, categoryID, authorID, price, quantity, image);
    }

    public LiveData<Boolean> deleteProduct(String id) {
        return repository.deleteProduct(id);
    }

    public LiveData<Product> getProductByID(String id) {
        return repository.getProductByID(id);
    }

    // --- TÌM KIẾM ---
    // ... (Giữ nguyên các hàm tìm kiếm và sắp xếp) ...
    /**
     * Tìm kiếm sản phẩm theo tên
     */
    public void searchProducts(String query, String type) {
        // Lấy danh sách sản phẩm gốc (master list)
        List<Product> masterList = allProductsLiveData.getValue();
        if (masterList == null) masterList = new ArrayList<>();

        // 1. Xử lý trường hợp chuỗi tìm kiếm rỗng
        if (query == null || query.trim().isEmpty()) {
            // Nếu query rỗng, refresh lại data để trả về danh sách đã sắp xếp mặc định
            refreshData();
            return;
        }

        String q = query.toLowerCase().trim();
        List<Product> result = new ArrayList<>();

        // Định nghĩa loại tìm kiếm (an toàn hơn)
        final String searchType = (type != null) ? type.toLowerCase() : "";

        for (Product p : masterList) {
            boolean match = false;

            // --- 2. Logic tìm kiếm sử dụng SWITCH CASE ---
            switch (searchType) {
                case "name":
                    // Tìm kiếm theo tên sản phẩm
                    if (p.getName() != null && p.getName().toLowerCase().contains(q)) {
                        match = true;
                    }
                    break;

                case "author":
                    // Tìm kiếm theo tên tác giả (Sử dụng đối tượng Author đã được parse)
                    Author author = p.getAuthor();
                    if (author != null && author.getName() != null &&
                            author.getName().toLowerCase().contains(q)) {
                        match = true;
                    }
                    break;

                // Tùy chọn: Thêm các loại tìm kiếm khác ở đây (ví dụ: case "category")

                default:
                    // Tùy chọn: Nếu không chỉ định loại, tìm kiếm theo tên SP là mặc định
                    if (p.getName() != null && p.getName().toLowerCase().contains(q)) {
                        match = true;
                    }
                    break;
            }

            if (match) {
                result.add(p);
            }
        }

        // 3. Cập nhật LiveData hiển thị
        displayedProductsLiveData.setValue(result);
    }

    // --- SẮP XẾP ---

    /**
     * Sắp xếp sản phẩm theo ngày tạo (createdAt)
     */
    public void sortByCreateAt(boolean newestFirst) {
        List<Product> current = displayedProductsLiveData.getValue();
        if (current == null || current.isEmpty()) return;

        List<Product> listToSort = new ArrayList<>(current);

        listToSort.sort((p1, p2) -> {
            if (p1.getCreatedAt() == null || p2.getCreatedAt() == null) return 0;
            return newestFirst ?
                    p2.getCreatedAt().compareTo(p1.getCreatedAt()) :
                    p1.getCreatedAt().compareTo(p2.getCreatedAt());
        });

        displayedProductsLiveData.setValue(listToSort);
    }
}