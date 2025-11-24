package com.example.fa25_duan1.model;

public class CartRequest {
    private String productId;
    private int quantity;

    public CartRequest(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}