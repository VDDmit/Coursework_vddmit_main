package com.smartcore.coursework.controller;

import com.smartcore.coursework.model.Project;
import com.smartcore.coursework.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Operations related to projects")
public class ProjectController {

    private final ProjectService projectService;

    @Operation(
            summary = "Get projects by user ID",
            description = "Retrieve a list of projects associated with a specific user ID. Requires LOW access level."
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.id, T(com.smartcore.coursework.model.AccessLevel).LOW)")
    @GetMapping
    public ResponseEntity<List<Project>> getProjectsByUserId(
            @Parameter(description = "The ID of the user whose projects are being retrieved", required = true)
            @RequestParam String userId) {
        log.info("Fetching projects for userId: {}", userId);
        return ResponseEntity.ok(projectService.getProjectsByUserId(userId));
    }

    @Operation(
            summary = "Create a new project",
            description = "Creates a new project. Requires MEDIUM access level."
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.id, T(com.smartcore.coursework.model.AccessLevel).MEDIUM)")
    @PostMapping
    public ResponseEntity<Project> createProject(
            @Parameter(description = "Project data to be created", required = true)
            @RequestBody Project project) {
        log.info("Creating project: {}", project);
        Project createdProject = projectService.save(project);
        log.info("Project created successfully: {}", createdProject);
        return ResponseEntity.ok(createdProject);
    }

    @Operation(
            summary = "Get project by ID",
            description = "Retrieve detailed information about a project, including its tasks. Requires LOW access level."
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.id, T(com.smartcore.coursework.model.AccessLevel).LOW)")
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(
            @Parameter(description = "The ID of the project to retrieve", required = true)
            @PathVariable String id) {
        log.info("Fetching project by ID: {}", id);
        Project project = projectService.getProjectById(id);
        log.info("Project retrieved: {}", project);
        return ResponseEntity.ok(project);
    }

    @Operation(
            summary = "Update project by ID",
            description = "Updates the project with the specified ID. Requires HIGH access level."
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.id, T(com.smartcore.coursework.model.AccessLevel).HIGH)")
    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(
            @Parameter(description = "The ID of the project to update", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated project data", required = true)
            @RequestBody Project updatedProject) {
        log.info("Updating project with ID: {}", id);
        Project project = projectService.getProjectById(id);
        project.setName(updatedProject.getName());
        project.setDescription(updatedProject.getDescription());
        Project savedProject = projectService.save(project);
        log.info("Project updated successfully: {}", savedProject);
        return ResponseEntity.ok(savedProject);
    }

    @Operation(
            summary = "Delete project by ID",
            description = "Deletes the project with the specified ID. Requires HIGH access level."
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.id, T(com.smartcore.coursework.model.AccessLevel).HIGH)")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProject(
            @Parameter(description = "The ID of the project to delete", required = true)
            @PathVariable String id,
            Authentication authentication) {
        String userId = authentication.getPrincipal().toString();
        log.info("User with ID {} is attempting to delete project with ID {}", userId, id);
        projectService.delete(id, userId);
        log.info("Project deleted successfully with ID: {}", id);
        return ResponseEntity.ok("Project deleted successfully.");
    }
}