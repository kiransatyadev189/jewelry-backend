package com.luxeglow.jewelrybackend.service;

import com.luxeglow.jewelrybackend.entity.PasswordResetToken;
import com.luxeglow.jewelrybackend.entity.User;
import com.luxeglow.jewelrybackend.repository.PasswordResetTokenRepository;
import com.luxeglow.jewelrybackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public PasswordResetService(
            PasswordResetTokenRepository passwordResetTokenRepository,
            UserRepository userRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder
    ) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public void forgotPassword(String email) {
        if (email == null || email.trim().isEmpty()) {
            return;
        }

        String cleanedEmail = email.trim();

        Optional<User> optionalUser = userRepository.findByEmail(cleanedEmail);

        // Do not reveal whether email exists
        if (optionalUser.isEmpty()) {
            return;
        }

        passwordResetTokenRepository.deleteByEmail(cleanedEmail);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);

        PasswordResetToken resetToken = new PasswordResetToken(token, cleanedEmail, expiryDate);
        passwordResetTokenRepository.save(resetToken);

        String resetLink = frontendUrl + "/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(cleanedEmail, resetLink);
    }

    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        Optional<PasswordResetToken> optionalToken = passwordResetTokenRepository.findByToken(token.trim());

        if (optionalToken.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = optionalToken.get();
        return resetToken.getExpiryDate().isAfter(LocalDateTime.now());
    }

    public void resetPassword(String token, String newPassword) {
        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("Invalid token");
        }

        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }

        String cleanedToken = token.trim();
        String cleanedPassword = newPassword.trim();

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(cleanedToken)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token has expired");
        }

        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(cleanedPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }
}