package com.smartcore.coursework.controller.api;

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

import java.util.*;
import java.util.stream.Collectors;

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
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.username, T(com.smartcore.coursework.model.AccessLevel).LOW)")
    @GetMapping("/me")
    public ResponseEntity<AppUser> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        log.info("Fetching current user by username: {}", username);
        AppUser currentUser = appUserAndTokenService.getAppUserByUsername(username);
        log.info("Current user retrieved: {}", currentUser);
        return ResponseEntity.ok(currentUser);
    }


    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.username, T(com.smartcore.coursework.model.AccessLevel).LOW)")
    @GetMapping("/list")
    @Operation(summary = "Get a list of all users",
            description = "Returns a list of all registered users. Available to users with Low and higher access level.")
    public ResponseEntity<List<AppUser>> getAllUsers() {
        List<AppUser> appUserList = appUserAndTokenService.getAllAppUsers();
        return ResponseEntity.ok(appUserList);
    }

    @Operation(
            summary = "Get the list of users without team",
            description = "Returns the list of AppUsers without team. Access Level: LOW"
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.username, T(com.smartcore.coursework.model.AccessLevel).LOW)")
    @GetMapping("/list-users-without-team")
    public ResponseEntity<List<AppUser>> getUsersWithoutTeam() {
        List<AppUser> appUserWithoutTeamList = appUserAndTokenService.getAllAppUsers().stream()
                .filter(appUser -> appUser.getTeam() == null)
                .collect(Collectors.toList());

        return ResponseEntity.ok(appUserWithoutTeamList);
    }

    @Operation(
            summary = "Update XP and level",
            description = "Adds XP to the user, checks if they can level up, and updates their level accordingly."
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.username, T(com.smartcore.coursework.model.AccessLevel).LOW)")
    @PostMapping("/update-xp")
    public ResponseEntity<AppUser> updateXp(
            Authentication authentication,
            @RequestParam int xp) {

        String username = authentication.getName();
        log.info("Adding {} XP to user: {}", xp, username);

        AppUser user = appUserAndTokenService.getAppUserByUsername(username);
        if (user == null) {
            log.warn("User not found: {}", username);
            return ResponseEntity.notFound().build();
        }

        user.setXp(user.getXp() + xp);

        while (user.getXp() >= user.getLvl() * 1000) {
            user.setXp(user.getXp() - user.getLvl() * 1000);
            user.setLvl(user.getLvl() + 1);
            log.info("User {} leveled up! New level: {}", username, user.getLvl());
        }

        appUserAndTokenService.save(user);
        return ResponseEntity.ok(user);
    }


    @Operation(
            summary = "Get a top of 9 users, including the current",
            description = "Returns a list of up to 9 users, including the current user, with their ranks in the general leaderboard. "
                    + "Users are sorted by level (LVL) in descending order, then by experience (XP) in descending order. "
                    + "If there are fewer than 9 users, all available users are returned. "
                    + "The current user is always included. Access: LOW."
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.username, T(com.smartcore.coursework.model.AccessLevel).LOW)")
    @GetMapping("/me_in_top_list")
    public ResponseEntity<List<Map<String, Object>>> getCurrentUserInTopList(Authentication authentication) {
        List<AppUser> appUserList = appUserAndTokenService.getAllAppUsers();

        appUserList.sort(Comparator.comparingInt(AppUser::getLvl).reversed()
                .thenComparing(AppUser::getXp).reversed());

        String username = authentication.getName();
        AppUser currentUser = appUserAndTokenService.getAppUserByUsername(username);

        int currentUserIndex = appUserList.indexOf(currentUser);

        List<Map<String, Object>> topUsersWithRanks = getTopUsersWithRanks(appUserList, currentUserIndex);

        return ResponseEntity.ok(topUsersWithRanks);
    }

    private static List<Map<String, Object>> getTopUsersWithRanks(List<AppUser> appUserList, int currentUserIndex) {
        int totalUsers = appUserList.size();
        int startIndex = Math.max(currentUserIndex - 4, 0);
        int endIndex = Math.min(startIndex + 9, totalUsers);

        if (endIndex - startIndex < 9 && startIndex > 0) {
            startIndex = Math.max(endIndex - 9, 0);
        }

        List<Map<String, Object>> topUsersWithRanks = new ArrayList<>();
        for (int i = startIndex; i < endIndex; i++) {
            AppUser user = appUserList.get(i);
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("rank", i + 1);
            userInfo.put("user", user);
            topUsersWithRanks.add(userInfo);
        }
        return topUsersWithRanks;
    }


    @Operation(
            summary = "Get user by ID",
            description = "Returns user data based on his ID. Access Level: HIGH"
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.username, T(com.smartcore.coursework.model.AccessLevel).HIGH)")
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
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.username, T(com.smartcore.coursework.model.AccessLevel).MEDIUM)")
    @PutMapping("/me")
    public ResponseEntity<String> updateCurrentUser(@RequestBody AppUser updatedUser, Authentication authentication) {
        String username = authentication.getName();
        log.info("Updating current user with username: {}", username);

        AppUser currentUser = appUserAndTokenService.getAppUserByUsername(username);

        if (updatedUser.getUsername() != null) {
            log.info("Updating username from {} to {}", currentUser.getUsername(), updatedUser.getUsername());
            currentUser.setUsername(updatedUser.getUsername());
        }
        if (updatedUser.getPassword() != null) {
            log.info("Updating password for user with username: {}", username);
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
        log.info("User profile updated successfully for username: {}", username);
        return ResponseEntity.ok("User profile updated successfully.");
    }
}
