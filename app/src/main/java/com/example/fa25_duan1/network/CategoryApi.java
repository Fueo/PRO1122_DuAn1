package com.example.fa25_duan1.network;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.Category;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CategoryApi {

    // GET: Lấy tất cả danh mục
    @GET("categories/")
    Call<ApiResponse<List<Category>>> getAllCategories();

    // GET: Lấy danh mục theo ID
    @GET("categories/{id}")
    Call<ApiResponse<Category>> getCategoryByID(@Path("id") String id);

    // POST: Thêm danh mục (Gửi JSON Body)
    @POST("categories/add")
    Call<ApiResponse<Category>> addCategory(@Body Category category);

    @GET("categories/get-total-category")
    Call<ApiResponse<Integer>> getTotalCategory();

    // PUT: Cập nhật danh mục theo ID (Gửi JSON Body)
    @PUT("categories/update/{id}")
    Call<ApiResponse<Category>> updateCategory(@Path("id") String id, @Body Category category);

    // DELETE: Xóa danh mục theo ID
    @DELETE("categories/delete/{id}")
    Call<Void> deleteCategory(@Path("id") String id);
}