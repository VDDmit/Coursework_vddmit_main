package com.smartcore.coursework.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateTeamDTO {
    private String id;
    private String name;
    private String leaderId;
}
