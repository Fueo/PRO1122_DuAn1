package com.example.fa25_duan1.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.statistic.*;
import com.example.fa25_duan1.network.RetrofitClient;
import com.example.fa25_duan1.network.StatisticApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticRepository {
    private final StatisticApi api;

    public StatisticRepository(Context context) {
        this.api = RetrofitClient.getInstance(context).getStatisticApi();
    }

    public LiveData<ApiResponse<StatsDashboard>> getDashboardStats() {
        MutableLiveData<ApiResponse<StatsDashboard>> data = new MutableLiveData<>();
        api.getDashboardStats().enqueue(makeCallback(data));
        return data;
    }

    public LiveData<ApiResponse<StatsOrderStatus>> getOrderStatusStats() {
        MutableLiveData<ApiResponse<StatsOrderStatus>> data = new MutableLiveData<>();
        api.getOrderStatusStats().enqueue(makeCallback(data));
        return data;
    }

    public LiveData<ApiResponse<StatsRevenue>> getRevenueStats(String period) {
        MutableLiveData<ApiResponse<StatsRevenue>> data = new MutableLiveData<>();

        // Gọi API với tham số period
        api.getRevenueStats(period).enqueue(makeCallback(data));

        return data;
    }

    public LiveData<ApiResponse<StatsOrder>> getOrderStats(String period) {
        MutableLiveData<ApiResponse<StatsOrder>> data = new MutableLiveData<>();
        // Gọi API stats/order-stats với tham số period
        api.getOrderStats(period).enqueue(makeCallback(data));
        return data;
    }

    public LiveData<ApiResponse<StatsProductOverview>> getProductOverview(String period) {
        MutableLiveData<ApiResponse<StatsProductOverview>> data = new MutableLiveData<>();
        api.getProductOverview(period).enqueue(makeCallback(data));
        return data;
    }

    // Helper để giảm code lặp
    private <T> Callback<ApiResponse<T>> makeCallback(MutableLiveData<ApiResponse<T>> liveData) {
        return new Callback<ApiResponse<T>>() {
            @Override
            public void onResponse(Call<ApiResponse<T>> call, Response<ApiResponse<T>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    liveData.setValue(response.body());
                } else {
                    liveData.setValue(null);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<T>> call, Throwable t) {
                liveData.setValue(null);
            }
        };
    }

    public LiveData<ApiResponse<StatsDailyOverview>> getDailyOverview() {
        MutableLiveData<ApiResponse<StatsDailyOverview>> data = new MutableLiveData<>();
        // Sử dụng hàm makeCallback (helper) đã viết ở bước trước để gọn code
        api.getDailyOverview().enqueue(makeCallback(data));
        return data;
    }
}