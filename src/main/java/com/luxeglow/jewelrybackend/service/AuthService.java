package com.luxeglow.jewelrybackend.service;

import com.luxeglow.jewelrybackend.dto.AuthResponse;
import com.luxeglow.jewelrybackend.dto.UserLoginRequest;
import com.luxeglow.jewelrybackend.dto.UserSignupRequest;
import com.luxeglow.jewelrybackend.entity.User;
import com.luxeglow.jewelrybackend.repository.UserRepository;
import com.luxeglow.jewelrybackend.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final PasswordResetService passwordResetService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       PasswordResetService passwordResetService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.passwordResetService = passwordResetService;
    }

    public String signup(UserSignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                "ROLE_USER"
        );

        userRepository.save(user);
        return "Signup successful";
    }

    public AuthResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        return new AuthResponse(
                token,
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    public String forgotPassword(String email) {
        passwordResetService.forgotPassword(email);
        return "If this email exists, a reset link has been sent.";
    }

    public String resetPassword(String token, String newPassword) {
        passwordResetService.resetPassword(token, newPassword);
        return "Password reset successful";
    }

    public boolean validateResetToken(String token) {
        return passwordResetService.validateToken(token);
    }
}