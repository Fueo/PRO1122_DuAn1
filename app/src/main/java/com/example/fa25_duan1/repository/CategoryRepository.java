package com.example.fa25_duan1.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.Category;
import com.example.fa25_duan1.network.CategoryApi;
import com.example.fa25_duan1.network.RetrofitClient;
import java.util.List;

public class CategoryRepository extends BaseRepository {
    private final CategoryApi categoryApi;

    public CategoryRepository(Context context) {
        categoryApi = RetrofitClient.getInstance(context).getCategoryApi();
    }

    // --- Get All Categories ---
    public LiveData<ApiResponse<List<Category>>> getAllCategories() {
        return performRequest(categoryApi.getAllCategories());
    }

    public LiveData<ApiResponse<Integer>> getTotalCategory() {
        return performRequest(categoryApi.getTotalCategory());
    }

    // --- Get Category by ID ---
    public LiveData<ApiResponse<Category>> getCategoryByID(String id) {
        return performRequest(categoryApi.getCategoryByID(id));
    }

    // --- Add Category ---
    public LiveData<ApiResponse<Category>> addCategory(Category category) {
        return performRequest(categoryApi.addCategory(category));
    }

    // --- Update Category ---
    public LiveData<ApiResponse<Category>> updateCategory(String id, Category category) {
        return performRequest(categoryApi.updateCategory(id, category));
    }

    // --- Delete Category ---
    // Assuming API interface is updated to return Call<ApiResponse<Void>>
    public LiveData<ApiResponse<Void>> deleteCategory(String id) {
        return performRequest(categoryApi.deleteCategory(id));
    }

    public LiveData<ApiResponse<List<Category>>> getAllCategoriesForAdmin() {
        return performRequest(categoryApi.getAllCategoriesForAdmin());
    }
}