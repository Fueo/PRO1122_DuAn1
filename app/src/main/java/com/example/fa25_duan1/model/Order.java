package com.example.fa25_duan1.model;

import java.util.List;
public class Order {

    private String orderCode;
    private String paymentMethod;
    private String total;
    private String status;      // "processing", "done", "canceled"
    private String date;        // "9:42 11/10/2025"
    private String address;
    private String phone;

    private List<OrderItem> productList;

    private boolean expanded = false; // dùng cho xổ/thu chi tiết

    public Order(String orderCode,
                 String paymentMethod,
                 String total,
                 String status,
                 String date,
                 String address,
                 String phone,
                 List<OrderItem> productList) {

        this.orderCode = orderCode;
        this.paymentMethod = paymentMethod;
        this.total = total;
        this.status = status;
        this.date = date;
        this.address = address;
        this.phone = phone;
        this.productList = productList;
    }

    public String getOrderCode() { return orderCode; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getTotal() { return total; }
    public String getStatus() { return status; }
    public String getDate() { return date; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public List<OrderItem> getProductList() { return productList; }

    public boolean isExpanded() { return expanded; }
    public void setExpanded(boolean expanded) { this.expanded = expanded; }
}
