package com.smartcore.coursework.controller;

import com.smartcore.coursework.mail.EmailService;
import com.smartcore.coursework.service.AppUserAndTokenService;
import com.smartcore.coursework.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.id, T(com.smartcore.coursework.model.AccessLevel).HIGH)")
    public ResponseEntity<?> register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String roleName) {
        try {
            authService.register(username, email, password, roleName);

            String subject = "Your Account Has Been Created";
            String body = "<h1>Welcome, " + username + "!</h1>" +
                    "<p>Your account has been created. Please login using the following credentials:</p>" +
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

    @Operation(
            summary = "User authorization",
            description = "Log in and receive access and refresh tokens. No Access Level."
    )
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