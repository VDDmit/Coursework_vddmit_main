package com.smartcore.coursework.controller;

import com.smartcore.coursework.dto.TeamAchievementRequestDTO;
import com.smartcore.coursework.dto.TeamWithAchievementPointsDTO;
import com.smartcore.coursework.model.TeamAchievement;
import com.smartcore.coursework.service.gamification.TeamAchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Tag(name = "Team Achievements", description = "API for managing team achievement points")
public class TeamAchievementController {

    private final TeamAchievementService teamAchievementService;

    @Operation(
            summary = "Get teams with achievement points",
            description = "Returns a score table of all teams with their total achievement points. Access Level: LOW"
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.username, T(com.smartcore.coursework.model.AccessLevel).LOW)")
    @GetMapping("/achievement-points-score-table")
    public ResponseEntity<List<TeamWithAchievementPointsDTO>> getTeamAchievementPointsScoreTable() {
        log.info("Fetching the score table of teams with achievement points.");
        List<TeamWithAchievementPointsDTO> teamsWithPoints = teamAchievementService.getAllTeamsWithAchievementPointsSortedDescending();
        log.info("Score table retrieved. Number of teams: {}", teamsWithPoints.size());
        return ResponseEntity.ok(teamsWithPoints);
    }

    @Operation(
            summary = "Create a new team achievement",
            description = "Allows the creation of a new team achievement. Access Level: HIGH"
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.username, T(com.smartcore.coursework.model.AccessLevel).HIGH)")
    @PostMapping("/achievement")
    public ResponseEntity<TeamAchievement> createTeamAchievement(@RequestBody TeamAchievementRequestDTO request) {
        log.info("Creating a new team achievement for team: {}", request.getTeamName());
        try {
            TeamAchievement teamAchievement = teamAchievementService.createTeamAchievement(request);
            log.info("Team achievement created successfully: {}", teamAchievement);
            return ResponseEntity.ok(teamAchievement);
        } catch (EntityNotFoundException e) {
            log.error("Failed to create team achievement: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}