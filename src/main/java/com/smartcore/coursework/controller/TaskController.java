package com.smartcore.coursework.controller;

import com.smartcore.coursework.model.Task;
import com.smartcore.coursework.service.TaskService;
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
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "API for tasks management")
public class TaskController {
    private final TaskService taskService;

    @Operation(
            summary = "Get tasks by assigned user username",
            description = "Retrieve a list of tasks assigned to the currently authenticated user. Requires LOW access level."
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.username, T(com.smartcore.coursework.model.AccessLevel).LOW)")
    @GetMapping("/list")
    public ResponseEntity<List<Task>> getTasksByAssignedUserUsername(Authentication authentication) {
        String username = authentication.getName();
        log.info("Fetching tasks assigned to user: {}", username);
        List<Task> tasks = taskService.getTasksByAssignedUserUsername(username);
        log.info("Fetched {} tasks for user: {}", tasks.size(), username);
        return ResponseEntity.ok(tasks);
    }

    @Operation(
            summary = "Get all tasks for a project",
            description = "Retrieve a list of all tasks associated with a specific project ID. Requires LOW access level."
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.username, T(com.smartcore.coursework.model.AccessLevel).LOW)")
    @GetMapping("/list-for-project/{projectId}")
    public ResponseEntity<List<Task>> getAllTasksForProject(
            @Parameter(description = "The ID of the project", required = true)
            @PathVariable("projectId") String projectId) {
        log.info("Fetching all tasks for project ID: {}", projectId);
        List<Task> tasks = taskService.getAllTasksForProject(projectId);
        log.info("Fetched {} tasks for project ID: {}", tasks.size(), projectId);
        return ResponseEntity.ok(tasks);
    }

    @Operation(
            summary = "Create a new task",
            description = "Create a new task. Requires MEDIUM access level."
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.username, T(com.smartcore.coursework.model.AccessLevel).MEDIUM)")
    @PostMapping
    public ResponseEntity<Task> createTask(
            @Parameter(description = "Task data to create", required = true)
            @RequestBody Task task) {
        log.info("Creating a new task with title: {}", task.getTitle());
        if (task.getAssignedUser() == null || task.getProject() == null) {
            log.warn("Assigned user or project cannot be null for task creation");
            return ResponseEntity.badRequest().body(null);
        }
        Task createdTask = taskService.save(task);
        log.info("Task created successfully with ID: {}", createdTask.getId());
        return ResponseEntity.ok(createdTask);
    }

    @Operation(
            summary = "Get task by ID",
            description = "Retrieve detailed information about a specific task. Requires MEDIUM access level."
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.username, T(com.smartcore.coursework.model.AccessLevel).MEDIUM)")
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(
            @Parameter(description = "The ID of the task to retrieve", required = true)
            @PathVariable("id") String id) {
        log.info("Fetching task with ID: {}", id);
        Task task = taskService.getTaskById(id);
        log.info("Task retrieved successfully: {}", task);
        return ResponseEntity.ok(task);
    }

    @Operation(
            summary = "Update a task",
            description = "Update an existing task. Requires MEDIUM access level."
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.username, T(com.smartcore.coursework.model.AccessLevel).MEDIUM)")
    @PutMapping
    public ResponseEntity<Task> updateTask(
            @Parameter(description = "Updated task data", required = true)
            @RequestBody Task updatedTask) {
        log.info("Updating task with ID: {}", updatedTask.getId());
        Task existingTask = taskService.getTaskById(updatedTask.getId());
        if (existingTask == null) {
            log.warn("Task with ID {} not found for update", updatedTask.getId());
            return ResponseEntity.notFound().build();
        }
        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setCompleted(updatedTask.getCompleted());
        existingTask.setAssignedUser(updatedTask.getAssignedUser());
        existingTask.setProject(updatedTask.getProject());
        Task savedTask = taskService.save(existingTask);
        log.info("Task updated successfully: {}", savedTask);
        return ResponseEntity.ok(savedTask);
    }

    @Operation(
            summary = "Delete a task by ID",
            description = "Delete a task by its ID. Requires MEDIUM access level."
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.username, T(com.smartcore.coursework.model.AccessLevel).MEDIUM)")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(
            @Parameter(description = "The ID of the task to delete", required = true)
            @PathVariable("id") String id) {
        log.info("Attempting to delete task with ID: {}", id);
        taskService.delete(id);
        log.info("Task deleted successfully with ID: {}", id);
        return ResponseEntity.ok("Task deleted successfully.");
    }

    @Operation(
            summary = "Mark a task as completed",
            description = "Marks a specific task as completed by setting its 'completed' field to true. Requires MEDIUM access level."
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.username, T(com.smartcore.coursework.model.AccessLevel).MEDIUM)")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<String> markTaskAsCompleted(
            @Parameter(description = "The ID of the task to mark as completed", required = true)
            @PathVariable("id") String taskId) {
        log.info("Attempting to mark task with ID: {} as completed", taskId);
        try {
            taskService.markTuskAsComplete(taskId);
            log.info("Task with ID: {} successfully marked as completed", taskId);
            return ResponseEntity.ok("Task marked as completed.");
        } catch (Exception e) {
            log.error("Error while marking task with ID: {} as completed: {}", taskId, e.getMessage());
            return ResponseEntity.status(400).body("Failed to mark task as completed. " + e.getMessage());
        }
    }
    @Operation(
            summary = "Mark a task as incomplete",
            description = "Marks a specific task as incomplete by setting its 'completed' field to false. Requires MEDIUM access level."
    )
    @PreAuthorize("@appUserAndTokenService.hasRequiredAccess(authentication.principal.username, T(com.smartcore.coursework.model.AccessLevel).MEDIUM)")
    @PatchMapping("/{id}/incomplete")
    public ResponseEntity<String> markTaskAsIncomplete(
            @Parameter(description = "The ID of the task to mark as incomplete", required = true)
            @PathVariable("id") String taskId) {
        log.info("Attempting to mark task with ID: {} as incomplete", taskId);
        try {
            taskService.markTaskAsIncomplete(taskId);
            log.info("Task with ID: {} successfully marked as incomplete", taskId);
            return ResponseEntity.ok("Task marked as incomplete.");
        } catch (Exception e) {
            log.error("Error while marking task with ID: {} as incomplete: {}", taskId, e.getMessage());
            return ResponseEntity.status(400).body("Failed to mark task as incomplete. " + e.getMessage());
        }
    }
}