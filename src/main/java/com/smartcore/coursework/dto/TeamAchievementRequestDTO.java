package com.smartcore.coursework.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamAchievementRequestDTO {
    @NotNull
    private Integer points;
    @NotNull
    private String teamName;
    private String description;
    @NotNull
    private String name;
}
