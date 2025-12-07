package com.example.fa25_duan1.model;

import com.google.gson.annotations.SerializedName;

public class CheckoutRequest {
    private String fullname;
    private String address;
    private String phone;
    private String note;

    @SerializedName("paymentmethod")
    private String paymentmethod; // Tên biến phải khớp chính xác với req.body backend

    public CheckoutRequest(String fullname, String address, String phone, String note, String paymentmethod) {
        this.fullname = fullname;
        this.address = address;
        this.phone = phone;
        this.note = note;
        this.paymentmethod = paymentmethod;
    }
}
