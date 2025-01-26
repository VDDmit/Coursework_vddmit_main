package com.smartcore.coursework.service.gamification;

import com.smartcore.coursework.dto.TeamAchievementRequestDTO;
import com.smartcore.coursework.dto.TeamWithAchievementPointsDTO;
import com.smartcore.coursework.model.Team;
import com.smartcore.coursework.model.TeamAchievement;
import com.smartcore.coursework.repository.TeamAchievementRepository;
import com.smartcore.coursework.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamAchievementService {
    private final TeamAchievementRepository teamAchievementRepository;
    private final TeamRepository teamRepository;

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

    public TeamAchievement createTeamAchievement(TeamAchievementRequestDTO request) {
        if (!teamRepository.existsByName(request.getTeamName())) {
            throw new EntityNotFoundException("Team with name '" + request.getTeamName() + "' does not exist");
        }
        Team team = teamRepository.findByName(request.getTeamName())
                .orElseThrow(() -> new EntityNotFoundException("Team with name '" + request.getTeamName() + "' not found"));

        TeamAchievement teamAchievement = TeamAchievement.builder()
                .points(request.getPoints())
                .team(team)
                .description(request.getDescription())
                .name(request.getName())
                .build();
        return teamAchievementRepository.save(teamAchievement);
    }


}
