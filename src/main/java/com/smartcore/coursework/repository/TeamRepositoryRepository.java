package com.smartcore.coursework.repository;

import com.smartcore.coursework.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepositoryRepository extends JpaRepository<Team, String> {
}
