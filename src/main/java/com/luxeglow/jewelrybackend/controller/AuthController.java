package com.luxeglow.jewelrybackend.controller;

import com.luxeglow.jewelrybackend.dto.AuthResponse;
import com.luxeglow.jewelrybackend.dto.UserLoginRequest;
import com.luxeglow.jewelrybackend.dto.UserSignupRequest;
import com.luxeglow.jewelrybackend.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://127.0.0.1:5173",
        "https://fashion-jewelry-store-frontend.vercel.app"
})
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody UserSignupRequest request) {
        return userService.signup(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody UserLoginRequest request) {
        return userService.login(request);
    }
}