package com.luxeglow.jewelrybackend.controller;

import com.luxeglow.jewelrybackend.dto.AuthResponse;
import com.luxeglow.jewelrybackend.dto.ForgotPasswordRequest;
import com.luxeglow.jewelrybackend.dto.ResetPasswordRequest;
import com.luxeglow.jewelrybackend.dto.UserLoginRequest;
import com.luxeglow.jewelrybackend.dto.UserSignupRequest;
import com.luxeglow.jewelrybackend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://fashion-jewelry-store-frontend.vercel.app"
})
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserSignupRequest request) {
        try {
            String response = authService.signup(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        try {
            String response = authService.forgotPassword(request.getEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        boolean valid = authService.validateResetToken(token);
        return ResponseEntity.ok(Map.of("valid", valid));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (request.getToken() == null || request.getToken().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Token is required");
        }

        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("New password is required");
        }

        try {
            String response = authService.resetPassword(
                    request.getToken(),
                    request.getNewPassword()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}