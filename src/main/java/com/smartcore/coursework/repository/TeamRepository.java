package com.smartcore.coursework.repository;

import com.smartcore.coursework.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, String> {
    boolean existsByName(String name);

    Optional<Team> findByName(String name);
}
