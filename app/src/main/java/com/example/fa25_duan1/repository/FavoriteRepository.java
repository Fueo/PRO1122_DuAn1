package com.example.fa25_duan1.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.FavoriteBody;
import com.example.fa25_duan1.network.FavoriteApi;
import com.example.fa25_duan1.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteRepository {
    private final FavoriteApi favoriteApi;

    public FavoriteRepository(Context context) {
        favoriteApi = RetrofitClient.getInstance(context).getFavoriteApi();
    }

    // --- Get All Favorite IDs ---
    public LiveData<List<String>> getMyFavorites() {
        MutableLiveData<List<String>> data = new MutableLiveData<>();
        favoriteApi.getMyFavorites().enqueue(new Callback<ApiResponse<List<String>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<String>>> call, @NonNull Response<ApiResponse<List<String>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body().getData() != null ? response.body().getData() : new ArrayList<>());
                } else {
                    data.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<String>>> call, @NonNull Throwable t) {
                data.setValue(new ArrayList<>());
            }
        });
        return data;
    }

    // --- Add Favorite ---
    // Trả về Boolean: true nếu thành công, false nếu thất bại
    public LiveData<Boolean> addFavorite(String productId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        FavoriteBody body = new FavoriteBody(productId);

        favoriteApi.addFavorite(body).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                // Backend trả về 201 Created là thành công
                result.setValue(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                result.setValue(false);
            }
        });
        return result;
    }

    // --- Remove Favorite ---
    public LiveData<Boolean> removeFavorite(String productId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        favoriteApi.removeFavorite(productId).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                result.setValue(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                result.setValue(false);
            }
        });
        return result;
    }
}