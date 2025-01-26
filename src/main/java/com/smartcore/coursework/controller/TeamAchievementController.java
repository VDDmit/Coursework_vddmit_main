package com.smartcore.coursework.controller;

import com.smartcore.coursework.dto.TeamWithAchievementPointsDTO;
import com.smartcore.coursework.service.gamification.TeamAchievementScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Tag(name = "Team Achievements", description = "API for managing team achievement points")
public class TeamAchievementController {

    private final TeamAchievementScoreService teamAchievementScoreService;

    @Operation(
            summary = "Get teams with achievement points",
            description = "Returns a score table of all teams with their total achievement points. Access Level: LOW"
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.id, T(com.smartcore.coursework.model.AccessLevel).LOW)")
    @GetMapping("/achievement-points-score-table")
    public ResponseEntity<List<TeamWithAchievementPointsDTO>> getTeamAchievementPointsScoreTable() {
        log.info("Fetching the score table of teams with achievement points.");
        List<TeamWithAchievementPointsDTO> teamsWithPoints = teamAchievementScoreService.getAllTeamsWithAchievementPointsSortedDescending();
        log.info("Score table retrieved. Number of teams: {}", teamsWithPoints.size());
        return ResponseEntity.ok(teamsWithPoints);
    }
}