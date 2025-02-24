package com.smartcore.coursework.dto;

import com.smartcore.coursework.model.Team;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamWithMembersAndTotalXpDTO {
    UserDTO leader;
    private Team team;
    private List<UserDTO> members;
    private Integer totalXp;
}
