package com.smartcore.coursework.controller;

import com.smartcore.coursework.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API for user registration and authorization")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "User registration", description = "New user registration with access token provision")
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password) {
        try {
            String token = authService.register(username, email, password);
            return ResponseEntity.ok().body("User registered successfully with token: " + token);
        } catch (RuntimeException e) {
            log.error("Error during registration: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "User authorization", description = "Log in and receive access and refresh tokens")
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestParam String username,
            @RequestParam String password) {
        try {
            Map<String, String> tokens = authService.authenticate(username, password);
            return ResponseEntity.ok().body("Login successful. Access Token: " + tokens.get("accessToken") +
                    ", Refresh Token: " + tokens.get("refreshToken"));
        } catch (RuntimeException e) {
            log.error("Error during login: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}