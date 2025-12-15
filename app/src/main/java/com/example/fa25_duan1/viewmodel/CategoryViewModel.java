package com.example.fa25_duan1.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fa25_duan1.model.ApiResponse; // Import ApiResponse
import com.example.fa25_duan1.model.Category;
import com.example.fa25_duan1.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;

public class CategoryViewModel extends AndroidViewModel {

    private final CategoryRepository repository;

    // Danh sách gốc (Master list)
    private final MediatorLiveData<List<Category>> allCategoriesLiveData = new MediatorLiveData<>();

    // Danh sách hiển thị lên UI (sau khi filter/search)
    private final MediatorLiveData<List<Category>> displayedCategoriesLiveData = new MediatorLiveData<>();

    // LiveData để báo lỗi cho View
    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>();

    // Nguồn dữ liệu hiện tại từ Repo (Đổi thành ApiResponse)
    private LiveData<ApiResponse<List<Category>>> currentRepoSource;

    public CategoryViewModel(@NonNull Application application) {
        super(application);
        repository = new CategoryRepository(application.getApplicationContext());
        refreshData();
    }

    /**
     * Lấy danh sách category để hiển thị
     */
    public LiveData<List<Category>> getDisplayedCategories() {
        return displayedCategoriesLiveData;
    }

    public LiveData<String> getMessage() {
        return messageLiveData;
    }

    /**
     * Tải lại dữ liệu từ Server
     */
    public void refreshData() {
        if (currentRepoSource != null) {
            displayedCategoriesLiveData.removeSource(currentRepoSource);
        }

        // Gọi Repository (trả về ApiResponse)
        currentRepoSource = repository.getAllCategories();

        displayedCategoriesLiveData.addSource(currentRepoSource, apiResponse -> {
            List<Category> categories = new ArrayList<>();

            if (apiResponse != null) {
                if (apiResponse.isStatus()) {
                    // Thành công: Lấy data
                    if (apiResponse.getData() != null) {
                        categories = apiResponse.getData();
                    }
                } else {
                    // Thất bại: Gửi thông báo lỗi
                    messageLiveData.setValue(apiResponse.getMessage());
                }
            } else {
                messageLiveData.setValue("Lỗi kết nối");
            }

            // Cập nhật danh sách gốc
            allCategoriesLiveData.setValue(categories);

            // Sắp xếp mặc định: Mới nhất lên đầu
            List<Category> sorted = new ArrayList<>(categories);
            sorted.sort((a1, a2) -> {
                if (a1.getCreateAt() == null || a2.getCreateAt() == null) return 0;
                return a2.getCreateAt().compareTo(a1.getCreateAt());
            });

            displayedCategoriesLiveData.setValue(sorted);
        });
    }

    public LiveData<ApiResponse<Integer>> getTotalCategory() {
        return repository.getTotalCategory();
    }

    // --- CRUD OPERATIONS (CẬP NHẬT KIỂU TRẢ VỀ APIRESPONSE) ---

    public LiveData<ApiResponse<Category>> addCategory(String name) {
        // Tạo object Category mới
        Category newCategory = new Category(name, false);
        return repository.addCategory(newCategory);
    }

    public LiveData<ApiResponse<Category>> updateCategory(String id, String name) {
        Category updateCategory = new Category(name, false);
        return repository.updateCategory(id, updateCategory);
    }

    public LiveData<ApiResponse<Void>> deleteCategory(String id) {
        return repository.deleteCategory(id);
    }

    public LiveData<ApiResponse<Category>> getCategoryByID(String id) {
        return repository.getCategoryByID(id);
    }

    // --- LOCAL SORT & SEARCH (Logic giữ nguyên) ---

    public void sortCategories(int type) {
        List<Category> masterList = allCategoriesLiveData.getValue();
        if (masterList == null) masterList = new ArrayList<>();

        List<Category> sorted = new ArrayList<>(masterList);

        switch (type) {
            case 0: // Mới nhất
                sorted.sort((c1, c2) -> {
                    if (c1.getCreateAt() == null || c2.getCreateAt() == null) return 0;
                    return c2.getCreateAt().compareTo(c1.getCreateAt()); // DESC
                });
                break;

            case 1: // Cũ nhất
                sorted.sort((c1, c2) -> {
                    if (c1.getCreateAt() == null || c2.getCreateAt() == null) return 0;
                    return c1.getCreateAt().compareTo(c2.getCreateAt()); // ASC
                });
                break;

            case 2: // Tên A-Z
            default:
                sorted.sort((c1, c2) -> {
                    String n1 = c1.getName() != null ? c1.getName() : "";
                    String n2 = c2.getName() != null ? c2.getName() : "";
                    return n1.compareToIgnoreCase(n2);
                });
                break;
        }

        displayedCategoriesLiveData.setValue(sorted);
    }

    /**
     * Tìm kiếm category theo tên
     */
    public void searchCategories(String query) {
        List<Category> masterList = allCategoriesLiveData.getValue();
        if (masterList == null) masterList = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            // Nếu query rỗng, trả về danh sách gốc (có thể sort lại A-Z cho dễ nhìn)
            List<Category> sorted = new ArrayList<>(masterList);
            sorted.sort((c1, c2) -> {
                String n1 = c1.getName() != null ? c1.getName() : "";
                String n2 = c2.getName() != null ? c2.getName() : "";
                return n1.compareToIgnoreCase(n2);
            });
            displayedCategoriesLiveData.setValue(sorted);
            return;
        }

        String q = query.toLowerCase().trim();
        List<Category> result = new ArrayList<>();

        for (Category c : masterList) {
            if (c.getName() != null && c.getName().toLowerCase().contains(q)) {
                result.add(c);
            }
        }
        displayedCategoriesLiveData.setValue(result);
    }
}