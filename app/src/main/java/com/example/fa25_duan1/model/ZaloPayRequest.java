package com.example.fa25_duan1.model;

public class ZaloPayRequest {
    private String orderId;

    public ZaloPayRequest(String orderId) {
        this.orderId = orderId;
    }
    // Getter & Setter nếu cần


    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}