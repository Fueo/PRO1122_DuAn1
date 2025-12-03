package com.example.fa25_duan1.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.statistic.*;
import com.example.fa25_duan1.repository.StatisticRepository;

public class StatisticViewModel extends AndroidViewModel {
    private final StatisticRepository repository;

    public StatisticViewModel(@NonNull Application application) {
        super(application);
        repository = new StatisticRepository(application);
    }

    public LiveData<ApiResponse<StatsDashboard>> getDashboardStats() {
        return repository.getDashboardStats();
    }

    public LiveData<ApiResponse<StatsOrderStatus>> getOrderStatusStats() {
        return repository.getOrderStatusStats();
    }

    public LiveData<ApiResponse<StatsRevenue>> getRevenueStats(String period) {
        return repository.getRevenueStats(period);
    }

    public LiveData<ApiResponse<StatsDailyOverview>> getDailyOverview() {
        return repository.getDailyOverview();
    }
}