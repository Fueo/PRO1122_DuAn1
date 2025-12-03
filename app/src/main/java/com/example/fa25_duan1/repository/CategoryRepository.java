package com.example.fa25_duan1.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.Category;
import com.example.fa25_duan1.network.CategoryApi;
import com.example.fa25_duan1.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryRepository {
    private final CategoryApi categoryApi;
    public CategoryRepository(Context context) {
        categoryApi = RetrofitClient.getInstance(context).getCategoryApi();
    }

    // --- Get All Categories ---
    public LiveData<List<Category>> getAllCategories() {
        MutableLiveData<List<Category>> data = new MutableLiveData<>();
        categoryApi.getAllCategories().enqueue(new Callback<ApiResponse<List<Category>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Category>>> call, @NonNull Response<ApiResponse<List<Category>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body().getData() != null ? response.body().getData() : new ArrayList<>());
                } else {
                    data.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Category>>> call, @NonNull Throwable t) {
                data.setValue(new ArrayList<>());
            }
        });
        return data;
    }

    public LiveData<Integer> getTotalCategory() {
        MutableLiveData<Integer> countData = new MutableLiveData<>();
        categoryApi.getTotalCategory().enqueue(new Callback<ApiResponse<Integer>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Integer>> call, @NonNull Response<ApiResponse<Integer>> response) {
                if (response.isSuccessful() && response.body() != null) countData.setValue(response.body().getData());
                else countData.setValue(0);
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Integer>> call, @NonNull Throwable t) { countData.setValue(0); }
        });
        return countData;
    }

    // --- Get Category by ID ---
    public LiveData<Category> getCategoryByID(String id) {
        MutableLiveData<Category> data = new MutableLiveData<>();
        categoryApi.getCategoryByID(id).enqueue(new Callback<ApiResponse<Category>>() {
            @Override
            public void onResponse(Call<ApiResponse<Category>> call, Response<ApiResponse<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body().getData());
                } else {
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Category>> call, Throwable t) {
                data.setValue(null);
            }
        });
        return data;
    }

    // --- Add Category ---
    public LiveData<Category> addCategory(Category category) {
        MutableLiveData<Category> result = new MutableLiveData<>();
        categoryApi.addCategory(category).enqueue(new Callback<ApiResponse<Category>>() {
            @Override
            public void onResponse(Call<ApiResponse<Category>> call, Response<ApiResponse<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body().getData());
                } else {
                    result.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Category>> call, Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    // --- Update Category ---
    public LiveData<Category> updateCategory(String id, Category category) {
        MutableLiveData<Category> result = new MutableLiveData<>();
        categoryApi.updateCategory(id, category).enqueue(new Callback<ApiResponse<Category>>() {
            @Override
            public void onResponse(Call<ApiResponse<Category>> call, Response<ApiResponse<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body().getData());
                } else {
                    result.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Category>> call, Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    // --- Delete Category ---
    public LiveData<Boolean> deleteCategory(String id) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        categoryApi.deleteCategory(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // API trả về 200 OK là thành công
                result.setValue(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(false);
            }
        });
        return result;
    }
}