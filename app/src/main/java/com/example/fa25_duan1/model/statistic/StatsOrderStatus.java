package com.example.fa25_duan1.model.statistic;

import com.google.gson.annotations.SerializedName;

public class StatsOrderStatus {
    // Sử dụng @SerializedName nếu tên biến trong Java khác tên key trong JSON
    // Hoặc đặt tên giống hệt Backend trả về (Pending, Processing...)
    
    @SerializedName("Pending")
    private int pending;
    
    @SerializedName("Processing")
    private int processing;
    
    @SerializedName("Shipping")
    private int shipping;
    
    @SerializedName("Completed")
    private int completed;
    
    @SerializedName("Cancelled")
    private int cancelled;

    // Getters
    public int getPending() { return pending; }
    public int getProcessing() { return processing; }
    public int getShipping() { return shipping; }
    public int getCompleted() { return completed; }
    public int getCancelled() { return cancelled; }
}