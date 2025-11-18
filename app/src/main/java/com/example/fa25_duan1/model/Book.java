package com.example.fa25_duan1.model;

public class Book {
    private String title;
    private String author;
    private int imageResId;
    private double salePrice;
    private double originalPrice;
    private String discount;
    private int viewCount;
    private int likeCount;

    public Book(String title, String author, int imageResId, double salePrice, double originalPrice, String discount, int viewCount, int likeCount) {
        this.title = title;
        this.author = author;
        this.imageResId = imageResId;
        this.salePrice = salePrice;
        this.originalPrice = originalPrice;
        this.discount = discount;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getImageResId() { return imageResId; }
    public double getSalePrice() { return salePrice; }
    public double getOriginalPrice() { return originalPrice; }
    public String getDiscount() { return discount; }
    public int getViewCount() { return viewCount; }
    public int getLikeCount() { return likeCount; }
}
