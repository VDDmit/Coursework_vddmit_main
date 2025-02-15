package com.smartcore.coursework.controller.api;

import com.smartcore.coursework.mail.EmailService;
import com.smartcore.coursework.service.AppUserAndTokenService;
import com.smartcore.coursework.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API for user registration and authorization")
public class AuthController {

    private final AuthService authService;
    private final AppUserAndTokenService appUserAndTokenService;
    private final EmailService emailService;

    @Operation(
            summary = "User registration",
            description = "Registration of a new user by the administrator. Access Level: HIGH"
    )
    @PostMapping("/register")
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.username, T(com.smartcore.coursework.model.AccessLevel).HIGH)")
    public ResponseEntity<?> register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String roleName) {
        try {
            authService.register(username, email, password, roleName);

            String subject = "Your Account Has Been Created";
            String body = "<h1>Welcome, " + username + "!</h1>" +
                    "<p>Your account has been created. Please log in using the following credentials:</p>" +
                    "<ul>" +
                    "<li><b>Username:</b> " + username + "</li>" +
                    "<li><b>Password:</b> " + password + "</li>" +
                    "</ul>" +
                    "<p>Thanks,<br>Smart Core</p>";

            emailService.sendHtmlEmail(email, subject, body);

            return ResponseEntity.ok().body("User registered successfully: " + appUserAndTokenService.getAppUserByEmail(email));
        } catch (RuntimeException e) {
            log.error("Error during registration: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "User authorization", description = "Log in and receive access and refresh tokens.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        log.info("Attempting to authenticate user: {}", request);

        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            log.warn("Error: Empty username or password");
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
        }

        try {
            log.info("Before authentication: username={} password={}", username, password);
            Map<String, String> tokens = authService.authenticate(username, password);
            log.info("Tokens successfully created for {}: {}", username, tokens);
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            log.error("Authentication error: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid username or password"));
        }
    }

    @Operation(
            summary = "Refresh access token",
            description = "Get a new access token using a refresh token"
    )
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestParam String refreshToken) {
        try {
            return ResponseEntity.ok(authService.refreshAccessToken(refreshToken));
        } catch (RuntimeException e) {
            log.error("Error during access token refresh: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Logout",
            description = "Revoke refresh token"
    )
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String refreshToken) {
        log.info("Attempting to log out, refreshToken: {}", refreshToken);

        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("Error: Empty refresh token");
            return ResponseEntity.badRequest().body("Invalid refresh token");
        }

        try {
            authService.logout(refreshToken);
            log.info("Logout successful");
            return ResponseEntity.ok().body("Logged out successfully");
        } catch (RuntimeException e) {
            log.error("Error during logout: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}