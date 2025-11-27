package com.example.fa25_duan1.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {

    @SerializedName("_id")
    private String id;

    @SerializedName("fullname")
    private String fullname;

    @SerializedName("address")
    private String address;

    @SerializedName("phone")
    private String phone;

    @SerializedName("note")
    private String note;

    @SerializedName("paymentmethod")
    private String paymentMethod;

    @SerializedName("total")
    private double total; // Backend trả về số (Number), nên dùng double hoặc long

    @SerializedName("status")
    private String status;

    @SerializedName("createAt")
    private String date; // Backend trả về chuỗi ISO date "2025-11-26T..."

    // Đây là phần quan trọng nhất: Danh sách chi tiết được lồng bên trong
    @SerializedName("orderDetails")
    private List<OrderDetail> orderDetails;

    // Biến này dùng cho UI (xổ ra/thu vào), không gửi lên server
    private boolean expanded = false;

    public Order() {
    }

    // --- Getters ---

    public String getId() { return id; }
    public String getFullname() { return fullname; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getNote() { return note; }
    public String getPaymentMethod() { return paymentMethod; }
    public double getTotal() { return total; }
    public String getStatus() { return status; }
    public String getDate() { return date; }
    public List<OrderDetail> getOrderDetails() { return orderDetails; }

    public boolean isExpanded() { return expanded; }
    public void setExpanded(boolean expanded) { this.expanded = expanded; }
}