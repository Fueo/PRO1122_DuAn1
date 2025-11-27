package com.example.fa25_duan1.model;

import com.google.gson.annotations.SerializedName;

public class CartItem {
    @SerializedName("_id")
    private String id;

    @SerializedName("productId")
    private Product product;

    @SerializedName("quantity")
    private int quantity;

    // 🔹 MỚI: Nhận giá tiền được lưu trong collection Cart
    @SerializedName("price")
    private double price;

    public CartItem() {
    }

    // --- Getter & Setter chuẩn ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Setter cho price (nếu cần thiết)
    public void setPrice(double price) {
        this.price = price;
    }

    // --- SMART GETTERS (Dùng cho Adapter) ---

    public String getTitle() {
        return (product != null) ? product.getName() : "Sản phẩm lỗi/Ngừng kinh doanh";
    }

    // 🔹 CẬP NHẬT: Lấy giá từ bảng Cart. Nếu = 0 (data cũ) thì lấy từ Product
    public double getPrice() {
        if (price > 0) {
            return price;
        }
        return (product != null) ? product.getPrice() : 0;
    }

    public String getImageUrl() {
        return (product != null) ? product.getImage() : "";
    }
}