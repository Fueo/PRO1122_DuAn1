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
    private double total;

    @SerializedName("status")
    private String status;

    @SerializedName(value = "createAt", alternate = {"createdAt", "date"})
    private String date;

    // --- CÁC THUỘC TÍNH MỚI CẬP NHẬT ---

    // 1. Mã giao dịch (Quan trọng cho QR Code)
    @SerializedName("transactionCode")
    private String transactionCode;

    // 2. Trạng thái đã thanh toán hay chưa (Quan trọng cho Polling)
    @SerializedName("isPaid")
    private boolean isPaid;

    // -------------------------------------

    @SerializedName("orderDetails")
    private List<OrderDetail> orderDetails;

    // Biến UI (không map từ server)
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

    // Getter cho thuộc tính mới
    public String getTransactionCode() { return transactionCode; }
    public boolean isPaid() { return isPaid; }

    public List<OrderDetail> getOrderDetails() { return orderDetails; }

    public boolean isExpanded() { return expanded; }
    public void setExpanded(boolean expanded) { this.expanded = expanded; }
}