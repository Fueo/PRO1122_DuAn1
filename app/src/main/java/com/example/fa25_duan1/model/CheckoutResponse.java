package com.example.fa25_duan1.model;

import com.google.gson.annotations.SerializedName;
public class CheckoutResponse {
    @SerializedName("orderId")
    private String orderId;
    public String getOrderId() { return orderId; }
}