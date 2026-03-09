package com.arcotcabs.arcotcabs_backend.model;


import lombok.Data;

@Data
public class User {

    private String userId;
    private String name;
    private String email;
    private String phone;
    private String passwordHash;
    private String role;

    private Long createdAt;



    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }



    public void setRole(String role) {
        this.role = role;
    }


    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }


    public String getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }
}
