package com.arcotcabs.arcotcabs_backend.dto;

public class AuthResponse {

    private String userId;
    private String role;
    private String token;

    public AuthResponse() {
    }

    public AuthResponse(String userId, String role, String token) {
        this.userId = userId;
        this.role = role;
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}
