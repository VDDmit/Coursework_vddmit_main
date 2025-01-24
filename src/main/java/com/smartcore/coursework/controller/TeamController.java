package com.smartcore.coursework.controller;

import com.smartcore.coursework.dto.TeamWithMembersDTO;
import com.smartcore.coursework.model.Team;
import com.smartcore.coursework.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Teams", description = "API for managing teams and their members")
public class TeamController {

    private final TeamService teamService;

    @Operation(
            summary = "Get a list of all teams",
            description = "Returns a list of all teams in the system. Access Level: LOW"
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.id, T(com.smartcore.coursework.model.AccessLevel).LOW)")
    @GetMapping("/list")
    public ResponseEntity<List<Team>> getAllTeams() {
        log.info("Fetching all teams.");
        List<Team> teams = teamService.getAllTeams();
        log.info("Teams retrieved: {}", teams.size());
        return ResponseEntity.ok(teams);
    }

    @Operation(
            summary = "Get teams with their members",
            description = "Returns a list of all teams with their associated members. Access Level: LOW"
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.id, T(com.smartcore.coursework.model.AccessLevel).LOW)")
    @GetMapping
    public ResponseEntity<List<TeamWithMembersDTO>> teamsWithMembers() {
        log.info("Fetching all teams with their members.");
        List<TeamWithMembersDTO> teamsWithMembers = teamService.getAllTeamsWithMembers();
        log.info("Teams with members retrieved: {}", teamsWithMembers.size());
        return ResponseEntity.ok(teamsWithMembers);
    }

    @Operation(
            summary = "Create a new team",
            description = "Allows you to create a new team. Access Level: HIGH"
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.id, T(com.smartcore.coursework.model.AccessLevel).HIGH)")
    @PostMapping
    public ResponseEntity<Team> createTeam(@RequestBody Team team) {
        log.info("Creating new team: {}", team);
        Team createdTeam = teamService.save(team);
        log.info("New team created: {}", createdTeam);
        return ResponseEntity.ok(createdTeam);
    }

    @Operation(
            summary = "Update a team",
            description = "Allows you to update the name of a team by its ID. Access Level: HIGH"
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.id, T(com.smartcore.coursework.model.AccessLevel).HIGH)")
    @PutMapping("/{id}")
    public ResponseEntity<Team> updateTeam(
            @Parameter(description = "The ID of the team to update", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated team data", required = true)
            @RequestBody Team updatedTeam) {
        log.info("Updating team with ID: {}", id);
        Team existingTeam = teamService.getTeamById(id);
        existingTeam.setName(updatedTeam.getName());
        Team savedTeam = teamService.save(existingTeam);
        log.info("Team updated successfully: {}", savedTeam);
        return ResponseEntity.ok(savedTeam);
    }

    @Operation(
            summary = "Delete a team",
            description = "Deletes a team by its ID. Access Level: HIGH"
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.id, T(com.smartcore.coursework.model.AccessLevel).HIGH)")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTeam(@PathVariable String id) {
        log.info("Deleting team with ID: {}", id);
        teamService.deleteTeamById(id);
        log.info("Team with ID: {} has been deleted", id);
        return ResponseEntity.ok("Team deleted successfully.");
    }
}