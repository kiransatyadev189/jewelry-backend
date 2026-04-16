package com.luxeglow.jewelrybackend.service;

import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public String forgotPassword(String email) {
        System.out.println("Forgot password requested for: " + email);
        return "If this email exists, a reset link has been sent.";
    }

    public String resetPassword(String token, String newPassword) {
        System.out.println("Reset password requested with token: " + token);
        return "Password reset successful";
    }
}