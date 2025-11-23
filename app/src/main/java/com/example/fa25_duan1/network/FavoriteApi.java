package com.example.fa25_duan1.network;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.FavoriteBody;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FavoriteApi {

    // 1. GET: Lấy danh sách ID các sản phẩm đã yêu thích
    // Backend trả về: { status: true, data: ["id1", "id2", ...] }
    @GET("favorite/")
    Call<ApiResponse<List<String>>> getMyFavorites();

    // 2. POST: Thêm yêu thích
    // Body gửi lên: { "ProductID": "..." }
    @POST("favorite/add")
    Call<ApiResponse<Object>> addFavorite(@Body FavoriteBody body);

    // 3. DELETE: Bỏ yêu thích theo ProductID
    @DELETE("favorite/delete/{productId}")
    Call<ApiResponse<Object>> removeFavorite(@Path("productId") String productId);
}