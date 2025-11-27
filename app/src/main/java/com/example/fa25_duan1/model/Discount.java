package com.example.fa25_duan1.model;

import java.io.Serializable;

public class Discount implements Serializable {
    private String _id;
    private String discountName;
    private double discountRate;
    private String startDate; // API trả về chuỗi ISO date
    private String endDate;
    private String createAt;

    public Discount() {
    }

    public Discount(String discountName, double discountRate, String startDate, String endDate, String createAt) {
        this.discountName = discountName;
        this.discountRate = discountRate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createAt = createAt;
    }

    // Constructor dùng cho việc tạo mới (thường không gửi createAt lên server vì server tự tạo)
    public Discount(String discountName, double discountRate, String startDate, String endDate) {
        this.discountName = discountName;
        this.discountRate = discountRate;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }

    public String getDiscountName() { return discountName; }
    public void setDiscountName(String discountName) { this.discountName = discountName; }

    public double getDiscountRate() { return discountRate; }
    public void setDiscountRate(double discountRate) { this.discountRate = discountRate; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    // Getter & Setter cho createAt
    public String getCreateAt() { return createAt; }
    public void setCreateAt(String createAt) { this.createAt = createAt; }
}