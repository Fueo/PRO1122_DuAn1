package com.example.fa25_duan1.repository;

import android.content.Context;
import android.util.Log;

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
        // Lấy instance từ RetrofitClient
        this.discountApi = RetrofitClient.getInstance(context).getDiscountApi();
    }

    // 1. Lấy danh sách
    public MutableLiveData<List<Discount>> getAllDiscounts() {
        MutableLiveData<List<Discount>> liveData = new MutableLiveData<>();

        discountApi.getAllDiscounts().enqueue(new Callback<ApiResponse<List<Discount>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Discount>>> call, Response<ApiResponse<List<Discount>>> response) {
                // Kiểm tra: response thành công + body không null + status == true
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    liveData.setValue(response.body().getData());
                } else {
                    liveData.setValue(null);
                    Log.e("DiscountRepo", "Lỗi lấy dữ liệu: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Discount>>> call, Throwable t) {
                liveData.setValue(null);
                Log.e("DiscountRepo", "Lỗi kết nối: " + t.getMessage());
            }
        });
        return liveData;
    }

    // 2. Thêm mới
    public MutableLiveData<Boolean> addDiscount(Discount discount) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        discountApi.addDiscount(discount).enqueue(new Callback<ApiResponse<Discount>>() {
            @Override
            public void onResponse(Call<ApiResponse<Discount>> call, Response<ApiResponse<Discount>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    result.setValue(true);
                } else {
                    result.setValue(false);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Discount>> call, Throwable t) {
                result.setValue(false);
            }
        });
        return result;
    }

    // 3. Cập nhật
    public MutableLiveData<Boolean> updateDiscount(String id, Discount discount) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        discountApi.updateDiscount(id, discount).enqueue(new Callback<ApiResponse<Discount>>() {
            @Override
            public void onResponse(Call<ApiResponse<Discount>> call, Response<ApiResponse<Discount>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    result.setValue(true);
                } else {
                    result.setValue(false);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Discount>> call, Throwable t) {
                result.setValue(false);
            }
        });
        return result;
    }

    // 4. Xóa
    public MutableLiveData<Boolean> deleteDiscount(String id) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        discountApi.deleteDiscount(id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    result.setValue(true);
                } else {
                    result.setValue(false);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                result.setValue(false);
            }
        });
        return result;
    }

    // 5. Lấy chi tiết (Dùng cho màn hình Edit)
    public MutableLiveData<Discount> getDiscountById(String id) {
        MutableLiveData<Discount> result = new MutableLiveData<>();

        discountApi.getDiscountById(id).enqueue(new Callback<ApiResponse<Discount>>() {
            @Override
            public void onResponse(Call<ApiResponse<Discount>> call, Response<ApiResponse<Discount>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    result.setValue(response.body().getData());
                } else {
                    result.setValue(null);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Discount>> call, Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }
}