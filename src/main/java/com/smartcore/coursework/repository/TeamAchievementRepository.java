package com.smartcore.coursework.repository;

import com.smartcore.coursework.model.TeamAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamAchievementRepository extends JpaRepository<TeamAchievement, String> {
}
