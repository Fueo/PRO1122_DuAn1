package com.example.fa25_duan1.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("_id")
    private String userID;
    private int role;
    private String username;
    private String password;
    private String name;
    private String avatar;
    private String email;
    @SerializedName("createAt")
    private String createAt;

    // --- MỚI THÊM ---
    private boolean isVerifiedEmail;

    public User() { }

    // Getter & Setter
    public String getUserID() { return userID; }
    public void setUserID(String userID) { this.userID = userID; }
    public int getRole() { return role; }
    public void setRole(int role) { this.role = role; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCreateAt() { return createAt; }
    public void setCreateAt(String createAt) { this.createAt = createAt; }

    // Getter Setter mới
    public boolean isVerifiedEmail() { return isVerifiedEmail; }
    public void setVerifiedEmail(boolean verifiedEmail) { isVerifiedEmail = verifiedEmail; }
}