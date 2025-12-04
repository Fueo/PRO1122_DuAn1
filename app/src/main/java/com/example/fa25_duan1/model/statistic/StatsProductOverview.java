package com.example.fa25_duan1.model.statistic;

import java.util.List;

public class StatsProductOverview {
    private List<TopProduct> topProducts;
    private int lowStockCount;
    private int activeCount;
    private int outOfStockCount;

    // Getters
    public List<TopProduct> getTopProducts() { return topProducts; }
    public int getLowStockCount() { return lowStockCount; }
    public int getActiveCount() { return activeCount; }
    public int getOutOfStockCount() { return outOfStockCount; }

    // Inner class cho TopProduct
    public static class TopProduct {
        private String name;
        private int sold; // 'value' trong JSON backend của bạn có thể là int hoặc double

        public String getName() { return name; }
        public int getSold() { return sold; }
    }
}