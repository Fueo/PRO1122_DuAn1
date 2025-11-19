package com.example.fa25_duan1.model;

import com.google.gson.annotations.SerializedName;

public class Author {

    @SerializedName("_id")
    private String authorID;  // đổi int -> String
    private String name;
    private String description;
    private String avatar;
    @SerializedName("createAt")
    private String createAt;
    public Author() {
    }

    public Author(String authorID, String name, String description, String avatar, String createAt) {
        this.authorID = authorID;
        this.name = name;
        this.description = description;
        this.avatar = avatar;
        this.createAt = createAt;
    }

    public String getAuthorID() {
        return authorID;
    }

    public void setAuthorID(String authorID) {
        this.authorID = authorID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }
}
