package com.smartcore.coursework.service.gamification;

import com.smartcore.coursework.dto.TeamWithAchievementPointsDTO;
import com.smartcore.coursework.model.Team;
import com.smartcore.coursework.repository.TeamAchievementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamAchievementScoreService {
    private final TeamAchievementRepository teamAchievementRepository;

    public List<TeamWithAchievementPointsDTO> getAllTeamsWithAchievementPointsSortedDescending() {
        return teamAchievementRepository.findTeamAchievementsWithPointsNative()
                .stream()
                .map(row -> {
                    String teamId = (String) row[0];
                    String teamName = (String) row[1];
                    Integer achievementPoints = ((Number) row[2]).intValue();
                    Team team = Team.builder()
                            .id(teamId)
                            .name(teamName)
                            .build();

                    return new TeamWithAchievementPointsDTO(team, achievementPoints);
                })
                .sorted((dto1, dto2) -> dto2.getAchievementPoints().compareTo(dto1.getAchievementPoints()))
                .collect(Collectors.toList());
    }

}
