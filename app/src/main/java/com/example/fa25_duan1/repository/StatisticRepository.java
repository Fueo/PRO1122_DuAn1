package com.example.fa25_duan1.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.statistic.*;
import com.example.fa25_duan1.network.RetrofitClient;
import com.example.fa25_duan1.network.StatisticApi;

public class StatisticRepository extends BaseRepository {
    private final StatisticApi api;

    public StatisticRepository(Context context) {
        this.api = RetrofitClient.getInstance(context).getStatisticApi();
    }

    public LiveData<ApiResponse<StatsDashboard>> getDashboardStats() {
        return performRequest(api.getDashboardStats());
    }

    public LiveData<ApiResponse<StatsOrderStatus>> getOrderStatusStats() {
        return performRequest(api.getOrderStatusStats());
    }

    public LiveData<ApiResponse<StatsRevenue>> getRevenueStats(String period) {
        return performRequest(api.getRevenueStats(period));
    }

    public LiveData<ApiResponse<StatsOrder>> getOrderStats(String period) {
        return performRequest(api.getOrderStats(period));
    }

    public LiveData<ApiResponse<StatsProductOverview>> getProductOverview(String period) {
        return performRequest(api.getProductOverview(period));
    }

    public LiveData<ApiResponse<StatsDailyOverview>> getDailyOverview() {
        return performRequest(api.getDailyOverview());
    }
}