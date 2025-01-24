package com.smartcore.coursework.service;

import com.smartcore.coursework.exception.AccessDeniedException;
import com.smartcore.coursework.model.AccessLevel;
import com.smartcore.coursework.model.Project;
import com.smartcore.coursework.model.Task;
import com.smartcore.coursework.model.UserProject;
import com.smartcore.coursework.repository.ProjectRepository;
import com.smartcore.coursework.repository.TaskRepository;
import com.smartcore.coursework.repository.UserProjectRepository;
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

    public List<Project> getProjectsByUserId(String userId) {
        List<UserProject> userProjects = userProjectRepository.findByUserId(userId);
        return userProjects.stream()
                .map(UserProject::getProject)
                .toList();
    }

    public Project save(Project project) {
        return projectRepository.save(project);
    }

    public Project getProjectById(String projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }

    public void delete(String projectId, String userId) {
        checkAccessToProject(projectId, userId, AccessLevel.HIGH);
        projectRepository.deleteById(projectId);
    }

    public List<Task> getTasksByProjectId(String projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    public boolean isProjectOwner(String projectId, String userId) {
        return userProjectRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    public boolean isUserAssignedToProject(String projectId, String userId) {
        return userProjectRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    public void checkAccessToProject(String projectId, String userId, AccessLevel accessLevel) {
        boolean isProjectOwner = isProjectOwner(projectId, userId);
        boolean isUserAssignedToProject = isUserAssignedToProject(projectId, userId);
        boolean hasRequiredAccess = appUserAndTokenService.hasRequiredAccess(userId, accessLevel);
        if (!isProjectOwner && !isUserAssignedToProject && !hasRequiredAccess) {
            throw new AccessDeniedException("You do not have sufficient permissions to access this project.");
        }
    }
}