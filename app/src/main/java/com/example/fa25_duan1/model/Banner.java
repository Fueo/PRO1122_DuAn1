package com.example.fa25_duan1.model;

public class Banner {
    private String title;
    private String author;
    private int imageResId;

    public Banner(String title, String author, int imageResId) {
        this.title = title;
        this.author = author;
        this.imageResId = imageResId;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getImageResId() { return imageResId; }
}