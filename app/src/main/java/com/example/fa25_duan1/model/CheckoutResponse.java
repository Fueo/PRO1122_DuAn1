package com.example.fa25_duan1.model;

import com.google.gson.annotations.SerializedName;
public class CheckoutResponse {
    @SerializedName("orderId")
    private String orderId;
    private long total;
    private String transactionCode; // Server trả về mã này (VD: CB123456)
    private String paymentMethod;

    public String getOrderId() { return orderId; }
    public long getTotal() { return total; }
    public String getTransactionCode() { return transactionCode; }
    public String getPaymentMethod() { return paymentMethod; }
}