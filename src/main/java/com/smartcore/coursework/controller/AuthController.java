package com.smartcore.coursework.controller;

import com.smartcore.coursework.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
