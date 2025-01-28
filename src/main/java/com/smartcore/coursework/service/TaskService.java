package com.smartcore.coursework.service;

import com.smartcore.coursework.exception.EntityNotFoundException;
import com.smartcore.coursework.model.Task;
import com.smartcore.coursework.repository.AppUserRepository;
import com.smartcore.coursework.repository.TaskRepository;
import com.smartcore.coursework.util.ClassUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final AppUserRepository appUserRepository;

    public List<Task> getTasksByAssignedUserUsername(String userName) {
        validateUserExistence(userName);

        return taskRepository.findByAssignedUserUsername(userName);
    }

    public List<Task> getAllTasksForProject(String projectId) {
        validateProjectId(projectId);

        List<Task> tasks = taskRepository.findByProjectId(projectId);
        if (tasks.isEmpty()) {
            throw new EntityNotFoundException("No tasks found for project with ID: " + projectId + " in " + ClassUtils.getClassAndMethodName());
        }

        return tasks;
    }

    public Task save(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null in " + ClassUtils.getClassAndMethodName());
        }

        return taskRepository.save(task);
    }

    public void delete(String taskId) {
        validateTaskId(taskId);

        if (!taskRepository.existsById(taskId)) {
            throw new EntityNotFoundException("Task with ID " + taskId + " not found in " + ClassUtils.getClassAndMethodName());
        }

        taskRepository.deleteById(taskId);
    }

    public Task getTaskById(String taskId) {
        validateTaskId(taskId);

        return taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + taskId + " in " + ClassUtils.getClassAndMethodName()));
    }

    public void markTuskAsComplete(String taskId) {
        Task recievedTask = getTaskById(taskId);
        recievedTask.setCompleted(true);
        taskRepository.save(recievedTask);
    }

    public void markTaskAsIncomplete(String taskId) {
        Task receivedTask = getTaskById(taskId);
        receivedTask.setCompleted(false);
        taskRepository.save(receivedTask);
    }

    private void validateUserExistence(String userName) {
        if (!appUserRepository.existsByUsername(userName)) {
            throw new EntityNotFoundException("User not found with username: " + userName + " in " + ClassUtils.getClassAndMethodName());
        }
    }

    private void validateProjectId(String projectId) {
        if (projectId == null || projectId.isEmpty()) {
            throw new IllegalArgumentException("Project ID cannot be null or empty in " + ClassUtils.getClassAndMethodName());
        }
    }

    private void validateTaskId(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            throw new IllegalArgumentException("Task ID cannot be null or empty in " + ClassUtils.getClassAndMethodName());
        }
    }
}