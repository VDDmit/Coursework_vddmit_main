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
@Tag(name = "Authentication", description = "API для регистрации и авторизации пользователей")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Регистрация пользователя", description = "Регистрация нового пользователя с предоставлением токена доступа")
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

    @Operation(summary = "Авторизация пользователя", description = "Вход в систему с получением токенов доступа и обновления")
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