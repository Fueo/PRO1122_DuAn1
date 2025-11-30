package com.example.fa25_duan1.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Discount implements Serializable {
    private String _id;
    private String discountName;
    private double discountRate;
    private String startDate;
    private String endDate;
    private String createAt;

    // --- THÊM MỚI CHO BE ---
    // 1. Dùng để GỬI lên server khi thêm/sửa (Backend hứng field này)
    private List<String> productIds;

    // 2. Dùng để NHẬN về khi xem chi tiết (Backend trả field này sau khi populate)
    @SerializedName("appliedProducts")
    private List<Product> appliedProducts;

    public Discount() {
    }

    // Constructor đầy đủ cho việc hiển thị danh sách
    public Discount(String discountName, double discountRate, String startDate, String endDate, String createAt) {
        this.discountName = discountName;
        this.discountRate = discountRate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createAt = createAt;
    }

    // Constructor dùng cho việc tạo mới/update (gửi kèm productIds)
    public Discount(String discountName, double discountRate, String startDate, String endDate) {
        this.discountName = discountName;
        this.discountRate = discountRate;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // --- GETTER & SETTER ---

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

    public String getCreateAt() { return createAt; }
    public void setCreateAt(String createAt) { this.createAt = createAt; }

    // Getter Setter cho trường mới
    public List<String> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<String> productIds) {
        this.productIds = productIds;
    }

    public List<Product> getAppliedProducts() {
        return appliedProducts;
    }

    public void setAppliedProducts(List<Product> appliedProducts) {
        this.appliedProducts = appliedProducts;
    }
}