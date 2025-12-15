package com.example.fa25_duan1.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.Discount;
import com.example.fa25_duan1.network.DiscountApi;
import com.example.fa25_duan1.network.RetrofitClient;

import java.util.List;

// 1. Kế thừa BaseRepository để dùng các hàm xử lý chung
public class DiscountRepository extends BaseRepository {
    private final DiscountApi discountApi;

    public DiscountRepository(Context context) {
        this.discountApi = RetrofitClient.getInstance(context).getDiscountApi();
    }

    // --- Lấy tất cả mã giảm giá ---
    public LiveData<ApiResponse<List<Discount>>> getAllDiscounts() {
        return performRequest(discountApi.getAllDiscounts());
    }

    // --- Lấy mã giảm giá theo ID ---
    public LiveData<ApiResponse<Discount>> getDiscountById(String id) {
        return performRequest(discountApi.getDiscountById(id));
    }

    // --- Thêm mã giảm giá ---
    public LiveData<ApiResponse<Discount>> addDiscount(Discount discount) {
        return performRequest(discountApi.addDiscount(discount));
    }

    // --- Cập nhật mã giảm giá ---
    public LiveData<ApiResponse<Discount>> updateDiscount(String id, Discount discount) {
        return performRequest(discountApi.updateDiscount(id, discount));
    }

    // --- Xóa mã giảm giá ---
    // Lưu ý: Đảm bảo bên DiscountApi hàm delete trả về Call<ApiResponse<Void>>
    public LiveData<ApiResponse<Void>> deleteDiscount(String id) {
        return performRequest(discountApi.deleteDiscount(id));
    }
}