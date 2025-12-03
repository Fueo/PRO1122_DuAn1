package com.example.fa25_duan1.model.statistic;

import java.util.List;

public class StatsRevenue {
    private long totalRevenue;
    private int totalOrders;
    private float growth;
    private long avgOrderValue;
    private int codPercent;
    private int bankingPercent;
    private List<ChartData> chartData;
    
    // API cũ có trả về topProducts, nếu API mới này không trả về thì xóa dòng này đi, 
    // hoặc giữ lại nếu bạn gộp chung API (tuỳ backend của bạn). 
    // Dựa vào code backend bạn vừa gửi thì API revenue KHÔNG trả về topProducts nữa.
    // private List<TopProduct> topProducts;

    // Getters
    public long getTotalRevenue() { return totalRevenue; }
    public int getTotalOrders() { return totalOrders; }
    public float getGrowth() { return growth; }
    public long getAvgOrderValue() { return avgOrderValue; }
    public int getCodPercent() { return codPercent; }
    public int getBankingPercent() { return bankingPercent; }
    public List<ChartData> getChartData() { return chartData; }

    // Inner Class cho dữ liệu biểu đồ
    public static class ChartData {
        private String date;
        private long value;

        public String getDate() { return date; }
        public long getValue() { return value; }
    }
}