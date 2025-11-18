package com.example.fa25_duan1.model;

public class Category {
    private String name;
    private boolean isSelected;

    public Category(String name, boolean isSelected) {
        this.name = name;
        this.isSelected = isSelected;
    }

    public String getName() { return name; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}
