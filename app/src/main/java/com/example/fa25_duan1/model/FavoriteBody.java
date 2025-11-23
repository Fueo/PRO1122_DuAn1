package com.example.fa25_duan1.model;

public class FavoriteBody {
    private String ProductID;

    public FavoriteBody(String productID) {
        this.ProductID = productID;
    }

    public String getProductID() {
        return ProductID;
    }

    public void setProductID(String productID) {
        this.ProductID = productID;
    }
}