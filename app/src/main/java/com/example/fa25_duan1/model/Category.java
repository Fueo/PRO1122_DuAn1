package com.example.fa25_duan1.model;

import com.google.gson.annotations.SerializedName;

public class Category {
    @SerializedName("cateName")
    private String name;
    @SerializedName("createAt")
    private String createAt;

    @SerializedName("_id")
    private String cateID;

    private boolean isSelected;

    public Category() {
    }

    public Category(String name, boolean isSelected) {
        this.name = name;
        this.isSelected = isSelected;
    }

    public Category(String name, String createAt, String cateID, boolean isSelected) {
        this.name = name;
        this.createAt = createAt;
        this.cateID = cateID;
        this.isSelected = isSelected;
    }

    public String getName() { return name; }

    // Thêm hàm này để khớp với code trong Fragment
    public String get_id() { return cateID; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }
}