package com.example.fa25_duan1.model.statistic;

import java.util.List;

public class StatsOrder {
    private List<PieDataEntry> pieData;
    private double successRate;
    private double cancelRate;
    private long avgOrderValue;
    private long maxOrderValue;

    // Getters
    public List<PieDataEntry> getPieData() { return pieData; }
    public double getSuccessRate() { return successRate; }
    public double getCancelRate() { return cancelRate; }
    public long getAvgOrderValue() { return avgOrderValue; }
    public long getMaxOrderValue() { return maxOrderValue; }

    // Inner class cho mảng pieData
    public static class PieDataEntry {
        private String label;
        private int value;

        public String getLabel() { return label; }
        public int getValue() { return value; }
    }
}