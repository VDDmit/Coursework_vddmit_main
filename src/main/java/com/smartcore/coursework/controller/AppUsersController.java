package com.smartcore.coursework.controller;

import com.smartcore.coursework.model.AppUser;
import com.smartcore.coursework.service.AppUserAndTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API for user management")
public class AppUsersController {

    private final AppUserAndTokenService appUserAndTokenService;

    @Operation(
            summary = "Get the current user",
            description = "Returns the current user's data based on the token. Access Level: LOW"
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.id, T(com.smartcore.coursework.model.AccessLevel).LOW)")
    @GetMapping("/me")
    public ResponseEntity<AppUser> getCurrentUser(Authentication authentication) {
        String userId = authentication.getPrincipal().toString();
        log.info("Fetching current user by ID: {}", userId);
        AppUser currentUser = appUserAndTokenService.getAppUserById(userId);
        log.info("Current user retrieved: {}", currentUser);
        return ResponseEntity.ok(currentUser);
    }

    @Operation(
            summary = "Get user by ID",
            description = "Returns user data based on his ID. Access Level: HIGH"
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.id, T(com.smartcore.coursework.model.AccessLevel).HIGH)")
    @GetMapping("/{id}")
    public ResponseEntity<AppUser> getUserById(@PathVariable String id) {
        log.info("Fetching user by ID: {}", id);
        AppUser user = appUserAndTokenService.getAppUserById(id);
        log.info("User retrieved: {}", user);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "Update the current user",
            description = "Allows you to update the current user's data. Access Level: MEDIUM"
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.id, T(com.smartcore.coursework.model.AccessLevel).MEDIUM)")
    @PutMapping("/me")
    public ResponseEntity<String> updateCurrentUser(@RequestBody AppUser updatedUser, Authentication authentication) {
        String userId = authentication.getPrincipal().toString();
        log.info("Updating current user with ID: {}", userId);

        AppUser currentUser = appUserAndTokenService.getAppUserById(userId);

        if (updatedUser.getUsername() != null) {
            log.info("Updating username from {} to {}", currentUser.getUsername(), updatedUser.getUsername());
            currentUser.setUsername(updatedUser.getUsername());
        }
        if (updatedUser.getPassword() != null) {
            log.info("Updating password for user with ID: {}", userId);
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

        appUserAndTokenService.save(currentUser);
        log.info("User profile updated successfully for ID: {}", userId);
        return ResponseEntity.ok("User profile updated successfully.");
    }
}