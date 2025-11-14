package com.example.fa25_duan1.model;

// model/CartItem.java


import java.util.Objects;

public class CartItem {
    private String id;
    private String title;
    private double price;
    private int quantity;
    private String imageUrl;

    public CartItem(String id, String title, double price, int quantity, String imageUrl) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getImageUrl() { return imageUrl; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
