package com.example.fa25_duan1.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.Category;
import com.example.fa25_duan1.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;

public class CategoryViewModel extends AndroidViewModel {

    private final CategoryRepository repository;

    // ===== MASTER LIST =====
    // Danh sách gốc để search / filter
    private final MediatorLiveData<List<Category>> allCategoriesLiveData = new MediatorLiveData<>();

    // ===== USER LIST =====
    // Chỉ danh mục có sản phẩm (API /categories/)
    private final MediatorLiveData<List<Category>> displayedCategoriesLiveData = new MediatorLiveData<>();

    // ===== ADMIN LIST =====
    // Tất cả danh mục (kể cả không có sản phẩm)
    private final MediatorLiveData<List<Category>> allCategoriesForAdminLiveData = new MediatorLiveData<>();

    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>();

    private LiveData<ApiResponse<List<Category>>> userRepoSource;
    private LiveData<ApiResponse<List<Category>>> adminRepoSource;

    public CategoryViewModel(@NonNull Application application) {
        super(application);
        repository = new CategoryRepository(application.getApplicationContext());
    }

    // =======================
    // GETTERS
    // =======================

    // USER
    public LiveData<List<Category>> getDisplayedCategories() {
        return displayedCategoriesLiveData;
    }

    // ADMIN
    public LiveData<List<Category>> getAllCategoriesForAdmin() {
        return allCategoriesForAdminLiveData;
    }

    public LiveData<String> getMessage() {
        return messageLiveData;
    }

    public List<Category> getMasterCategoryList() {
        return allCategoriesLiveData.getValue() != null
                ? allCategoriesLiveData.getValue()
                : new ArrayList<>();
    }

    // =======================
    // USER FLOW
    // =======================

    /**
     * USER: chỉ lấy danh mục có sản phẩm
     */
    public void refreshData() {
        if (userRepoSource != null) {
            displayedCategoriesLiveData.removeSource(userRepoSource);
        }

        userRepoSource = repository.getAllCategories();

        displayedCategoriesLiveData.addSource(userRepoSource, this::handleUserResponse);
    }

    private void handleUserResponse(ApiResponse<List<Category>> response) {
        if (response != null && response.isStatus() && response.getData() != null) {

            List<Category> list = new ArrayList<>(response.getData());

            // ✅ sort mới nhất cho USER
            list.sort((a, b) -> {
                if (a.getCreateAt() == null || b.getCreateAt() == null) return 0;
                return b.getCreateAt().compareTo(a.getCreateAt());
            });

            displayedCategoriesLiveData.setValue(list);

        } else if (response != null) {
            messageLiveData.setValue(response.getMessage());
        }
    }

    // =======================
    // ADMIN FLOW
    // =======================

    /**
     * ADMIN / STAFF: lấy tất cả danh mục
     */
    public void refreshAllForAdmin() {
        if (adminRepoSource != null) {
            allCategoriesForAdminLiveData.removeSource(adminRepoSource);
        }

        adminRepoSource = repository.getAllCategoriesForAdmin();

        allCategoriesForAdminLiveData.addSource(adminRepoSource, response -> {
            if (response != null && response.isStatus() && response.getData() != null) {

                List<Category> list = new ArrayList<>(response.getData());

                // sort mặc định: mới nhất
                list.sort((a, b) -> {
                    if (a.getCreateAt() == null || b.getCreateAt() == null) return 0;
                    return b.getCreateAt().compareTo(a.getCreateAt());
                });

                allCategoriesForAdminLiveData.setValue(list);
                allCategoriesLiveData.setValue(list);

            } else if (response != null) {
                messageLiveData.setValue(response.getMessage());
            }
        });
    }

    // =======================
    // SORT & SEARCH (ADMIN)
    // =======================

    public void sortCategories(int type) {
        List<Category> list = allCategoriesForAdminLiveData.getValue();
        if (list == null) return;

        List<Category> sorted = new ArrayList<>(list);

        switch (type) {
            case 0: // Mới nhất
                sorted.sort((a, b) -> {
                    if (a.getCreateAt() == null || b.getCreateAt() == null) return 0;
                    return b.getCreateAt().compareTo(a.getCreateAt());
                });
                break;

            case 1: // Cũ nhất
                sorted.sort((a, b) -> {
                    if (a.getCreateAt() == null || b.getCreateAt() == null) return 0;
                    return a.getCreateAt().compareTo(b.getCreateAt());
                });
                break;

            case 2: // A-Z
            default:
                sorted.sort((a, b) -> {
                    String n1 = a.getName() != null ? a.getName() : "";
                    String n2 = b.getName() != null ? b.getName() : "";
                    return n1.compareToIgnoreCase(n2);
                });
                break;
        }

        allCategoriesForAdminLiveData.setValue(sorted);
    }

    public void searchCategories(String query) {
        List<Category> master = getMasterCategoryList();

        if (query == null || query.trim().isEmpty()) {
            allCategoriesForAdminLiveData.setValue(new ArrayList<>(master));
            return;
        }

        String q = query.toLowerCase().trim();
        List<Category> result = new ArrayList<>();

        for (Category c : master) {
            if (c.getName() != null && c.getName().toLowerCase().contains(q)) {
                result.add(c);
            }
        }

        allCategoriesForAdminLiveData.setValue(result);
    }

    // =======================
    // CRUD
    // =======================

    public LiveData<ApiResponse<Category>> addCategory(String name) {
        return repository.addCategory(new Category(name, false));
    }

    public LiveData<ApiResponse<Category>> updateCategory(String id, String name) {
        return repository.updateCategory(id, new Category(name, false));
    }

    public LiveData<ApiResponse<Void>> deleteCategory(String id) {
        return repository.deleteCategory(id);
    }

    public LiveData<ApiResponse<Category>> getCategoryByID(String id) {
        return repository.getCategoryByID(id);
    }

    public LiveData<ApiResponse<Integer>> getTotalCategory() {
        return repository.getTotalCategory();
    }
}
