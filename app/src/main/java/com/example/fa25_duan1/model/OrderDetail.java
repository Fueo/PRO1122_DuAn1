package com.example.fa25_duan1.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class OrderDetail implements Serializable {

    @SerializedName("_id")
    private String id;

    @SerializedName("orderId")
    private String orderId;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("totalprice")
    private double totalPrice; // Tổng tiền của riêng item này (giá * số lượng)

    // Backend populate product vào đây
    @SerializedName("productId")
    private Product product;

    public OrderDetail() {
    }

    // --- Getters cơ bản ---
    public String getId() { return id; }
    public int getQuantity() { return quantity; }
    public double getTotalPrice() { return totalPrice; }
    public Product getProduct() { return product; }

    // --- SMART GETTERS (Giúp Adapter gọi code ngắn gọn, không lo null) ---

    // Lấy tên sách
    public String getProductName() {
        return (product != null) ? product.getName() : "Sản phẩm không tồn tại";
    }

    // Lấy ảnh sách (URL String)
    public String getProductImage() {
        return (product != null) ? product.getImage() : "";
    }

    // Lấy giá gốc một cuốn
    public double getUnitItemPrice() {
        return (product != null) ? product.getPrice() : 0;
    }
}