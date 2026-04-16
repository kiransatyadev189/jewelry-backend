package com.luxeglow.jewelrybackend.controller;

import com.luxeglow.jewelrybackend.dto.AuthResponse;
import com.luxeglow.jewelrybackend.dto.ForgotPasswordRequest;
import com.luxeglow.jewelrybackend.dto.ResetPasswordRequest;
import com.luxeglow.jewelrybackend.dto.UserLoginRequest;
import com.luxeglow.jewelrybackend.dto.UserSignupRequest;
import com.luxeglow.jewelrybackend.service.PasswordResetService;
import com.luxeglow.jewelrybackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://127.0.0.1:5173",
        "https://fashion-jewelry-store-frontend.vercel.app"
})
public class AuthController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    public AuthController(UserService userService, PasswordResetService passwordResetService) {
        this.userService = userService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody UserSignupRequest request) {
        return userService.signup(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody UserLoginRequest request) {
        return userService.login(request);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request.getEmail());

        return ResponseEntity.ok(
                Map.of("message", "If this email exists, a reset link has been sent.")
        );
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<Map<String, Object>> validateResetToken(@RequestParam String token) {
        boolean valid = passwordResetService.validateToken(token);
        return ResponseEntity.ok(Map.of("valid", valid));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }
}