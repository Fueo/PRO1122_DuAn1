package com.example.fa25_duan1.model;

public class User {
    private int userID, role, avatarId;
    private String username, password, name, avatar, address, phone, email, createAt;

    public User() {
    }

    public User(int userID, int role, String username, String password, String name, String avatar, String address, String phone, String email, String createAt) {
        this.userID = userID;
        this.role = role;
        this.username = username;
        this.password = password;
        this.name = name;
        this.avatar = avatar;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.createAt = createAt;
    }

    public User(int role, String username, String password, String name, String avatar, String address, String phone, String email, String createAt) {
        this.role = role;
        this.username = username;
        this.password = password;
        this.name = name;
        this.avatar = avatar;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.createAt = createAt;
    }

    public User(int userID, String password, String username) {
        this.userID = userID;
        this.password = password;
        this.username = username;
    }

    public int getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(int avatarId) {
        this.avatarId = avatarId;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }
}
