package com.example.fa25_duan1.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

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

    // Nguồn dữ liệu hiện tại từ Repo
    private LiveData<List<Category>> currentRepoSource;

    public CategoryViewModel(@NonNull Application application) {
        super(application);
        repository = new CategoryRepository(application.getApplicationContext());
        refreshData();
    }

    /**
     * Lấy danh sách category để hiển thị (Fragment sẽ observe cái này)
     */
    public LiveData<List<Category>> getDisplayedCategories() {
        return displayedCategoriesLiveData;
    }

    /**
     * Tải lại dữ liệu từ Server
     */
    public void refreshData() {
        if (currentRepoSource != null) {
            displayedCategoriesLiveData.removeSource(currentRepoSource);
        }

        currentRepoSource = repository.getAllCategories();

        displayedCategoriesLiveData.addSource(currentRepoSource, categories -> {
            if (categories == null) {
                categories = new ArrayList<>();
            }

            // Cập nhật danh sách gốc
            allCategoriesLiveData.setValue(categories);

            // Mặc định hiển thị toàn bộ danh sách (có thể sort A-Z nếu muốn)
            List<Category> sorted = new ArrayList<>(categories);
            // Ví dụ sort theo tên A-Z
            sorted.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));

            displayedCategoriesLiveData.setValue(sorted);
            Log.d("CategoryViewModel", "Data refreshed. Loaded " + categories.size() + " categories.");
        });
    }

    // --- CRUD OPERATIONS ---

    public LiveData<Category> addCategory(String name) {
        // Tạo object Category mới để gửi đi
        // Lưu ý: Constructor của bạn là Category(String name, boolean isSelected)
        Category newCategory = new Category(name, false);
        return repository.addCategory(newCategory);
    }

    public LiveData<Category> updateCategory(String id, String name) {
        Category updateCategory = new Category(name, false);
        return repository.updateCategory(id, updateCategory);
    }

    public LiveData<Boolean> deleteCategory(String id) {
        return repository.deleteCategory(id);
    }

    public LiveData<Category> getCategoryByID(String id) {
        return repository.getCategoryByID(id);
    }

    // --- LOCAL FILTER / SEARCH ---

    /**
     * Tìm kiếm category theo tên
     */
    public void searchCategories(String query) {
        List<Category> masterList = allCategoriesLiveData.getValue();
        if (masterList == null) masterList = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            // Nếu query rỗng, trả về danh sách gốc (sort A-Z)
            List<Category> sorted = new ArrayList<>(masterList);
            sorted.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
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