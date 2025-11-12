package com.example.fa25_duan1.model;

public class MenuItem {
    private int iconResId, id;
    private String title;

    public MenuItem(int id, int iconResId, String title) {
        this.id = id;
        this.iconResId = iconResId;
        this.title = title;
    }

    public int getId() {return id;}

    public void setId(int id) {this.id = id;}

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
