package com.luxeglow.jewelrybackend.dto;

public class AuthResponse {

    private String token;
    private String name;
    private String email;
    private String role;

    public AuthResponse() {
    }

    public AuthResponse(String token, String name, String email, String role) {
        this.token = token;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String username) {
        this.name = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String emailOrUsername) {
        this.email = emailOrUsername;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}