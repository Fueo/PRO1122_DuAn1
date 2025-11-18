package com.example.fa25_duan1.model;

public class OrderItem {

    private String name;      // Tên sách
    private int quantity;     // Số lượng
    private String amount;    // Thành tiền: "75.000 VND"
    private int imageResId;   // R.drawable.xxx (nếu chưa có ảnh server)

    public OrderItem(String name, int quantity, String amount, int imageResId) {
        this.name = name;
        this.quantity = quantity;
        this.amount = amount;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getAmount() {
        return amount;
    }

    public int getImageResId() {
        return imageResId;
    }
}

