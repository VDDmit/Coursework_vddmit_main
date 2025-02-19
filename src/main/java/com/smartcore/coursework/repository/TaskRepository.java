package com.smartcore.coursework.repository;

import com.smartcore.coursework.model.Project;
import com.smartcore.coursework.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {
    List<Task> findByProjectId(String projectId);

    List<Task> findByAssignedUserUsername(String userName);

    boolean existsByTitleAndProject(String title, Project project);
}
