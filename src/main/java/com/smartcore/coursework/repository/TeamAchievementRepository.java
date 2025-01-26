package com.smartcore.coursework.repository;

import com.smartcore.coursework.model.TeamAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamAchievementRepository extends JpaRepository<TeamAchievement, String> {
    @Query(value = """
                SELECT t.id AS teamId, t.name AS teamName, SUM(ta.points) AS achievementPoints
                FROM team_achievement ta
                JOIN team t ON ta.team_id = t.id
                GROUP BY t.id, t.name
            """, nativeQuery = true)
    List<Object[]> findTeamAchievementsWithPointsNative();
}
