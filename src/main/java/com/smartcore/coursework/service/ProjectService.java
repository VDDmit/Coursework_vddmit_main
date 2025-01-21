package com.smartcore.coursework.service;

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

    public void delete(String projectId) {
        projectRepository.deleteById(projectId);
    }

    public List<Task> getTasksByProjectId(String projectId) {
        return taskRepository.findByProjectId(projectId);
    }
}