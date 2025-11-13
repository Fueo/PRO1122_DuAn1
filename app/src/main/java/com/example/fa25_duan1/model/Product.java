package com.example.fa25_duan1.model; // Thay bằng tên package của bạn

public class Product {
    private String title;
    private String author;
    private String currentPrice;
    private String originalPrice;
    private String discountPercent;
    private int imageResId;

    public Product(String title, String author, String currentPrice, String originalPrice, String discountPercent, int imageResId) {
        this.title = title;
        this.author = author;
        this.currentPrice = currentPrice;
        this.originalPrice = originalPrice;
        this.discountPercent = discountPercent;
        this.imageResId = imageResId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getCurrentPrice() {
        return currentPrice;
    }

    public String getOriginalPrice() {
        return originalPrice;
    }

    public String getDiscountPercent() {
        return discountPercent;
    }

    public int getImageResId() {
        return imageResId;
    }
}