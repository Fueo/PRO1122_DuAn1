// AuthResponse.java
package com.example.fa25_duan1.model.Auth;

import com.example.fa25_duan1.model.User;

public class AuthResponse {
    private String message;
    private String accessToken;
    private String refreshToken;
    private User user;

    // Getter & Setter

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
