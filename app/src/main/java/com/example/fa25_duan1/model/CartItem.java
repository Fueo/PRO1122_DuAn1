package com.example.fa25_duan1.model;

import com.google.gson.annotations.SerializedName;

public class CartItem {
    @SerializedName("_id")
    private String id; // ID của dòng trong giỏ hàng

    @SerializedName("productId")
    private Product product; // Object Product chi tiết từ Backend

    @SerializedName("quantity")
    private int quantity;

    public CartItem() {
    }

    // --- Getter & Setter ---

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

    // --- SMART GETTERS (Để Adapter cũ không bị lỗi) ---

    public String getTitle() {
        return (product != null) ? product.getName() : "Sản phẩm lỗi/Ngừng kinh doanh";
    }

    public double getPrice() {
        return (product != null) ? product.getPrice() : 0;
    }

    public String getImageUrl() {
        return (product != null) ? product.getImage() : "";
    }
}