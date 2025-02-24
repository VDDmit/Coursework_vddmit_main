package com.smartcore.coursework.repository;


import com.smartcore.coursework.model.AppUser;
import com.smartcore.coursework.model.Project;
import com.smartcore.coursework.model.UserProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProjectRepository extends JpaRepository<UserProject, String> {
    List<UserProject> findByUserId(String userId);

    boolean existsByProjectIdAndUserId(String projectId, String userId);

    void deleteByProjectIdAndUserId(String projectId, String id);

    boolean existsByUserAndProject(AppUser user, Project project);
}
