package com.smartcore.coursework.dto;

import com.smartcore.coursework.model.Team;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamWithAchievementPointsDTO {
    private Team team;
    private Integer achievementPoints;
}
