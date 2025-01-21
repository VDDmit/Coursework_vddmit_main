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
@Tag(name = "Users", description = "API для управления пользователями")
public class AppUsersController {

    private final AppUserService appUserService;

    @Operation(summary = "Получить текущего пользователя", description = "Возвращает данные текущего пользователя на основе токена")
    @GetMapping("/me")
    public ResponseEntity<AppUser> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        AppUser currentUser = appUserService.getAppUserByEmail(email);
        return ResponseEntity.ok(currentUser);
    }

    @Operation(summary = "Получить пользователя по ID", description = "Возвращает данные пользователя на основе его ID")
    @GetMapping("/{id}")
    public ResponseEntity<AppUser> getUserById(@PathVariable String id) {
        AppUser user = appUserService.getAppUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Обновить текущего пользователя", description = "Позволяет обновить данные текущего пользователя")
    @PutMapping("/me")
    public ResponseEntity<String> updateCurrentUser(@RequestBody AppUser updatedUser) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        AppUser currentUser = appUserService.getAppUserByEmail(email);

        if (updatedUser.getUsername() != null) currentUser.setUsername(updatedUser.getUsername());
        if (updatedUser.getPassword() != null) currentUser.setPassword(updatedUser.getPassword());
        if (updatedUser.getEmail() != null) currentUser.setEmail(updatedUser.getEmail());
        if (updatedUser.getTeam() != null) currentUser.setTeam(updatedUser.getTeam());
        if (updatedUser.getXp() != null) currentUser.setXp(updatedUser.getXp());

        appUserService.save(currentUser);
        return ResponseEntity.ok("User profile updated successfully.");
    }
}