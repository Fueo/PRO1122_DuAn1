package com.example.fa25_duan1.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.FavoriteBody;
import com.example.fa25_duan1.network.FavoriteApi;
import com.example.fa25_duan1.network.RetrofitClient;

import java.util.List;

// 1. Kế thừa BaseRepository
public class FavoriteRepository extends BaseRepository {
    private final FavoriteApi favoriteApi;

    public FavoriteRepository(Context context) {
        favoriteApi = RetrofitClient.getInstance(context).getFavoriteApi();
    }

    // --- Lấy danh sách ID sản phẩm yêu thích ---
    public LiveData<ApiResponse<List<String>>> getMyFavorites() {
        return performRequest(favoriteApi.getMyFavorites());
    }

    // --- Thêm vào yêu thích ---
    // Thay vì trả về Boolean, ta trả về ApiResponse<Object>
    // ViewModel sẽ check response.isStatus() để biết thành công hay thất bại
    public LiveData<ApiResponse<Object>> addFavorite(String productId) {
        FavoriteBody body = new FavoriteBody(productId);
        return performRequest(favoriteApi.addFavorite(body));
    }

    // --- Xóa khỏi yêu thích ---
    public LiveData<ApiResponse<Object>> removeFavorite(String productId) {
        return performRequest(favoriteApi.removeFavorite(productId));
    }
}