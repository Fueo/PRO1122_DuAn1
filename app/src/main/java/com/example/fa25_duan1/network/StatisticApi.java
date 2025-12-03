package com.example.fa25_duan1.network;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.statistic.StatsDailyOverview;
import com.example.fa25_duan1.model.statistic.StatsDashboard;
import com.example.fa25_duan1.model.statistic.StatsOrderStatus;
import com.example.fa25_duan1.model.statistic.StatsRevenue;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface StatisticApi {

    @GET("stats/dashboard")
    Call<ApiResponse<StatsDashboard>> getDashboardStats();

    @GET("stats/order-status")
    Call<ApiResponse<StatsOrderStatus>> getOrderStatusStats();

    @GET("stats/revenue")
    Call<ApiResponse<StatsRevenue>> getRevenueStats(@Query("period") String period);

    @GET("stats/daily-overview")
    Call<ApiResponse<StatsDailyOverview>> getDailyOverview();
}