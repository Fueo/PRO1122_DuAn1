package com.example.fa25_duan1.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.Discount;
import com.example.fa25_duan1.network.DiscountApi;
import com.example.fa25_duan1.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiscountRepository {
    private final DiscountApi discountApi;

    public DiscountRepository(Context context) {
        this.discountApi = RetrofitClient.getInstance(context).getDiscountApi();
    }

    public MutableLiveData<List<Discount>> getAllDiscounts() {
        MutableLiveData<List<Discount>> liveData = new MutableLiveData<>();
        discountApi.getAllDiscounts().enqueue(new Callback<ApiResponse<List<Discount>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Discount>>> call, @NonNull Response<ApiResponse<List<Discount>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    liveData.setValue(response.body().getData());
                } else {
                    liveData.setValue(null);
                    Log.e("Repo", "Error: " + response.message());
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Discount>>> call, @NonNull Throwable t) {
                liveData.setValue(null);
                Log.e("Repo", "Fail: " + t.getMessage());
            }
        });
        return liveData;
    }

    public MutableLiveData<Boolean> addDiscount(Discount discount) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        discountApi.addDiscount(discount).enqueue(new Callback<ApiResponse<Discount>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Discount>> call, @NonNull Response<ApiResponse<Discount>> response) {
                // Backend trả 201 Created khi thành công
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    result.setValue(true);
                } else {
                    result.setValue(false);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Discount>> call, @NonNull Throwable t) {
                result.setValue(false);
            }
        });
        return result;
    }

    public MutableLiveData<Boolean> updateDiscount(String id, Discount discount) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        discountApi.updateDiscount(id, discount).enqueue(new Callback<ApiResponse<Discount>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Discount>> call, @NonNull Response<ApiResponse<Discount>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    result.setValue(true);
                } else {
                    result.setValue(false);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Discount>> call, @NonNull Throwable t) {
                result.setValue(false);
            }
        });
        return result;
    }

    public MutableLiveData<Boolean> deleteDiscount(String id) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        discountApi.deleteDiscount(id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    result.setValue(true);
                } else {
                    result.setValue(false);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                result.setValue(false);
            }
        });
        return result;
    }

    public MutableLiveData<Discount> getDiscountById(String id) {
        MutableLiveData<Discount> result = new MutableLiveData<>();
        discountApi.getDiscountById(id).enqueue(new Callback<ApiResponse<Discount>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Discount>> call, @NonNull Response<ApiResponse<Discount>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    // Object Discount này sẽ chứa appliedProducts (List<Product>)
                    result.setValue(response.body().getData());
                } else {
                    result.setValue(null);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Discount>> call, @NonNull Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }
}