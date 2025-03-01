package com.smartcore.coursework.controller.api;

import com.smartcore.coursework.dto.UserMerchAchievementDTO;
import com.smartcore.coursework.model.AppUser;
import com.smartcore.coursework.model.UserMerchAchievement;
import com.smartcore.coursework.service.AppUserAndTokenService;
import com.smartcore.coursework.service.gamification.UserMerchAchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/merch")
@RequiredArgsConstructor
@Tag(name = "User Merch Achievements", description = "Operations related to user merchandise achievements")
public class UserMerchAchievementController {
    private final UserMerchAchievementService userMerchAchievementService;
    private final AppUserAndTokenService appUserAndTokenService;

    @Operation(
            summary = "Get list of user merchandise achievements",
            description = "Retrieves a list of merchandise achievements for the specified user. If no username is provided, returns achievements for the currently authenticated user. Requires LOW access level."
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.username, T(com.smartcore.coursework.model.AccessLevel).LOW)")
    @GetMapping("/list")
    public ResponseEntity<List<UserMerchAchievement>> getUserMerchAchievements(
            @Parameter(description = "The username of the user whose achievements should be retrieved (optional)")
            @RequestParam(required = false) String username) {

        log.info("Fetching merchandise achievements for user: {}", username != null ? username : "current authenticated user");

        if (username != null && appUserAndTokenService.doesUserExist(username)) {
            List<UserMerchAchievement> achievements = userMerchAchievementService.getAllUserMerchAchievementsByUserUsername(username);
            log.info("Retrieved {} achievements for user: {}", achievements.size(), username);
            return ResponseEntity.ok(achievements);
        }

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        List<UserMerchAchievement> achievements = userMerchAchievementService.getAllUserMerchAchievementsByUserUsername(currentUsername);

        log.info("Retrieved {} achievements for current user: {}", achievements.size(), currentUsername);
        return ResponseEntity.ok(achievements);
    }

    @Operation(
            summary = "Get obtained user achievements",
            description = "Returns only the list of obtained achievements for the authenticated user."
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.username, T(com.smartcore.coursework.model.AccessLevel).LOW)")
    @GetMapping("/obtained")
    public ResponseEntity<List<String>> getObtainedAchievements() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        List<String> obtainedAchievements = userMerchAchievementService
                .getAllUserMerchAchievementsByUserUsername(currentUsername)
                .stream()
                .map(UserMerchAchievement::getName)
                .toList();

        return ResponseEntity.ok(obtainedAchievements);
    }

    @Operation(
            summary = "Request an award from the administrator",
            description = "Sends an email notification to the administrator when a user requests a reward."
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.username, T(com.smartcore.coursework.model.AccessLevel).LOW)")
    @PostMapping("/request-an-administrator-award")
    public ResponseEntity<?> requestAnAdministratorAchievement(
            @RequestParam String username,
            @RequestParam String achievementName
    ) {
        try {
            log.info("User '{}' requested an administrator award '{}'", username, achievementName);

            AppUser user = appUserAndTokenService.getAppUserByUsername(username);
            String userEmail = user.getEmail();

            userMerchAchievementService.notifyAdminAboutAchievementRequest(username, userEmail, achievementName);

            return ResponseEntity.ok("Request for award '" + achievementName + "' has been sent to the administrator.");
        } catch (RuntimeException e) {
            log.error("Error while requesting an award: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @Operation(
            summary = "Issue a new achievement to a user",
            description = "Grants a new merchandise achievement to the specified user. Requires HIGH access level."
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.username, T(com.smartcore.coursework.model.AccessLevel).HIGH)")
    @PostMapping("/issue")
    public ResponseEntity<UserMerchAchievement> issueAchievement(
            @Parameter(description = "The username of the user who will receive the achievement", required = true)
            @RequestParam String username,
            @Parameter(description = "The achievement details", required = true)
            @RequestBody UserMerchAchievementDTO achievementDTO) {

        log.info("Issuing achievement '{}' to user '{}'", achievementDTO.getName(), username);

        UserMerchAchievement issuedAchievement = userMerchAchievementService.issueTheAchievement(username, achievementDTO);

        log.info("Successfully issued achievement '{}' to user '{}'", issuedAchievement.getName(), username);
        return ResponseEntity.ok(issuedAchievement);
    }
}