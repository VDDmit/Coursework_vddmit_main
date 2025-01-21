package com.smartcore.coursework.controller;

import com.smartcore.coursework.model.AppUser;
import com.smartcore.coursework.service.AppUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API for user management")
public class AppUsersController {

    private final AppUserService appUserService;

    @Operation(summary = "Get the current user", description = "Returns the current user's data based on the token")
    @GetMapping("/me")
    public ResponseEntity<AppUser> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        log.info("Fetching current user by email: {}", email);
        AppUser currentUser = appUserService.getAppUserByEmail(email);
        log.info("Current user retrieved: {}", currentUser);
        return ResponseEntity.ok(currentUser);
    }

    @Operation(summary = "Get user by ID", description = "Returns user data based on his ID")
    @GetMapping("/{id}")
    public ResponseEntity<AppUser> getUserById(@PathVariable String id) {
        log.info("Fetching user by ID: {}", id);
        AppUser user = appUserService.getAppUserById(id);
        log.info("User retrieved: {}", user);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Update the current user", description = "Allows you to update the current user's data")
    @PutMapping("/me")
    public ResponseEntity<String> updateCurrentUser(@RequestBody AppUser updatedUser) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        log.info("Updating current user with email: {}", email);

        AppUser currentUser = appUserService.getAppUserByEmail(email);

        if (updatedUser.getUsername() != null) {
            log.info("Updating username from {} to {}", currentUser.getUsername(), updatedUser.getUsername());
            currentUser.setUsername(updatedUser.getUsername());
        }
        if (updatedUser.getPassword() != null) {
            log.info("Updating password for user with email: {}", email);
            currentUser.setPassword(updatedUser.getPassword());
        }
        if (updatedUser.getEmail() != null) {
            log.info("Updating email from {} to {}", currentUser.getEmail(), updatedUser.getEmail());
            currentUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getTeam() != null) {
            log.info("Updating team for user {} to {}", currentUser.getUsername(), updatedUser.getTeam());
            currentUser.setTeam(updatedUser.getTeam());
        }
        if (updatedUser.getXp() != null) {
            log.info("Updating XP for user {} from {} to {}", currentUser.getUsername(), currentUser.getXp(), updatedUser.getXp());
            currentUser.setXp(updatedUser.getXp());
        }

        appUserService.save(currentUser);
        log.info("User profile updated successfully for email: {}", email);
        return ResponseEntity.ok("User profile updated successfully.");
    }
}