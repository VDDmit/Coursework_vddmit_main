package com.smartcore.coursework.service;

import com.smartcore.coursework.exception.AccessDeniedException;
import com.smartcore.coursework.exception.EntityNotFoundException;
import com.smartcore.coursework.model.*;
import com.smartcore.coursework.repository.AppUserRepository;
import com.smartcore.coursework.repository.ProjectRepository;
import com.smartcore.coursework.repository.TaskRepository;
import com.smartcore.coursework.repository.UserProjectRepository;
import com.smartcore.coursework.util.ClassUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;
    private final TaskRepository taskRepository;
    private final AppUserAndTokenService appUserAndTokenService;
    private final AppUserRepository appUserRepository;

    public List<Project> getProjectsByUserId(String userId) {
        validateUserId(userId);

        List<UserProject> userProjects = userProjectRepository.findByUserId(userId);
        if (userProjects.isEmpty()) {
            throw new EntityNotFoundException("No projects found for user ID: " + userId + " in " + ClassUtils.getClassAndMethodName());
        }

        return userProjects.stream()
                .map(UserProject::getProject)
                .toList();
    }

    public Project save(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null in " + ClassUtils.getClassAndMethodName());
        }

        Project savedProject = projectRepository.save(project);

        UserProject userProject = UserProject.builder()
                .project(savedProject)
                .user(savedProject.getOwner())
                .build();
        userProjectRepository.save(userProject);

        return savedProject;
    }

    public void addUserToProject(String username, String projectId) {
        validateProjectId(projectId);

        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with ID: " + projectId));

        if (userProjectRepository.existsByProjectIdAndUserId(projectId, user.getId())) {
            throw new IllegalArgumentException("User is already assigned to this project.");
        }

        UserProject userProject = UserProject.builder()
                .user(user)
                .project(project)
                .build();

        userProjectRepository.save(userProject);
    }


    public Project getProjectById(String projectId) {
        validateProjectId(projectId);

        return projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with ID: " + projectId + " in " + ClassUtils.getClassAndMethodName()));
    }

    public boolean delete(String projectId, String userId) {
        validateProjectId(projectId);
        validateUserId(userId);

        checkAccessToProject(projectId, userId, AccessLevel.HIGH);

        if (!projectRepository.existsById(projectId)) {
            throw new EntityNotFoundException("Project with ID " + projectId + " not found in " + ClassUtils.getClassAndMethodName());
        }

        projectRepository.deleteById(projectId);
        return true;
    }

    public List<Task> getTasksByProjectId(String projectId) {
        validateProjectId(projectId);

        List<Task> tasks = taskRepository.findByProjectId(projectId);
        if (tasks.isEmpty()) {
            throw new EntityNotFoundException("No tasks found for project with ID: " + projectId + " in " + ClassUtils.getClassAndMethodName());
        }

        return tasks;
    }

    public boolean isProjectOwner(String projectId, String userId) {
        validateProjectId(projectId);
        validateUserId(userId);

        return userProjectRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    public boolean isUserAssignedToProject(String projectId, String userId) {
        validateProjectId(projectId);
        validateUserId(userId);

        return userProjectRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    public void checkAccessToProject(String projectId, String userId, AccessLevel accessLevel) {
        validateProjectId(projectId);
        validateUserId(userId);

        boolean isProjectOwner = isProjectOwner(projectId, userId);
        boolean isUserAssignedToProject = isUserAssignedToProject(projectId, userId);
        boolean hasRequiredAccess = appUserAndTokenService.hasRequiredAccess(userId, accessLevel);

        if (!isProjectOwner && !isUserAssignedToProject && !hasRequiredAccess) {
            throw new AccessDeniedException("You do not have sufficient permissions to access this project in " + ClassUtils.getClassAndMethodName());
        }
    }

    private void validateProjectId(String projectId) {
        if (projectId == null || projectId.isEmpty()) {
            throw new IllegalArgumentException("Project ID cannot be null or empty in " + ClassUtils.getClassAndMethodName());
        }
    }

    private void validateUserId(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty in " + ClassUtils.getClassAndMethodName());
        }
    }
}