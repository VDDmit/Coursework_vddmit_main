package com.smartcore.coursework.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserMerchAchievementDTO {
    @NotNull
    private String name;
    private String description;
}
